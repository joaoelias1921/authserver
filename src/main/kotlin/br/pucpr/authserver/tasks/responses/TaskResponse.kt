package br.pucpr.authserver.tasks.responses

import br.pucpr.authserver.tasks.Task
import br.pucpr.authserver.tasks.TaskStatus

data class TaskResponse(
    val title: String,
    val description: String,
    val status: TaskStatus,
) {
    constructor(task: Task): this(
        task.title,
        task.description,
        task.status,
    )
}