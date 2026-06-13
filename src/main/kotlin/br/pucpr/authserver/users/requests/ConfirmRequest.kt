package br.pucpr.authserver.users.requests

import jakarta.validation.constraints.NotBlank

data class ConfirmRequest(
    @NotBlank
    var phone: String,

    @NotBlank
    var uuid: String,

    @NotBlank
    val code: String
)