package br.pucpr.authserver.roles

import org.springframework.stereotype.Service

@Service
class RoleService(private val repository: RoleRepository) {
    fun insert(role: Role): Role? {
        if (repository.findByName(role.name) != null) {
            return null
        }
        return repository.save(role)
    }

    fun findAll(): List<Role> = repository.findAll()
}