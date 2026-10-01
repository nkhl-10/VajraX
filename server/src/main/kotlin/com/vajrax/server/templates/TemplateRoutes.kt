package com.vajrax.server.templates

import com.vajrax.contract.ApiPaths
import com.vajrax.contract.ErrorCodes
import com.vajrax.contract.templates.LibraryTemplate
import com.vajrax.server.auth.AUTH_USER
import com.vajrax.server.auth.UserPrincipal
import com.vajrax.server.http.ApiException
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import kotlinx.serialization.Serializable

@Serializable
data class PublishResponse(val version: String)

fun Route.templateRoutes(templates: TemplateService) {
    // Public and cacheable: apps check with If-None-Match (or ?since=<version>) and usually get a 304.
    get(ApiPaths.TEMPLATES) {
        val library = templates.library()
        val etag = "\"lib-${library.version}\""
        call.response.header(HttpHeaders.ETag, etag)
        call.response.header(HttpHeaders.CacheControl, "public, max-age=3600")
        val since = call.request.queryParameters["since"]
        if (call.request.header(HttpHeaders.IfNoneMatch) == etag || since == library.version) {
            call.respond(HttpStatusCode.NotModified)
        } else {
            call.respond(library)
        }
    }

    authenticate(AUTH_USER) {
        put("/v1/admin/templates/{id}") {
            call.requireAdmin()
            val body = call.receive<LibraryTemplate>()
            if (body.id != call.parameters["id"]) {
                throw com.vajrax.server.http.ValidationException(
                    mapOf("id" to "Must match the address.")
                )
            }
            templates.save(body, call.request.queryParameters["order"]?.toIntOrNull())
            call.respond(HttpStatusCode.NoContent)
        }
        delete("/v1/admin/templates/{id}") {
            call.requireAdmin()
            templates.delete(requireNotNull(call.parameters["id"]))
            call.respond(HttpStatusCode.NoContent)
        }
        post("/v1/admin/templates/publish") {
            call.requireAdmin()
            call.respond(PublishResponse(templates.publish()))
        }
    }
}

private fun ApplicationCall.requireAdmin() {
    if (principal<UserPrincipal>()?.isAdmin != true) {
        throw ApiException(
            HttpStatusCode.Forbidden,
            ErrorCodes.FORBIDDEN,
            "Only administrators can change the template library."
        )
    }
}
