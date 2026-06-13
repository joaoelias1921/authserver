package br.pucpr.authserver.users

import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

data class VerificationToken(
    val code: String,
    val expiresAt: LocalDateTime
)

@Service
class SmsVerificationService {
    private val verificationStorage = ConcurrentHashMap<String, VerificationToken>()

    fun generateValidationCode(phone: String, uuid: String): String {
        val code = String.format("%06d", Random.nextInt(999999))
        val storageKey = makeKey(phone, uuid)

        verificationStorage[storageKey] = VerificationToken(
            code = code,
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
}