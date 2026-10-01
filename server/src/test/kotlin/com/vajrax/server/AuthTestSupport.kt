package com.vajrax.server

import com.vajrax.contract.ApiPaths
import com.vajrax.contract.auth.AuthResponse
import com.vajrax.contract.auth.LoginRequest
import com.vajrax.contract.auth.RegisterRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.assertEquals

const val GOOD_PASSWORD = "river stone lamp"

fun HttpRequestBuilder.json(body: Any) {
    contentType(ContentType.Application.Json)
    setBody(body)
}

suspend fun HttpClient.register(email: String = "sam@example.com", password: String = GOOD_PASSWORD, name: String = "Sam"): AuthResponse {
    val response = post(ApiPaths.REGISTER) { json(RegisterRequest(email, password, name)) }
    assertEquals(HttpStatusCode.Created, response.status, "register failed")
    return response.body()
}

suspend fun HttpClient.login(email: String = "sam@example.com", password: String = GOOD_PASSWORD): HttpResponse =
    post(ApiPaths.LOGIN) { json(LoginRequest(email, password)) }

fun HttpRequestBuilder.auth(session: AuthResponse) = bearerAuth(session.accessToken)
