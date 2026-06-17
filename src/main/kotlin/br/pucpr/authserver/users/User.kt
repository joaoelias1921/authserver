package br.pucpr.authserver.users

import br.pucpr.authserver.roles.Role
import jakarta.persistence.*

@Entity
@Table(name = "UserTable")
class User (
    @Id @GeneratedValue
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    val phone: String,

    var uuid: String,

    var password: String? = null,
    var name: String? = null,
    var email: String? = null,

    var isActive: Boolean = false,

    @Column(nullable = false)
    var bio: String = "",

    @ManyToMany
    @JoinTable(
        name = "UserRole",
        joinColumns = [JoinColumn(name = "idUser")],
        inverseJoinColumns = [JoinColumn(name = "idRole")]
    )
    var roles: MutableSet<Role> = mutableSetOf(),

    var avatar: String = AvatarService.DEFAULT_AVATAR,
) {
    @Transient
    fun isAdmin() = roles.any { it.name == "ADMIN" }
}