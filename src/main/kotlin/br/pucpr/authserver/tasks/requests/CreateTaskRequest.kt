package br.pucpr.authserver.tasks.requests

import br.pucpr.authserver.projects.Project
import br.pucpr.authserver.tasks.Task
import br.pucpr.authserver.tasks.TaskStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateTaskRequest(
    @NotBlank
    val title: String?,

    @NotNull
    val status: TaskStatus,

    val description: String?,
) {
    fun toTask(project: Project): Task {
        return Task(
            title = title!!,
            description = description ?: "",
            project = project
        )
    }
}
