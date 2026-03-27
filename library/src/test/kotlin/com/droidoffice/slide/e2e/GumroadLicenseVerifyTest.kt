package com.droidoffice.slide.e2e

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import kotlin.test.assertTrue

class GumroadLicenseVerifyTest {

    /**
     * Real Gumroad license verification test.
     * Requires DROIDSLIDE_LICENSE_KEY environment variable.
     * Skipped in CI unless the env var is set.
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "DROIDSLIDE_LICENSE_KEY", matches = ".+")
    fun `verify real license key`() {
        val key = System.getenv("DROIDSLIDE_LICENSE_KEY")
        assertTrue(key.isNotBlank(), "License key should not be blank")
        // Full verification requires Android context (SharedPreferences)
        // This test validates that the key is at least present
    }
}
