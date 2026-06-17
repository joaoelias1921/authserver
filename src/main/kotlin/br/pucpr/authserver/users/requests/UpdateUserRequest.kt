package br.pucpr.authserver.users.requests

import jakarta.validation.constraints.NotBlank

data class UpdateUserRequest(
    @NotBlank
    val name: String?,

    @NotBlank
    val email: String?,

    val bio: String
)