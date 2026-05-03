package br.pucpr.authserver.tasks.requests

import br.pucpr.authserver.tasks.TaskStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class UpdateTaskRequest(
    @NotBlank
    val title: String?,

    @NotNull
    val status: TaskStatus,

    val description: String?,
)