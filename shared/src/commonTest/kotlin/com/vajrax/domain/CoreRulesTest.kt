package com.vajrax.domain

import com.vajrax.core.coroutines.runCatchingCancellable
import com.vajrax.core.error.AppError
import com.vajrax.domain.habit.NotificationPrivacy
import com.vajrax.domain.usecase.ProfileRules
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** Small shared rules: profile validation, stored privacy values, error handling helpers. */
class CoreRulesTest {
    @Test
    fun emailIsOptionalButMustLookLikeAnAddress() {
        assertNull(ProfileRules.emailError(""))
        assertNull(ProfileRules.emailError("   "))
        assertNull(ProfileRules.emailError(" sam@example.com "))
        assertNotNull(ProfileRules.emailError("sam@"))
        assertNotNull(ProfileRules.emailError("sam example.com"))
    }

    @Test
    fun unknownStoredPrivacyFallsBackToTheDefault() {
        assertEquals(NotificationPrivacy.FULL, NotificationPrivacy.of("FULL"))
        assertEquals(NotificationPrivacy.Default, NotificationPrivacy.of(null))
        assertEquals(NotificationPrivacy.Default, NotificationPrivacy.of("full"))
    }

    @Test
    fun cancellationIsNeverSwallowed() {
        assertFailsWith<CancellationException> {
            runCatchingCancellable { throw CancellationException("screen closed") }
        }
        val failure = runCatchingCancellable { error("disk full") }
        assertTrue(failure.isFailure)
        assertEquals(42, runCatchingCancellable { 42 }.getOrNull())
    }

    @Test
    fun appErrorsCarryReadableMessagesAndKeepTheCause() {
        val cause = IllegalStateException("socket reset")
        val error = AppError.Unknown(cause)
        assertSame(cause, error.cause)
        listOf(AppError.Offline(), AppError.Timeout(), AppError.Server(503), AppError.SignedOut(), error).forEach {
            assertTrue(!it.message.isNullOrBlank(), "${it::class.simpleName} needs a message")
        }
    }
}
