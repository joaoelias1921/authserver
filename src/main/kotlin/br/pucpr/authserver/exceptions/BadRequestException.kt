package br.pucpr.authserver.exceptions

import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(code = BAD_REQUEST)
class BadRequestException(
    message: String = "Bad Request",
    cause: Throwable? = null
): IllegalArgumentException(message, cause)