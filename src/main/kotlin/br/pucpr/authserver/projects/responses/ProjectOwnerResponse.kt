package br.pucpr.authserver.projects.responses

import br.pucpr.authserver.users.User

data class ProjectOwnerResponse(
    val name: String,
    val email: String
) {
    constructor(user: User): this(
        user.name,
        user.email
    )
}