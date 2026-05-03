package br.pucpr.authserver.tasks

import br.pucpr.authserver.exceptions.ForbiddenException
import br.pucpr.authserver.exceptions.NotFoundException
import br.pucpr.authserver.tasks.requests.UpdateTaskRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class TaskService(private val repository: TaskRepository) {
    fun findById(id: Long): Task {
        val userId = getUserId().toLong()
        val task = repository.findByIdOrNull(id)
        if (task == null || task.project.owner.id != userId) {
            throw NotFoundException("Task with id $id not found")
        }
        return task
    }

    fun update(id: Long, taskRequest: UpdateTaskRequest): Task {
        val userId = getUserId().toLong()
        val taskToUpdate = repository.findByIdOrNull(id)
        if (taskToUpdate == null || taskToUpdate.project.owner.id != userId) {
            throw NotFoundException("Task with id $id not found")
        }
        taskToUpdate.title = taskRequest.title!!
        taskToUpdate.description = taskRequest.description!!
        taskToUpdate.status = taskRequest.status
        return repository.save(taskToUpdate)
            .also { log.info("Task with id $id updated successfully") }
    }

    fun updateStatus(id: Long, newStatus: TaskStatus): Task {
        val userId = getUserId().toLong()
        val task = repository.findByIdOrNull(id)
        if (task == null || task.project.owner.id != userId) {
            throw NotFoundException("Task with id $id not found")
        }
        task.status = newStatus
        return repository.save(task)
            .also { log.info("Task $id status updated successfully") }
    }

    fun delete(id: Long) {
        val userId = getUserId().toLong()
        val task = repository.findByIdOrNull(id)
        if (task == null || task.project.owner.id != userId) {
            throw NotFoundException("Task with id $id not found")
        }
        repository.delete(task)
            .also { log.warn("Task with id {} deleted successfully", id) }
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(TaskService::class.java)

        fun getUserId() = SecurityContextHolder.getContext().authentication?.name
            ?: throw ForbiddenException("User not allowed to perform this operation")
    }
}