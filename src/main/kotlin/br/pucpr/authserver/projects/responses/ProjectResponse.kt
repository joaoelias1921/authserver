package br.pucpr.authserver.projects.responses

import br.pucpr.authserver.projects.Project
import br.pucpr.authserver.tasks.responses.TaskResponse

data class ProjectResponse(
    val id: Long,
    val name: String,
    val description: String,
    val owner: ProjectOwnerResponse,
    val tasks: List<TaskResponse>,
) {
    constructor(project: Project): this(
        project.id!!,
        project.name,
        project.description,
        ProjectOwnerResponse(project.owner),
        project.tasks.map { TaskResponse(it) }
    )
}