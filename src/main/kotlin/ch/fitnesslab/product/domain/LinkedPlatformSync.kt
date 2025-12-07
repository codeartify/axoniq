package ch.fitnesslab.product.domain

import java.time.Instant

data class LinkedPlatformSync(
    val platformName: String,
    val idOnPlatform: String?,
    val revision: String?,
    val visibilityOnPlatform: PlatformVisibility?,
    val isSynced: Boolean,
    val isSourceOfTruth: Boolean = false,
    val lastSyncedAt: Instant?,
    val syncError: String?
)

enum class PlatformVisibility {
    DRAFT,
    PUBLISHED,
    ARCHIVED,
    HIDDEN,
    NOT_PUBLISHED
}
