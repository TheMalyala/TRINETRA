package org.trinetra.android.core.security

import org.junit.Assert.assertEquals
import org.junit.Test

class CryptoHelperTest {

    @Test
    fun sha256_emptyString_matchesNistVector() {
        val emptyHash = CryptoHelper.sha256("")
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", emptyHash)
    }

    @Test
    fun sha256_quickBrownFox_matchesStandardVector() {
        val hash = CryptoHelper.sha256("The quick brown fox jumps over the lazy dog")
        assertEquals("d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592", hash)
    }

    @Test
    fun sha256_isDeterministic() {
        val sample = "Trinetra Medical Records Privacy Vault"
        val hash1 = CryptoHelper.sha256(sample)
        val hash2 = CryptoHelper.sha256(sample)
        assertEquals(hash1, hash2)
    }
}
