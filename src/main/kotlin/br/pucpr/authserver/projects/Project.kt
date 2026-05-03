package br.pucpr.authserver.projects

import br.pucpr.authserver.tasks.Task
import br.pucpr.authserver.users.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "ProjectTable")
class Project (
    @Id @GeneratedValue
    var id: Long? = null,

    var name: String = "",
    var description: String = "",

    @ManyToOne
    @JoinColumn(
        name = "idUser",
        nullable = false
    )
    var owner: User,

    @OneToMany(mappedBy = "project", cascade = [CascadeType.ALL], orphanRemoval = true)
    var tasks: List<Task> = emptyList(),
)