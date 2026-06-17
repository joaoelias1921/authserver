package br.pucpr.authserver.exceptions

import org.springframework.http.HttpStatus.TOO_MANY_REQUESTS
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(TOO_MANY_REQUESTS)
class TooManyRequestsException(
    message: String = "Too Many Requests",
    cause: Throwable? = null
) : IllegalStateException(message, cause)