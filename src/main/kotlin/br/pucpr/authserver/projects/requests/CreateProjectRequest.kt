package br.pucpr.authserver.projects.requests

import br.pucpr.authserver.projects.Project
import br.pucpr.authserver.users.User
import jakarta.validation.constraints.NotBlank

data class CreateProjectRequest(
    @NotBlank
    val name: String?,

    @NotBlank
    val description: String?,
) {
    fun toProject(owner: User) = Project(
        name = name!!,
        description = description!!,
        owner = owner,
    )
}
