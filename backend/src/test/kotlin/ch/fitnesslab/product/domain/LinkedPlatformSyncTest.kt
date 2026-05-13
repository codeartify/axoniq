package ch.fitnesslab.product.domain

import org.axonframework.conversion.jackson.JacksonConverter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LinkedPlatformSyncTest {
    @Test
    fun `deserializes legacy null boolean sync flags`() {
        val json =
            """
            {
              "platformName": "wix",
              "idOnPlatform": "plan-1",
              "revision": null,
              "visibilityOnPlatform": null,
              "isSynced": null,
              "isSourceOfTruth": null,
              "lastSyncedAt": null,
              "syncError": null,
              "hasLocalChanges": null,
              "hasIncomingChanges": null,
              "localHash": null,
              "remoteHash": null
            }
            """.trimIndent()

        val result = JacksonConverter().convert<LinkedPlatformSync>(json.toByteArray(), LinkedPlatformSync::class.java)

        assertEquals("wix", result?.platformName)
        assertEquals(false, result?.isSynced == true)
        assertEquals(false, result?.isSourceOfTruth == true)
        assertEquals(false, result?.hasLocalChanges == true)
        assertEquals(false, result?.hasIncomingChanges == true)
    }
}
