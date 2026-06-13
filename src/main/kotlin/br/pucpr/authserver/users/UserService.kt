package br.pucpr.authserver.users

import br.pucpr.authserver.exception.NotFoundException
import br.pucpr.authserver.exceptions.BadRequestException
import br.pucpr.authserver.integration.quotes.QuoteClient
import br.pucpr.authserver.integration.sms.SMSClient
import br.pucpr.authserver.roles.RoleRepository
import br.pucpr.authserver.security.Jwt
import br.pucpr.authserver.users.requests.ConfirmRequest
import br.pucpr.authserver.users.requests.LoginRequest
import br.pucpr.authserver.users.requests.UpdateUserRequest
import br.pucpr.authserver.users.responses.LoginResponse
import br.pucpr.authserver.users.responses.UserResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import kotlin.random.Random

@Service
class UserService(
    val repository: UserRepository,
    val roleRepository: RoleRepository,
    val avatarService: AvatarService,
    val jwt: Jwt,
    val quoteClient: QuoteClient,
    val smsClient: SMSClient,
    val smsVerificationService: SmsVerificationService
) {
    fun insert(user: User): User {
        if (repository.findByEmail(user.email) != null) {
            throw BadRequestException("User already exists")
        }
        if (user.bio.isEmpty()) {
            user.bio = quoteClient.randomQuote()?.text ?: ""
        }
        if (user.phone.length == 14) {
            val code = Random.nextInt(1000, 9999)
            smsClient.send(user, "Hello ${user.name}! Here's your AuthServer code: $code", true)
        }
        return repository.save(user)
    }

    fun findAll(dir: SortDir = SortDir.ASC) = when (dir) {
        SortDir.ASC -> repository.findAll(Sort.by("name").ascending())
        SortDir.DESC -> repository.findAll(Sort.by("name").descending())
    }

    fun findByIdOrNull(id: Long) = repository.findByIdOrNull(id)
    fun findById(id: Long) = repository.findByIdOrNull(id) ?: throw NotFoundException(id)

    fun delete(id: Long) {
        val user = findById(id)
        if (user.isAdmin() && repository.findByRole("ADMIN").size == 1) {
            throw BadRequestException("Cannot delete the last admin")
        }
        repository.delete(user)
        log.info("User $id deleted successfully")
    }

    fun findByRole(role: String) = repository.findByRole(role)

    fun addRole(id: Long, roleName: String): Boolean {
        val upperRole = roleName.uppercase()
        val user = findById(id)
        if (user.roles.any { it.name == upperRole }) return false

        val role = roleRepository.findByName(upperRole) ?: throw BadRequestException("Role $upperRole not found")

        user.roles.add(role)
        repository.save(user)
        log.info("User $id successfully added to role $role")
        return true
    }

    fun update(id: Long, request: UpdateUserRequest): User? {
        val user = findById(id)
        user.name = request.name
        user.email = request.email
        repository.save(user)
        return user
    }

    fun loginByPhone(request: LoginRequest): LoginResponse? {
        val user = repository.findByPhone(request.phone)

        if (user != null && user.isActive && user.uuid == request.uuid) {
            logSignInSuccess(user.id)
            return LoginResponse(
                token = jwt.createToken(user),
                user = toResponse(user)
            )
        }

        val code = smsVerificationService.generateValidationCode(request.phone, request.uuid)

        val tempUser = User(
            phone = request.phone,
            uuid = request.uuid,
            name = "Usuário",
            email = ""
        )

        smsClient.send(
            user = tempUser,
            text = "Seu código de confirmação do AuthServer é: $code",
            important = true
        )

        return null
    }

    fun confirmUser(request: ConfirmRequest): LoginResponse {
        if (!smsVerificationService.isValid(request.phone, request.uuid, request.code)) {
            throw NotFoundException("Código de confirmação inválido, expirado ou não encontrado")
        }

        var user = repository.findByPhone(request.phone)

        if (user == null) {
            user = User(
                phone = request.phone,
                uuid = request.uuid,
                isActive = true,
                email = ""
            )
        } else {
            user.uuid = request.uuid
            user.isActive = true
        }

        val savedUser = repository.save(user)
        logSignInSuccess(savedUser.id)
        return LoginResponse(
            token = jwt.createToken(savedUser),
            user = toResponse(savedUser)
        )
    }

    fun saveAvatar(id: Long, avatar: MultipartFile): String {
        val user = findById(id)
        user.avatar = avatarService.save(user, avatar)
        repository.save(user)
        return avatarService.urlFor(user.avatar)
    }

    fun toResponse(user: User) =
        UserResponse(user, avatarService.urlFor(user.avatar))

    companion object {
        val log: Logger = LoggerFactory.getLogger(UserService::class.java)

        fun logSignInSuccess(id: Long?) {
            if (id.toString().isNotEmpty()) {
                log.info("User $id is logged in")
                return
            }
            log.info("User logged in successfully")
        }
    }
}