package ch.fitnesslab.product.infrastructure.wix

import ch.fitnesslab.domain.value.ProductVariantId
import ch.fitnesslab.generated.model.ProductView
import ch.fitnesslab.product.application.FindAllProductsQuery
import ch.fitnesslab.product.application.ProductUpdatedUpdate
import ch.fitnesslab.product.domain.*
import ch.fitnesslab.product.domain.commands.AddLinkedPlatformCommand
import ch.fitnesslab.product.domain.commands.CreateProductCommand
import ch.fitnesslab.product.domain.commands.UpdateProductCommand
import ch.fitnesslab.product.infrastructure.ProductRepository
import ch.fitnesslab.product.infrastructure.ProductVariantEntity
import ch.fitnesslab.product.infrastructure.wix.v3.WixBillingTerms
import ch.fitnesslab.product.infrastructure.wix.v3.WixPlan
import ch.fitnesslab.product.infrastructure.wix.v3.WixPricingVariantV3
import ch.fitnesslab.utils.waitForUpdateOf
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.security.MessageDigest
import java.time.Instant
import java.util.*

@Service
class WixSyncService(
    private val wixClient: WixClient,
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
    private val productRepository: ProductRepository,
) {
    private val logger = LoggerFactory.getLogger(WixSyncService::class.java)

    fun uploadProduct(productId: String) {
        val product = this.productRepository.findById(UUID.fromString(productId))

        if (product.isPresent) {
            val actualProduct = product.get()
            val wixPlatform = actualProduct.linkedPlatforms?.find { it.platformName == "wix" }
            val isNotPresentOnPlatformYet = wixPlatform == null

            if (isNotPresentOnPlatformYet) {
                try {
                    val wixResponse = this.wixClient.uploadPricingPlanToWix(actualProduct)

                    // After successful upload, create an UpdateProductCommand with the Wix linked platform
                    val linkedPlatforms =
                        (actualProduct.linkedPlatforms?.toMutableList() ?: mutableListOf()).apply {
                            add(
                                LinkedPlatformSync(
                                    platformName = "wix",
                                    idOnPlatform = wixResponse?.id,
                                    revision = wixResponse?.revision,
                                    visibilityOnPlatform = mapWixVisibility(wixResponse?.visibility),
                                    isSynced = true,
                                    isSourceOfTruth = false,
                                    lastSyncedAt = Instant.now(),
                                    syncError = null,
                                    hasLocalChanges = false,
                                    hasIncomingChanges = false,
                                    localHash = null,
                                    remoteHash = null, // Will be computed on next sync
                                ),
                            )
                        }

                    val updateCommand =
                        AddLinkedPlatformCommand(
                            productId = ProductVariantId(actualProduct.productId),
                            linkedPlatforms = linkedPlatforms,
                        )

                    val subscriptionQuery =
                        queryGateway.subscriptionQuery(
                            FindAllProductsQuery(),
                            ResponseTypes.multipleInstancesOf(ProductView::class.java),
                            ResponseTypes.instanceOf(ProductUpdatedUpdate::class.java),
                        )

                    try {
                        commandGateway.sendAndWait<Unit>(updateCommand)
                        waitForUpdateOf(subscriptionQuery)
                        logger.info("Successfully uploaded and linked product to Wix: ${wixResponse?.id}")
                    } finally {
                        subscriptionQuery.close()
                    }
                } catch (e: Exception) {
                    logger.error("Failed to upload product to Wix: ${e.message}", e)
                    throw WixSyncException("Failed to upload product to Wix", e)
                }
            } else {
                // Product is already linked to Wix, perform update
                try {
                    val wixPlanId = wixPlatform?.idOnPlatform
                    val revision = wixPlatform?.revision

                    if (wixPlanId == null) {
                        logger.warn("Product is linked to Wix but has no Wix plan ID")
                        return
                    }

                    val wixResponse = this.wixClient.updatePricingPlanOnWix(actualProduct, wixPlanId, revision)

                    // After successful update, update the linked platform info
                    val linkedPlatforms =
                        (actualProduct.linkedPlatforms?.toMutableList() ?: mutableListOf()).apply {
                            removeIf { it.platformName == "wix" }
                            add(
                                LinkedPlatformSync(
                                    platformName = "wix",
                                    idOnPlatform = wixResponse?.id,
                                    revision = wixResponse?.revision,
                                    visibilityOnPlatform = mapWixVisibility(wixResponse?.visibility),
                                    isSynced = true,
                                    isSourceOfTruth = false,
                                    lastSyncedAt = Instant.now(),
                                    syncError = null,
                                    hasLocalChanges = false, // Reset after successful upload
                                    hasIncomingChanges = false, // Reset after successful upload
                                    localHash = null,
                                    remoteHash =
                                        computeWixPlanHash(
                                            WixPlan(
                                                id = wixResponse?.id,
                                                revision = wixResponse?.revision,
                                                name = wixResponse?.name,
                                                slug = wixResponse?.slug,
                                                description = wixResponse?.description,
                                                visibility = wixResponse?.visibility,
                                                buyable = wixResponse?.buyable,
                                                buyerCanCancel = wixResponse?.buyerCanCancel,
                                                maxPurchasesPerBuyer = wixResponse?.maxPurchasesPerBuyer,
                                                perks = wixResponse?.perks ?: emptyList(),
                                                pricingVariants = wixResponse?.pricingVariants ?: emptyList(),
                                                createdDate = wixResponse?.createdDate,
                                                updatedDate = wixResponse?.updatedDate,
                                                status = wixResponse?.status,
                                                archived = wixResponse?.archived,
                                                primary = wixResponse?.primary,
                                                currency = wixResponse?.currency,
                                            ),
                                        ),
                                ),
                            )
                        }

                    val updateCommand =
                        AddLinkedPlatformCommand(
                            productId = ProductVariantId(actualProduct.productId),
                            linkedPlatforms = linkedPlatforms,
                        )

                    val subscriptionQuery =
                        queryGateway.subscriptionQuery(
                            FindAllProductsQuery(),
                            ResponseTypes.multipleInstancesOf(ProductView::class.java),
                            ResponseTypes.instanceOf(ProductUpdatedUpdate::class.java),
                        )

                    try {
                        commandGateway.sendAndWait<Unit>(updateCommand)
                        waitForUpdateOf(subscriptionQuery)
                        logger.info("Successfully updated product on Wix: ${wixResponse?.id}")
                    } finally {
                        subscriptionQuery.close()
                    }
                } catch (e: Exception) {
                    logger.error("Failed to update product on Wix: ${e.message}", e)
                    throw WixSyncException("Failed to update product on Wix", e)
                }
            }
        }
    }

    fun checkForWixUpdatesForAll() {
        try {
            val wixPlans = fetchWixPlans()
            checkForUpdatesFrom(wixPlans)
        } catch (e: Exception) {
            logger.error("Wix check for updates failed: ${e.message}", e)
        }
    }

    fun applyAllWixUpdates() {
        try {
            val wixPlans = fetchWixPlans()
            applyUpdatesFrom(wixPlans)
        } catch (e: Exception) {
            logger.error("Wix apply updates failed: ${e.message}", e)
        }
    }

    fun checkForWixUpdates(productId: String) {
        val product = this.productRepository.findById(UUID.fromString(productId))

        if (product.isEmpty) {
            logger.warn("Product not found: $productId")
            return
        }

        val actualProduct = product.get()
        val wixPlatform = actualProduct.linkedPlatforms?.find { it.platformName == "wix" }

        if (wixPlatform == null || wixPlatform.idOnPlatform == null) {
            logger.warn("Product $productId is not linked to Wix")
            return
        }

        try {
            val wixPlanId = wixPlatform.idOnPlatform
            val wixPlan = wixClient.fetchPlanById(wixPlanId)

            if (wixPlan != null) {
                val remoteHash = computeWixPlanHash(wixPlan)
                val storedRemoteHash = wixPlatform.remoteHash

                if (remoteHash != storedRemoteHash) {
                    logger.info("Found updates on Wix for product $productId")
                    markProductWithIncomingChanges(wixPlan, actualProduct, remoteHash)
                } else {
                    logger.info("No updates found on Wix for product $productId")
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to check for Wix updates for product $productId: ${e.message}", e)
            throw WixSyncException("Failed to check for Wix updates", e)
        }
    }

    fun downloadProductFromWix(productId: String) {
        val product = this.productRepository.findById(UUID.fromString(productId))

        if (product.isEmpty) {
            logger.warn("Product not found: $productId")
            return
        }

        val actualProduct = product.get()
        val wixPlatform = actualProduct.linkedPlatforms?.find { it.platformName == "wix" }

        if (wixPlatform == null || wixPlatform.idOnPlatform == null) {
            logger.warn("Product $productId is not linked to Wix")
            return
        }

        try {
            val wixPlanId = wixPlatform.idOnPlatform
            val wixPlan = wixClient.fetchPlanById(wixPlanId)

            if (wixPlan != null) {
                val remoteHash = computeWixPlanHash(wixPlan)
                updateProductFromWixPlan(wixPlan, actualProduct, remoteHash)
                logger.info("Successfully downloaded and applied Wix changes for product $productId")
            }
        } catch (e: Exception) {
            logger.error("Failed to download product from Wix for product $productId: ${e.message}", e)
            throw WixSyncException("Failed to download product from Wix", e)
        }
    }

    private fun fetchWixPlans(): List<WixPlan> {
        val wixPlans = wixClient.fetchPricingPlans()
        logger.info("Fetched ${wixPlans.size} Wix pricing plans")
        return wixPlans
    }

    private fun checkForUpdatesFrom(wixPlans: List<WixPlan>) {
        val subscriptionQuery =
            queryGateway.subscriptionQuery(
                FindAllProductsQuery(),
                ResponseTypes.multipleInstancesOf(ProductView::class.java),
                ResponseTypes.instanceOf(ProductUpdatedUpdate::class.java),
            )

        try {
            var updatesExpected = 0
            wixPlans.forEach { wixPlan ->
                val wixPlanId = wixPlan.id
                if (!wixPlanId.isNullOrBlank()) {
                    val existingProduct = findProductByWixId(wixPlanId)
                    if (existingProduct != null) {
                        val remoteHash = computeWixPlanHash(wixPlan)
                        val wixPlatform = existingProduct.linkedPlatforms?.find { it.platformName == "wix" }
                        val storedRemoteHash = wixPlatform?.remoteHash
                        if (remoteHash != storedRemoteHash) {
                            updatesExpected++
                        }
                    }
                }
            }

            logger.info("Expecting $updatesExpected updates from Wix check")

            wixPlans.forEach { wixPlan ->
                checkForUpdatesForPlanWithoutSubscription(wixPlan)
            }

            // Wait for all expected updates
            for (i in 1..updatesExpected) {
                subscriptionQuery.updates().blockFirst(java.time.Duration.ofSeconds(5))
            }

            logger.info("Wix check for updates completed successfully")
        } finally {
            subscriptionQuery.close()
        }
    }

    private fun applyUpdatesFrom(wixPlans: List<WixPlan>) {
        val subscriptionQuery =
            queryGateway.subscriptionQuery(
                FindAllProductsQuery(),
                ResponseTypes.multipleInstancesOf(ProductView::class.java),
                ResponseTypes.instanceOf(ProductUpdatedUpdate::class.java),
            )

        try {
            var updatesExpected = 0
            wixPlans.forEach { wixPlan ->
                val wixPlanId = wixPlan.id
                if (!wixPlanId.isNullOrBlank()) {
                    val existingProduct = findProductByWixId(wixPlanId)
                    if (existingProduct != null) {
                        val wixPlatform = existingProduct.linkedPlatforms?.find { it.platformName == "wix" }
                        // If there are incoming changes marked, we need to apply them
                        if (wixPlatform?.hasIncomingChanges == true) {
                            logger.debug("Product $wixPlanId has incoming changes marked, will apply")
                            updatesExpected++
                        } else {
                            // Otherwise check hash difference
                            val remoteHash = computeWixPlanHash(wixPlan)
                            val storedRemoteHash = wixPlatform?.remoteHash
                            if (remoteHash != storedRemoteHash) {
                                logger.debug("Product $wixPlanId needs update - remote: $remoteHash, stored: $storedRemoteHash")
                                updatesExpected++
                            }
                        }
                    } else {
                        logger.debug("Product $wixPlanId does not exist, will be created")
                        updatesExpected++ // New product will be created
                    }
                }
            }

            logger.info("Expecting $updatesExpected updates from Wix apply")

            if (updatesExpected == 0) {
                logger.info("No updates needed, skipping")
                return
            }

            wixPlans.forEach { wixPlan ->
                try {
                    applyUpdatesForPlanWithoutSubscription(wixPlan)
                } catch (e: Exception) {
                    logger.error("Error applying update for plan ${wixPlan.id}: ${e.message}", e)
                }
            }

            // Wait for all expected updates
            logger.info("Waiting for $updatesExpected update notifications...")
            for (i in 1..updatesExpected) {
                try {
                    val update = subscriptionQuery.updates().blockFirst(java.time.Duration.ofSeconds(10))
                    logger.info("Received update notification $i of $updatesExpected")
                } catch (e: Exception) {
                    logger.error("Error waiting for update $i: ${e.message}", e)
                }
            }

            logger.info("Wix apply updates completed successfully")
        } finally {
            subscriptionQuery.close()
        }
    }

    private fun checkForUpdatesForPlanWithoutSubscription(wixPlan: WixPlan) {
        val wixPlanId = wixPlan.id

        try {
            if (wixPlanId.isNullOrBlank()) {
                logger.warn("Skipping Wix plan without ID: ${wixPlan.name}")
                return
            }

            val existingProduct = findProductByWixId(wixPlanId)

            if (existingProduct != null) {
                logger.debug("Checking for updates for product with Wix ID $wixPlanId")
                val remoteHash = computeWixPlanHash(wixPlan)
                val wixPlatform = existingProduct.linkedPlatforms?.find { it.platformName == "wix" }
                val storedRemoteHash = wixPlatform?.remoteHash

                if (remoteHash != storedRemoteHash) {
                    logger.info("Detected changes from Wix for product $wixPlanId - marking as having incoming changes")
                    markProductWithIncomingChangesWithoutSubscription(wixPlan, existingProduct, remoteHash)
                } else {
                    logger.debug("No changes detected for product $wixPlanId")
                }
            } else {
                logger.debug("Product with Wix ID $wixPlanId does not exist locally, skipping")
            }
        } catch (e: Exception) {
            logger.error("Failed to check for updates for Wix plan $wixPlanId: ${e.message}", e)
        }
    }

    private fun applyUpdatesForPlanWithoutSubscription(wixPlan: WixPlan) {
        val wixPlanId = wixPlan.id

        try {
            if (wixPlanId.isNullOrBlank()) {
                logger.warn("Skipping Wix plan without ID: ${wixPlan.name}")
                return
            }

            val existingProduct = findProductByWixId(wixPlanId)

            if (existingProduct != null) {
                logger.debug("Applying updates for product with Wix ID $wixPlanId")
                val wixPlatform = existingProduct.linkedPlatforms?.find { it.platformName == "wix" }
                val remoteHash = computeWixPlanHash(wixPlan)

                // Apply if there are incoming changes marked OR if hash differs
                if (wixPlatform?.hasIncomingChanges == true || remoteHash != wixPlatform?.remoteHash) {
                    logger.info("Applying changes from Wix for product $wixPlanId (hasIncomingChanges: ${wixPlatform?.hasIncomingChanges})")
                    val command = mapWixPlanToUpdateCommand(wixPlan, existingProduct, remoteHash)
                    commandGateway.sendAndWait<Unit>(command)
                    logger.info("Updated product from Wix plan: ${wixPlan.name} (${wixPlan.id})")
                } else {
                    logger.debug("No changes to apply for product $wixPlanId")
                }
            } else {
                logger.debug("Product with Wix ID $wixPlanId does not exist yet, creating")
                val command = mapWixPlanToCommand(wixPlan)
                commandGateway.sendAndWait<Unit>(command)
                logger.info("Created product from Wix plan: ${wixPlan.name} (${wixPlan.id})")
            }
        } catch (e: Exception) {
            logger.error("Failed to apply updates for Wix plan $wixPlanId: ${e.message}", e)
        }
    }

    private fun syncWixPlan(wixPlan: WixPlan) {
        val wixPlanId = wixPlan.id

        try {
            if (wixPlanId.isNullOrBlank()) {
                logger.warn("Skipping Wix plan without ID: ${wixPlan.name}")
                return
            }

            val existingProduct = findProductByWixId(wixPlanId)

            if (existingProduct != null) {
                logger.debug("Product with Wix ID $wixPlanId already exists, checking for changes")
                val remoteHash = computeWixPlanHash(wixPlan)
                val wixPlatform = existingProduct.linkedPlatforms?.find { it.platformName == "wix" }
                val storedRemoteHash = wixPlatform?.remoteHash

                if (remoteHash != storedRemoteHash) {
                    logger.info("Detected changes from Wix for product $wixPlanId - marking as having incoming changes")
                    // Just mark that there are incoming changes, don't apply them automatically
                    markProductWithIncomingChanges(wixPlan, existingProduct, remoteHash)
                } else {
                    logger.debug("No changes detected for product $wixPlanId")
                }
            } else {
                logger.debug("Product with Wix ID $wixPlanId does not exist yet, creating")
                createProductFromWixPlan(wixPlan)
            }
        } catch (e: Exception) {
            logger.error("Failed to sync Wix plan $wixPlanId: ${e.message}", e)
        }
    }

    private fun markProductWithIncomingChanges(
        wixPlan: WixPlan,
        existingProduct: ProductVariantEntity,
        remoteHash: String,
    ) {
        val subscriptionQuery =
            queryGateway.subscriptionQuery(
                FindAllProductsQuery(),
                ResponseTypes.multipleInstancesOf(ProductView::class.java),
                ResponseTypes.instanceOf(ProductUpdatedUpdate::class.java),
            )

        try {
            markProductWithIncomingChangesWithoutSubscription(wixPlan, existingProduct, remoteHash)
            waitForUpdateOf(subscriptionQuery)
        } finally {
            subscriptionQuery.close()
        }
    }

    private fun markProductWithIncomingChangesWithoutSubscription(
        wixPlan: WixPlan,
        existingProduct: ProductVariantEntity,
        remoteHash: String,
    ) {
        val wixPlatform = existingProduct.linkedPlatforms?.find { it.platformName == "wix" }

        val linkedPlatforms =
            (existingProduct.linkedPlatforms?.toMutableList() ?: mutableListOf()).apply {
                removeIf { it.platformName == "wix" }
                add(
                    wixPlatform?.copy(
                        hasIncomingChanges = true,
                        remoteHash = remoteHash,
                        lastSyncedAt = Instant.now(),
                    ) ?: LinkedPlatformSync(
                        platformName = "wix",
                        idOnPlatform = wixPlan.id,
                        revision = wixPlan.revision,
                        visibilityOnPlatform = mapWixVisibility(wixPlan.visibility),
                        isSynced = true,
                        isSourceOfTruth = false,
                        lastSyncedAt = Instant.now(),
                        syncError = null,
                        hasLocalChanges = wixPlatform?.hasLocalChanges ?: false,
                        hasIncomingChanges = true,
                        localHash = wixPlatform?.localHash,
                        remoteHash = remoteHash,
                    ),
                )
            }

        val updateCommand =
            AddLinkedPlatformCommand(
                productId = ProductVariantId(existingProduct.productId),
                linkedPlatforms = linkedPlatforms,
            )

        commandGateway.sendAndWait<Unit>(updateCommand)
        logger.info("Marked product ${existingProduct.productId} as having incoming changes from Wix")
    }

    fun findProductByWixId(wixId: String): ProductVariantEntity? =
        productRepository
            .findAll()
            .firstOrNull { product -> isWixProductWithWixId(product, wixId) }

    private fun isWixProductWithWixId(
        product: ProductVariantEntity?,
        id: String,
    ): Boolean =
        product?.linkedPlatforms?.any {
            it.platformName == "wix" && it.idOnPlatform == id
        } == true

    private fun updateProductFromWixPlan(
        wixPlan: WixPlan,
        existingProduct: ProductVariantEntity,
        remoteHash: String? = null,
    ) {
        val subscriptionQuery =
            queryGateway.subscriptionQuery(
                FindAllProductsQuery(),
                ResponseTypes.multipleInstancesOf(ProductView::class.java),
                ResponseTypes.instanceOf(ProductUpdatedUpdate::class.java),
            )

        try {
            val command = mapWixPlanToUpdateCommand(wixPlan, existingProduct)

            commandGateway.sendAndWait<Unit>(command)

            waitForUpdateOf(subscriptionQuery)

            logger.info("Updated product from Wix plan: ${wixPlan.name} (${wixPlan.id})")
        } finally {
            subscriptionQuery.close()
        }
    }

    private fun mapWixPlanToUpdateCommand(
        wixPlan: WixPlan,
        existingProduct: ProductVariantEntity,
        remoteHash: String? = null,
    ): UpdateProductCommand {
        val linkedPlatforms =
            (existingProduct.linkedPlatforms?.toMutableList() ?: mutableListOf()).apply {
                // Update or add the Wix platform sync info
                removeIf { it.platformName == "wix" }
                add(
                    LinkedPlatformSync(
                        platformName = "wix",
                        idOnPlatform = wixPlan.id,
                        revision = wixPlan.revision,
                        visibilityOnPlatform = mapWixVisibility(wixPlan.visibility),
                        isSynced = true,
                        isSourceOfTruth = false,
                        lastSyncedAt = Instant.now(),
                        syncError = null,
                        hasLocalChanges = false, // Reset local changes when downloading from Wix
                        hasIncomingChanges = false, // Reset incoming changes when downloading from Wix
                        localHash = null,
                        remoteHash = remoteHash ?: computeWixPlanHash(wixPlan),
                    ),
                )
            }

        val pricingVariant = wixPlan.pricingVariants.firstOrNull()

        return UpdateProductCommand(
            productId = ProductVariantId(existingProduct.productId),
            slug = wixPlan.slug ?: existingProduct.slug,
            name = wixPlan.name ?: existingProduct.name,
            productType = existingProduct.productType,
            audience = existingProduct.audience,
            requiresMembership = existingProduct.requiresMembership,
            pricingVariant = mapWixPricingVariant(pricingVariant),
            behavior =
                ProductBehaviorConfig(
                    canBePaused = existingProduct.canBePaused,
                    renewalLeadTimeDays = existingProduct.renewalLeadTimeDays,
                    maxActivePerCustomer = existingProduct.maxActivePerCustomer,
                    maxPurchasesPerBuyer = wixPlan.maxPurchasesPerBuyer ?: existingProduct.maxPurchasesPerBuyer,
                    numberOfSessions = existingProduct.numberOfSessions,
                ),
            description = wixPlan.description ?: existingProduct.description,
            termsAndConditions = existingProduct.termsAndConditions, // Not available in Wix v3
            visibility = mapWixVisibilityToProductVisibility(wixPlan.visibility),
            buyable = wixPlan.buyable ?: existingProduct.buyable,
            buyerCanCancel = wixPlan.buyerCanCancel ?: existingProduct.buyerCanCancel,
            perks =
                wixPlan.perks
                    .mapNotNull { it.description }
                    .takeIf { it.isNotEmpty() } ?: existingProduct.perks,
            linkedPlatforms = linkedPlatforms,
        )
    }

    private fun createProductFromWixPlan(wixPlan: WixPlan) {
        val subscriptionQuery =
            queryGateway.subscriptionQuery(
                FindAllProductsQuery(),
                ResponseTypes.multipleInstancesOf(ProductView::class.java),
                ResponseTypes.instanceOf(ProductUpdatedUpdate::class.java),
            )

        try {
            val command = mapWixPlanToCommand(wixPlan)

            commandGateway.sendAndWait<Unit>(command)

            waitForUpdateOf(subscriptionQuery)

            logger.info("Created product from Wix plan: ${wixPlan.name} (${wixPlan.id})")
        } finally {
            subscriptionQuery.close()
        }
    }

    private fun mapWixPlanToCommand(wixPlan: WixPlan): CreateProductCommand {
        val linkedPlatformSync =
            LinkedPlatformSync(
                platformName = "wix",
                idOnPlatform = wixPlan.id,
                revision = wixPlan.revision,
                visibilityOnPlatform = mapWixVisibility(wixPlan.visibility),
                isSynced = true,
                isSourceOfTruth = false,
                lastSyncedAt = Instant.now(),
                syncError = null,
                hasLocalChanges = false,
                hasIncomingChanges = false,
                localHash = null,
                remoteHash = computeWixPlanHash(wixPlan),
            )

        val pricingVariant = wixPlan.pricingVariants.firstOrNull()

        return CreateProductCommand(
            productId = ProductVariantId(UUID.randomUUID()),
            slug = wixPlan.slug ?: generateSlug(wixPlan.name ?: "unnamed-plan"),
            name = wixPlan.name.toString(),
            productType = "Wix Plan",
            audience = ProductAudience.BOTH,
            requiresMembership = false,
            pricingVariant = mapWixPricingVariant(pricingVariant),
            behavior =
                ProductBehaviorConfig(
                    canBePaused = false,
                    renewalLeadTimeDays = null,
                    maxActivePerCustomer = null,
                    maxPurchasesPerBuyer = wixPlan.maxPurchasesPerBuyer,
                    numberOfSessions = null,
                ),
            description = wixPlan.description,
            termsAndConditions = null, // Not available in v3
            visibility = mapWixVisibilityToProductVisibility(wixPlan.visibility),
            buyable = wixPlan.buyable == true,
            buyerCanCancel = wixPlan.buyerCanCancel == true,
            perks =
                wixPlan.perks
                    .mapNotNull { it.description }
                    .takeIf { it.isNotEmpty() },
            linkedPlatforms = listOf(linkedPlatformSync),
        )
    }

    private fun mapWixVisibility(visibility: String?): PlatformVisibility? =
        when (visibility?.uppercase()) {
            "PUBLIC" -> PlatformVisibility.PUBLISHED
            "HIDDEN" -> PlatformVisibility.HIDDEN
            "ARCHIVED" -> PlatformVisibility.ARCHIVED
            else -> PlatformVisibility.NOT_PUBLISHED
        }

    private fun mapWixVisibilityToProductVisibility(visibility: String?): ProductVisibility =
        when (visibility?.uppercase()) {
            "PUBLIC" -> ProductVisibility.PUBLIC
            "HIDDEN" -> ProductVisibility.HIDDEN
            "ARCHIVED" -> ProductVisibility.ARCHIVED
            else -> ProductVisibility.HIDDEN
        }

    private fun mapWixPricingVariant(pricingVariant: WixPricingVariantV3?): PricingVariantConfig {
        if (pricingVariant == null) {
            return createDefaultPricing()
        }

        return PricingVariantConfig(
            pricingModel = pricingModelFrom(pricingVariant.billingTerms),
            flatRate = flatRateFrom(pricingVariant),
            billingCycle = billingCycleFrom(pricingVariant),
            duration = durationFrom(pricingVariant),
            freeTrial = freeTrialFrom(pricingVariant),
        )
    }

    private fun freeTrialFrom(pricingVariant: WixPricingVariantV3): PricingDuration? =
        when {
            hasFreeTrialPeriod(pricingVariant) ->
                PricingDuration(
                    interval = BillingInterval.DAY,
                    count = pricingVariant.freeTrialDays!!,
                )

            else -> null
        }

    private fun hasFreeTrialPeriod(pricingVariant: WixPricingVariantV3): Boolean = (pricingVariant.freeTrialDays ?: 0) > 0

    private fun durationFrom(pricingVariant: WixPricingVariantV3): PricingDuration? =
        when {
            isCyclic(pricingVariant) ->
                PricingDuration(
                    interval = mapWixPeriodToInterval(pricingVariant.billingTerms?.billingCycle?.period),
                    count = countFrom(pricingVariant.billingTerms),
                )

            else -> null
        }

    private fun isCyclic(pricingVariant: WixPricingVariantV3): Boolean = pricingVariant.billingTerms?.endType == "CYCLES_COMPLETED"

    private fun countFrom(billingTerms: WixBillingTerms?): Int = (billingTerms?.billingCycle?.count ?: 1) * cycleCountFrom(billingTerms)

    private fun cycleCountFrom(billingTerms: WixBillingTerms?): Int = billingTerms?.cyclesCompletedDetails?.billingCycleCount ?: 1

    private fun billingCycleFrom(pricingVariant: WixPricingVariantV3): PricingDuration? =
        pricingVariant.billingTerms?.billingCycle?.let { cycle ->
            PricingDuration(
                interval = mapWixPeriodToInterval(cycle.period),
                count = cycle.count ?: 1,
            )
        }

    private fun flatRateFrom(pricingVariant: WixPricingVariantV3): BigDecimal =
        pricingVariant.pricingStrategies
            .firstOrNull()
            ?.flatRate
            ?.amount
            ?.toBigDecimalOrNull() ?: BigDecimal.ZERO

    private fun pricingModelFrom(billingTerms: WixBillingTerms?): PricingModel =
        when (billingTerms?.endType?.uppercase()) {
            "UNTIL_CANCELLED" -> PricingModel.SUBSCRIPTION
            "CYCLES_COMPLETED" -> PricingModel.SINGLE_PAYMENT_FOR_DURATION
            else -> PricingModel.SUBSCRIPTION
        }

    private fun mapWixPeriodToInterval(period: String?): BillingInterval =
        when (period?.uppercase()) {
            "DAY" -> BillingInterval.DAY
            "WEEK" -> BillingInterval.WEEK
            "MONTH" -> BillingInterval.MONTH
            "YEAR" -> BillingInterval.YEAR
            else -> BillingInterval.MONTH
        }

    private fun createDefaultPricing(): PricingVariantConfig =
        PricingVariantConfig(
            pricingModel = PricingModel.SUBSCRIPTION,
            flatRate = BigDecimal.ZERO,
            billingCycle = PricingDuration(BillingInterval.MONTH, 1),
            duration = null,
            freeTrial = null,
        )

    private fun generateSlug(name: String): String =
        name
            .lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .replace(Regex("\\s+"), "-")
            .trim('-')

    private fun computeWixPlanHash(wixPlan: WixPlan): String {
        val data =
            buildString {
                append(wixPlan.slug ?: "")
                append(wixPlan.name ?: "")
                append(wixPlan.description ?: "")
                append(wixPlan.visibility ?: "")
                append(wixPlan.buyable ?: false)
                append(wixPlan.buyerCanCancel ?: false)
                append(wixPlan.maxPurchasesPerBuyer ?: "")
                append(wixPlan.perks.mapNotNull { it.description }.joinToString(","))
                val pricingVariant = wixPlan.pricingVariants.firstOrNull()
                if (pricingVariant != null) {
                    append(pricingVariant.billingTerms?.endType ?: "")
                    append(pricingVariant.billingTerms?.billingCycle?.period ?: "")
                    append(pricingVariant.billingTerms?.billingCycle?.count ?: "")
                    append(pricingVariant.freeTrialDays ?: "")
                    append(
                        pricingVariant.pricingStrategies
                            .firstOrNull()
                            ?.flatRate
                            ?.amount ?: "",
                    )
                }
            }
        return MessageDigest
            .getInstance("SHA-256")
            .digest(data.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
