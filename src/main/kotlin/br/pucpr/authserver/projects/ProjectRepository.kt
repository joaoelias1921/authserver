package br.pucpr.authserver.projects

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProjectRepository: JpaRepository<Project, Long> {
    fun findByNameAndOwnerId(projectName: String, ownerId: String): List<Project>
    fun findAllByOwnerId(ownerId: Long, sort: Sort): List<Project>
}