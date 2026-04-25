package br.pucpr.authserver.exceptions

import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(code = UNAUTHORIZED)
class UnauthorizedException(
    message: String = "Unauthorized",
    cause: Throwable? = null
): IllegalStateException(message, cause)
