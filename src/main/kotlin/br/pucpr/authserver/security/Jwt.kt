package br.pucpr.authserver.security

import br.pucpr.authserver.users.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.jackson.io.JacksonDeserializer
import io.jsonwebtoken.jackson.io.JacksonSerializer
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Date

@Component
class Jwt {
    fun createToken(user: User): String =
        UserToken(user).let {
            Jwts.builder().json(JacksonSerializer())
                .signWith(Keys.hmacShaKeyFor(SECRET.toByteArray()))
                .issuedAt(utcNow().toDate())
                .expiration(
                    utcNow().plusHours(
                        if (it.isAdmin) ADMIN_EXPIRE_HOURS else EXPIRE_HOURS
                    ).toDate()
                )
                .issuer(ISSUER)
                .subject(it.id.toString())
                .claim(USER_FIELD, it)
                .compact()
        }

    fun extract(req: HttpServletRequest): Authentication? {
        try {
            val header = req.getHeader(HttpHeaders.AUTHORIZATION)
            if (header == null || !header.startsWith("Bearer ")) {
                log.debug("Token not found")
                return null
            }
            val token = header.substring(7).trim()
            val claims = Jwts.parser()
                .json(JacksonDeserializer(
                    mapOf(USER_FIELD to UserToken::class.java)
                ))
                .verifyWith(Keys.hmacShaKeyFor(SECRET.toByteArray()))
                .build()
                .parseSignedClaims(token).payload

            if (claims.issuer != ISSUER) {
                log.trace("Invalid issuer ${claims.issuer}")
                return null
            }
            return claims
                .get(USER_FIELD, UserToken::class.java)
                // toAuthentication defined below in the companion object
                .toAuthentication()
        } catch (e: Throwable) {
            log.debug(e.message)
            return null
        }
    }

    companion object {
        const val SECRET = "eff56a337431c19cafa596c9847859f9525dcd48"
        const val ADMIN_EXPIRE_HOURS = 1L
        const val EXPIRE_HOURS = 48L
        const val ISSUER = "AuthServer"
        const val USER_FIELD = "user"

        val log: Logger = LoggerFactory.getLogger(Jwt::class.java)

        private fun utcNow() = ZonedDateTime.now(ZoneOffset.UTC)
        private fun ZonedDateTime.toDate(): Date = Date.from(this.toInstant())
        private fun UserToken.toAuthentication(): Authentication {
            val authorities = roles.map { SimpleGrantedAuthority("ROLE_$it") }
            return UsernamePasswordAuthenticationToken.authenticated(
                this.id.toString(),
                id,
                authorities
            )
        }
    }
}