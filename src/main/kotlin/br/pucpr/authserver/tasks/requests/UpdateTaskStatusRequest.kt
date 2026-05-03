package br.pucpr.authserver.tasks.requests

import br.pucpr.authserver.tasks.TaskStatus
import jakarta.validation.constraints.NotNull

data class UpdateTaskStatusRequest (
    @NotNull
    val status: TaskStatus
)