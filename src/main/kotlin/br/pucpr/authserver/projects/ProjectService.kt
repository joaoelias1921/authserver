package br.pucpr.authserver.projects

import br.pucpr.authserver.exceptions.BadRequestException
import br.pucpr.authserver.exceptions.ForbiddenException
import br.pucpr.authserver.exceptions.NotFoundException
import br.pucpr.authserver.projects.requests.CreateProjectRequest
import br.pucpr.authserver.tasks.Task
import br.pucpr.authserver.tasks.TaskRepository
import br.pucpr.authserver.tasks.TaskStatus
import br.pucpr.authserver.tasks.requests.CreateTaskRequest
import br.pucpr.authserver.users.SortDir
import br.pucpr.authserver.users.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class ProjectService(
    val repository: ProjectRepository,
    val userRepository: UserRepository,
    val taskRepository: TaskRepository
) {
    val userNotFoundException = NotFoundException("User not found")

    fun findAll(direction: SortDir): List<Project> {
        val sort = Sort.by(Sort.Direction.fromString(
            direction.toString().uppercase()),
            "name"
        )
        return repository.findAllByOwnerId(getUserId().toLong(), sort)
    }

    fun findById(id: Long): Project {
        val project = repository.findByIdOrNull(id)
        if (project == null || project.owner.id.toString() != getUserId()) {
            throw NotFoundException("Project with id $id not found")
        }
        return project
    }

    fun findAllTasks(id: Long, status: TaskStatus?): List<Task> {
        val project = repository.findByIdOrNull(id)
        if (project == null || project.owner.id.toString() != getUserId()) {
            throw NotFoundException("Project with id $id not found")
        }
        if (status != null) {
            log.info("Searching for tasks with status $status")
            return taskRepository.findAllByProjectIdAndStatus(id, status)
        }
        return taskRepository.findAllByProjectId(id)
    }

    fun insert(projectRequest: CreateProjectRequest): Project {
        val userId = getUserId()
        val owner = userRepository.findById(userId.toLong()).orElseThrow { userNotFoundException }
        val project = projectRequest.toProject(owner)

        if (repository.findByNameAndOwnerId(project.name, userId).isNotEmpty()) {
            throw BadRequestException("Project with name ${project.name} already exists")
        }
        return repository.save(project)
            .also { log.info("Project with id ${project.id} created successfully") }
    }

    fun insertTask(id: Long, taskRequest: CreateTaskRequest): Task {
        val project = repository.findByIdOrNull(id)
        if (project == null || project.owner.id.toString() != getUserId()) {
            throw NotFoundException("Project with id $id not found")
        }
        val task = taskRequest.toTask(project)
        return taskRepository.save(task)
            .also { log.info("Task ${task.title} created successfully for project $id") }
    }

    fun update(id: Long, projectRequest: CreateProjectRequest): Project {
        val project = repository.findByIdOrNull(id)
        if (project == null || project.owner.id.toString() != getUserId()) {
            throw NotFoundException("Project with id $id not found")
        }
        project.name = projectRequest.name!!
        project.description = projectRequest.description!!
        return repository.save(project)
            .also { log.info("Project with id ${project.id} updated successfully") }
    }

    fun delete(id: Long) {
        val userId = getUserId()
        val user = userRepository.findById(userId.toLong()).orElseThrow { userNotFoundException }
        val project = repository.findByIdOrNull(id)
        if (project == null || !user.isAdmin() && project.owner.id.toString() != userId) {
            throw NotFoundException("Project with id $id not found")
        }
        repository.deleteById(id)
            .also { log.warn("Project with id {} deleted successfully", id) }
    }

    companion object {
        val defaultForbiddenException = ForbiddenException("User not allowed to perform this operation")
        val log: Logger = LoggerFactory.getLogger(ProjectService::class.java)

        fun getUserId() = SecurityContextHolder.getContext().authentication?.name
            ?: throw defaultForbiddenException
    }
}