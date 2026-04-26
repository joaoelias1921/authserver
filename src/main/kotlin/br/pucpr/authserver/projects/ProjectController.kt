package br.pucpr.authserver.projects

import br.pucpr.authserver.projects.requests.CreateProjectRequest
import br.pucpr.authserver.projects.responses.ProjectResponse
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/projects")
class ProjectController(val projectService: ProjectService) {
    @GetMapping
    fun list(): ResponseEntity<List<ProjectResponse>> {
        val projects = projectService.findAll()
        return projects
            .map { ProjectResponse(it) }
            .let { ResponseEntity.ok(it) }
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long) =
        projectService.findById(id)
            .let { ProjectResponse(it) }
            .let { ResponseEntity.ok(it) }

    @SecurityRequirement(name = "jwt-auth")
    @PostMapping
    @ApiResponse(responseCode = "201")
    fun insert(
        @RequestBody @Valid projectRequest: CreateProjectRequest
    ) {
        projectService.insert(projectRequest)
            .let { ProjectResponse(it) }
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }
    }

    @SecurityRequirement(name = "jwt-auth")
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody @Valid projectRequest: CreateProjectRequest
    ) = projectService.update(id, projectRequest)
        .let { ProjectResponse(it) }
        .let { ResponseEntity.ok(it) }

    @SecurityRequirement(name = "jwt-auth")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = projectService.delete(id)
}