package com.vajrax.server.http

import com.vajrax.contract.ErrorCodes
import io.ktor.http.HttpStatusCode

/** A failure with a status, a stable [code] for apps and a sentence a person can act on. */
open class ApiException(
    val status: HttpStatusCode,
    val code: String,
    val title: String,
    val detail: String? = null,
    val errors: Map<String, String> = emptyMap()
) : RuntimeException(title)

class ValidationException(errors: Map<String, String>, title: String = "Please check the highlighted fields.") :
    ApiException(HttpStatusCode.UnprocessableEntity, ErrorCodes.VALIDATION, title, errors = errors)

fun unauthorized(code: String = ErrorCodes.UNAUTHORIZED, title: String = "Please sign in again.") =
    ApiException(HttpStatusCode.Unauthorized, code, title)

fun notFound(title: String = "Not found.") = ApiException(HttpStatusCode.NotFound, ErrorCodes.NOT_FOUND, title)
