package com.vajrax.server

import com.vajrax.contract.ApiPaths
import com.vajrax.contract.ErrorCodes
import com.vajrax.contract.Problem
import com.vajrax.contract.auth.AuthResponse
import com.vajrax.contract.auth.ChangePasswordRequest
import com.vajrax.contract.auth.ForgotPasswordRequest
import com.vajrax.contract.auth.LoginRequest
import com.vajrax.contract.auth.LogoutRequest
import com.vajrax.contract.auth.RefreshRequest
import com.vajrax.contract.auth.RegisterRequest
import com.vajrax.contract.auth.ResetPasswordRequest
import com.vajrax.contract.auth.UpdateMeRequest
import com.vajrax.contract.auth.UserDto
import com.vajrax.server.auth.AuditLog
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import java.time.Duration
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthTest {
    private val clock = TestClock()
    private val mailer = RecordingMailer()
    private fun server(extra: Map<String, String> = emptyMap(), block: suspend io.ktor.server.testing.ApplicationTestBuilder.(io.ktor.client.HttpClient) -> Unit) =
        withServer(testConfig(extra), deps = { ServerDeps(it, TestDb.dataSource, clock, mailer) }, block = block)

    @Test
    fun registerSignInAndReadTheProfile() = server { client ->
        val session = client.register(email = "Sam@Example.com ")
        assertEquals("Sam@Example.com", session.user.email)
        assertNotNull(session.refreshToken)
        val me = client.get(ApiPaths.ME) { auth(session) }.body<UserDto>()
        assertEquals(session.user.id, me.id)
        // Email matching ignores case and spaces.
        assertEquals(HttpStatusCode.OK, client.login(email = "sam@example.com").status)
        val renamed = client.patch(ApiPaths.ME) { auth(session); json(UpdateMeRequest("  Sam Lee ")) }.body<UserDto>()
        assertEquals("Sam Lee", renamed.displayName)
    }

    @Test
    fun oneAccountPerEmail() = server { client ->
        client.register()
        val again = client.post(ApiPaths.REGISTER) { json(RegisterRequest("SAM@example.com", GOOD_PASSWORD)) }
        assertEquals(HttpStatusCode.Conflict, again.status)
        assertEquals(ErrorCodes.EMAIL_TAKEN, again.body<Problem>().code)
    }

    @Test
    fun weakPasswordsAndBadEmailsAreExplained() = server { client ->
        val response = client.post(ApiPaths.REGISTER) { json(RegisterRequest("not-an-email", "1234567890")) }
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        val problem = response.body<Problem>()
        assertEquals(ErrorCodes.VALIDATION, problem.code)
        assertNotNull(problem.errors["email"])
        assertNotNull(problem.errors["password"])
    }

    @Test
    fun wrongPasswordAndUnknownEmailLookTheSame() = server { client ->
        client.register()
        val wrong = client.login(password = "wrong password here")
        val unknown = client.login(email = "nobody@example.com")
        assertEquals(HttpStatusCode.Unauthorized, wrong.status)
        assertEquals(HttpStatusCode.Unauthorized, unknown.status)
        assertEquals(wrong.body<Problem>().title, unknown.body<Problem>().title)
    }

    @Test
    fun fiveFailuresLockTheAccountForAWhile() = server { client ->
        client.register()
        repeat(5) { assertEquals(HttpStatusCode.Unauthorized, client.login(password = "wrong password $it").status) }
        val locked = client.login()
        assertEquals(HttpStatusCode.TooManyRequests, locked.status)
        assertEquals(ErrorCodes.LOCKED, locked.body<Problem>().code)
        clock.advance(Duration.ofMinutes(2))
        assertEquals(HttpStatusCode.OK, client.login().status)
    }

    @Test
    fun refreshRotatesAndAReusedTokenEndsTheWholeSignIn() = server { client ->
        val first = client.register()
        val second = client.post(ApiPaths.REFRESH) { json(RefreshRequest(first.refreshToken)) }.body<AuthResponse>()
        assertTrue(second.refreshToken != first.refreshToken)
        clock.advance(Duration.ofMinutes(5))
        // The old token shows up again: treated as stolen, so the newer one stops working too.
        assertEquals(HttpStatusCode.Unauthorized, client.post(ApiPaths.REFRESH) { json(RefreshRequest(first.refreshToken)) }.status)
        assertEquals(HttpStatusCode.Unauthorized, client.post(ApiPaths.REFRESH) { json(RefreshRequest(second.refreshToken)) }.status)
        val actions = TestDb.dataSource.connection.use { AuditLog.actionsFor(it, UUID.fromString(first.user.id)) }
        assertTrue("refresh_reuse" in actions)
    }

    @Test
    fun aDuplicateRefreshRightAfterRotationDoesNotSignOut() = server { client ->
        val first = client.register()
        val second = client.post(ApiPaths.REFRESH) { json(RefreshRequest(first.refreshToken)) }.body<AuthResponse>()
        assertEquals(HttpStatusCode.Unauthorized, client.post(ApiPaths.REFRESH) { json(RefreshRequest(first.refreshToken)) }.status)
        assertEquals(HttpStatusCode.OK, client.post(ApiPaths.REFRESH) { json(RefreshRequest(second.refreshToken)) }.status)
    }

    @Test
    fun signOutEndsTheRefreshToken() = server { client ->
        val session = client.register()
        assertEquals(HttpStatusCode.NoContent, client.post(ApiPaths.LOGOUT) { json(LogoutRequest(session.refreshToken)) }.status)
        assertEquals(HttpStatusCode.Unauthorized, client.post(ApiPaths.REFRESH) { json(RefreshRequest(session.refreshToken)) }.status)
    }

    @Test
    fun signOutEverywhereEndsAccessTokensAtOnce() = server { client ->
        val session = client.register()
        assertEquals(HttpStatusCode.NoContent, client.post(ApiPaths.LOGOUT_ALL) { auth(session) }.status)
        assertEquals(HttpStatusCode.Unauthorized, client.get(ApiPaths.ME) { auth(session) }.status)
    }

    @Test
    fun forgotPasswordNeverRevealsAccountsAndResetWorksOnce() = server { client ->
        client.register()
        assertEquals(HttpStatusCode.Accepted, client.post(ApiPaths.PASSWORD_FORGOT) { json(ForgotPasswordRequest("nobody@example.com")) }.status)
        assertTrue(mailer.sent.isEmpty())

        assertEquals(HttpStatusCode.Accepted, client.post(ApiPaths.PASSWORD_FORGOT) { json(ForgotPasswordRequest("sam@example.com")) }.status)
        // The token sits in the link's fragment, which browsers never send to a server (so no request log has it).
        assertTrue("http://localhost/reset-password#token=" in mailer.sent.single().body)
        val token = Regex("token=([A-Za-z0-9_-]+)").find(mailer.sent.single().body)!!.groupValues[1]
        val newPassword = "quiet morning tea"
        assertEquals(HttpStatusCode.NoContent, client.post(ApiPaths.PASSWORD_RESET) { json(ResetPasswordRequest(token, newPassword)) }.status)

        assertEquals(HttpStatusCode.Unauthorized, client.login().status)
        assertEquals(HttpStatusCode.OK, client.login(password = newPassword).status)
        val again = client.post(ApiPaths.PASSWORD_RESET) { json(ResetPasswordRequest(token, "another long phrase")) }
        assertEquals(ErrorCodes.RESET_INVALID, again.body<Problem>().code)
    }

    @Test
    fun resetLinksExpire() = server { client ->
        client.register()
        client.post(ApiPaths.PASSWORD_FORGOT) { json(ForgotPasswordRequest("sam@example.com")) }
        val token = Regex("token=([A-Za-z0-9_-]+)").find(mailer.sent.single().body)!!.groupValues[1]
        clock.advance(Duration.ofMinutes(31))
        assertEquals(HttpStatusCode.BadRequest, client.post(ApiPaths.PASSWORD_RESET) { json(ResetPasswordRequest(token, "quiet morning tea")) }.status)
    }

    @Test
    fun changingThePasswordNeedsTheCurrentOneAndSignsOutOtherDevices() = server { client ->
        val phone = client.register()
        val laptop = client.login().body<AuthResponse>()
        val wrong = client.post(ApiPaths.PASSWORD_CHANGE) { auth(phone); json(ChangePasswordRequest("nope nope nope", "brand new phrase")) }
        assertEquals(HttpStatusCode.Forbidden, wrong.status)

        val changed = client.post(ApiPaths.PASSWORD_CHANGE) { auth(phone); json(ChangePasswordRequest(GOOD_PASSWORD, "brand new phrase")) }
        assertEquals(HttpStatusCode.OK, changed.status)
        val fresh = changed.body<AuthResponse>()
        assertEquals(HttpStatusCode.OK, client.get(ApiPaths.ME) { auth(fresh) }.status)
        assertEquals(HttpStatusCode.Unauthorized, client.get(ApiPaths.ME) { auth(laptop) }.status)
        assertEquals(HttpStatusCode.Unauthorized, client.post(ApiPaths.REFRESH) { json(RefreshRequest(laptop.refreshToken)) }.status)
    }

    @Test
    fun webClientsGetTheRefreshTokenOnlyAsAnHttpOnlyCookie() = server { client ->
        val response = client.post(ApiPaths.REGISTER) {
            header(ApiPaths.CLIENT_HEADER, ApiPaths.CLIENT_WEB)
            json(RegisterRequest("web@example.com", GOOD_PASSWORD))
        }
        val body = response.body<AuthResponse>()
        assertNull(body.refreshToken)
        val setCookie = response.headers[HttpHeaders.SetCookie].orEmpty()
        assertTrue(setCookie.contains("HttpOnly") && setCookie.contains("SameSite=Strict") && setCookie.contains("Path=/v1/auth"), setCookie)
        val cookie = setCookie.substringBefore(';')

        val refreshed = client.post(ApiPaths.REFRESH) {
            header(ApiPaths.CLIENT_HEADER, ApiPaths.CLIENT_WEB)
            header(HttpHeaders.Cookie, cookie)
            json(RefreshRequest())
        }
        assertEquals(HttpStatusCode.OK, refreshed.status)
        // Without the web header the cookie is ignored (protects against cross-site requests).
        assertEquals(HttpStatusCode.Unauthorized, client.post(ApiPaths.REFRESH) { header(HttpHeaders.Cookie, cookie); json(RefreshRequest()) }.status)
    }

    @Test
    fun tooManySignInAttemptsFromOneAddressAreSlowedDown() = server(mapOf("RATE_LOGIN_PER_MINUTE" to "3")) { client ->
        repeat(3) { client.login(email = "x$it@example.com") }
        val limited = client.login(email = "y@example.com")
        assertEquals(HttpStatusCode.TooManyRequests, limited.status)
        assertEquals(ErrorCodes.RATE_LIMITED, limited.body<Problem>().code)
    }

    @Test
    fun behindALoadBalancerEachClientAddressHasItsOwnLimit() = server(
        mapOf("RATE_LOGIN_PER_MINUTE" to "2", "FORWARDED_SKIP_LAST" to "1")
    ) { client ->
        // "<forged>, <client>, <load balancer>": the limit follows the client, not the balancer or the forged entry.
        suspend fun loginFrom(forwardedFor: String) = client.post(ApiPaths.LOGIN) {
            json(LoginRequest("x@example.com", GOOD_PASSWORD))
            header(HttpHeaders.XForwardedFor, forwardedFor)
        }
        repeat(2) { loginFrom("1.1.1.$it, 10.0.0.1, 34.0.0.9") }
        assertEquals(HttpStatusCode.TooManyRequests, loginFrom("1.1.1.9, 10.0.0.1, 34.0.0.9").status)
        assertEquals(HttpStatusCode.Unauthorized, loginFrom("10.0.0.2, 34.0.0.9").status)
    }
}
