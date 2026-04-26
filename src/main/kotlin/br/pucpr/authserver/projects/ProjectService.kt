package br.pucpr.authserver.projects

import br.pucpr.authserver.exceptions.BadRequestException
import br.pucpr.authserver.exceptions.ForbiddenException
import br.pucpr.authserver.exceptions.NotFoundException
import br.pucpr.authserver.projects.requests.CreateProjectRequest
import br.pucpr.authserver.users.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class ProjectService(
    val repository: ProjectRepository,
    val userRepository: UserRepository
) {
    val defaultForbiddenException = ForbiddenException("User not allowed to perform this operation")
    val userNotFoundException = NotFoundException("User not found")

    fun findAll() = repository.findAll()

    fun findById(id: Long) = repository.findByIdOrNull(id) ?: throw NotFoundException("Project $id not found")

    fun insert(projectRequest: CreateProjectRequest): Project {
        val userId = SecurityContextHolder.getContext().authentication?.name
            ?: throw defaultForbiddenException
        val owner = userRepository.findById(userId.toLong()).orElseThrow { userNotFoundException }
        val project = projectRequest.toProject(owner)

        if (repository.findByNameAndOwnerId(project.name, userId).isNotEmpty()) {
            throw BadRequestException("Project with name ${project.name} already exists")
        }
        return repository.save(project)
            .also { log.info("Project with id ${project.id} created successfully") }
    }

    fun update(id: Long, projectRequest: CreateProjectRequest): Project {
        val userId = SecurityContextHolder.getContext().authentication?.name
            ?: throw defaultForbiddenException
        val project = repository.findById(id)
            .orElseThrow { NotFoundException("Project with id $id not found") }

        if (project.owner.id.toString() != userId) throw defaultForbiddenException
        project.name = projectRequest.name!!
        project.description = projectRequest.description!!
        return repository.save(project)
            .also { log.info("Project with id ${project.id} updated successfully") }
    }

    fun delete(id: Long) {
        val userId = SecurityContextHolder.getContext().authentication?.name
            ?: throw defaultForbiddenException
        val user = userRepository.findById(userId.toLong()).orElseThrow { userNotFoundException }
        val project = repository.findById(id)
            .orElseThrow { NotFoundException("Project with id $id not found") }
        if (!user.isAdmin() && project.owner.id.toString() != userId) {
            throw defaultForbiddenException
        }
        repository.deleteById(id)
            .also { log.warn("Project with id {} deleted successfully", id) }
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(ProjectService::class.java)
    }
}