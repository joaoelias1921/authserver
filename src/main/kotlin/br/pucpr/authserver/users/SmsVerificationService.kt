package br.pucpr.authserver.users

import br.pucpr.authserver.exceptions.TooManyRequestsException
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

data class VerificationToken(
    val code: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val expiresAt: LocalDateTime
)

@Service
class SmsVerificationService {
    private val verificationStorage = ConcurrentHashMap<String, VerificationToken>()

    fun generateValidationCode(phone: String, uuid: String): String {
        val storageKey = makeKey(phone, uuid)
        val existingToken = verificationStorage[storageKey]

        if (existingToken != null) {
            val secondsSinceCreation = ChronoUnit.SECONDS.between(
                existingToken.createdAt, LocalDateTime.now()
            )

            if (secondsSinceCreation < SMS_COOLDOWN_IN_SECONDS) {
                val secondsLeft = SMS_COOLDOWN_IN_SECONDS - secondsSinceCreation
                throw TooManyRequestsException("Please wait $secondsLeft seconds before a new verification")
            }
        }

        val code = String.format("%06d", Random.nextInt(999999))

        verificationStorage[storageKey] = VerificationToken(
            code = code,
            createdAt = LocalDateTime.now(),
            expiresAt = LocalDateTime.now().plusMinutes(5)
        )
        return code
    }

    fun isValid(phone: String, uuid: String, code: String): Boolean {
        val key = makeKey(phone, uuid)
        val storedKey = verificationStorage[key] ?: return false

        if (storedKey.expiresAt.isBefore(LocalDateTime.now())) {
            verificationStorage.remove(key)
            return false
        }

        val matches = storedKey.code == code
        if (matches) verificationStorage.remove(key)
        return matches
    }

    private fun makeKey(phone: String, uuid: String): String = "$phone|$uuid"

    companion object {
        const val SMS_COOLDOWN_IN_SECONDS = 120
    }
}