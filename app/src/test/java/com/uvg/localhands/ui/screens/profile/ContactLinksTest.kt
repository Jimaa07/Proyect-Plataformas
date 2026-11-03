package com.uvg.localhands.ui.screens.profile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContactLinksTest {

    @Test
    fun phoneWithCountryCode_keepsOnlyDigits() {
        assertEquals("50255550101", internationalPhoneDigits("+502 5555-0101"))
    }

    @Test
    fun localPhone_getsGuatemalaCode() {
        assertEquals("50255550101", internationalPhoneDigits("5555-0101"))
    }

    @Test
    fun emptyPhone_hasNoLinks() {
        assertNull(dialUri(""))
        assertNull(whatsAppUrl("  "))
    }

    @Test
    fun links_useTheInternationalNumber() {
        assertEquals("tel:+50255550101", dialUri("+502 5555-0101"))
        assertEquals("https://wa.me/50255550101", whatsAppUrl("+502 5555-0101"))
    }
}
