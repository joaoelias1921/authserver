package br.pucpr.authserver.tasks

import br.pucpr.authserver.projects.Project
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "TaskTable")
class Task (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var title: String = "",
    var description: String = "",

    @Enumerated(EnumType.STRING)
    var status: TaskStatus = TaskStatus.TODO,

    @ManyToOne
    @JoinColumn(
        name = "idProject",
        nullable = false
    )
    val project: Project
)