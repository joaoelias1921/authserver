package br.pucpr.authserver.tasks

import br.pucpr.authserver.tasks.requests.UpdateTaskRequest
import br.pucpr.authserver.tasks.requests.UpdateTaskStatusRequest
import br.pucpr.authserver.tasks.responses.TaskResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/tasks")
class TaskController(private val taskService: TaskService) {
    @SecurityRequirement(name = "jwt-auth")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long) =
        taskService.findById(id)
            .let { TaskResponse(it) }
            .let { ResponseEntity.ok(it) }

    @SecurityRequirement(name = "jwt-auth")
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody @Valid taskRequest: UpdateTaskRequest
    ) = taskService.update(id, taskRequest)
        .let { TaskResponse(it) }
        .let { ResponseEntity.status(HttpStatus.OK).body(it) }

    @SecurityRequirement(name = "jwt-auth")
    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestBody @Valid taskRequest: UpdateTaskStatusRequest)
    = taskService.updateStatus(id, taskRequest.status)
        .let { TaskResponse(it) }
        .let { ResponseEntity.status(HttpStatus.OK).body(it) }

    @SecurityRequirement(name = "jwt-auth")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = taskService.delete(id)
}