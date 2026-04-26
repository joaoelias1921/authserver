package br.pucpr.authserver.projects.responses

import br.pucpr.authserver.projects.Project
import br.pucpr.authserver.tasks.Task

data class ProjectResponse(
    val name: String,
    val description: String,
    val owner: ProjectOwnerResponse,
    val tasks: MutableSet<Task>,
) {
    constructor(project: Project): this(
        project.name,
        project.description,
        ProjectOwnerResponse(project.owner),
        project.tasks
    )
}