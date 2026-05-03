package br.pucpr.authserver.tasks

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TaskRepository: JpaRepository<Task, Long> {
    fun findAllByProjectId(projectId: Long): List<Task>
    fun findAllByProjectIdAndStatus(projectId: Long, status: TaskStatus): List<Task>
}