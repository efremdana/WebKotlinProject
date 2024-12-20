package ru.ac.uniyar.web.session

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.Verification
import java.time.LocalDateTime
import java.time.ZoneOffset

private const val TIMEZONE = 3
private const val HOURS = 24
private const val MINUTES = 60
private const val SECONDS = 60

class JwtTools(secret: String, private val nameOrganization: String, private val tokenLifespan: Int = 7) {
    private val algorithm = Algorithm.HMAC512(secret)
    private val verifier = createVerification().build()

    fun createJWT(userName: String): String {
        val builderJWT: JWTCreator.Builder = JWT.create()
        builderJWT.withSubject(userName)
        val today = LocalDateTime.now()
        val term = today.plusDays(tokenLifespan.toLong())
        builderJWT.withExpiresAt(term.toInstant(ZoneOffset.ofHours(TIMEZONE)))
        builderJWT.withIssuer(nameOrganization)
        return builderJWT.sign(algorithm)
    }

    fun verificationJWT(tokenForVerified: String): String? {
        return try {
            val tokenVerified = verifier.verify(tokenForVerified)
            tokenVerified.subject
        } catch (_: JWTVerificationException) {
            null
        }
    }

    private fun createVerification(): Verification {
        val verification = JWT.require(algorithm)
        val leeway = tokenLifespan * HOURS * MINUTES * SECONDS
        verification.acceptExpiresAt(leeway.toLong())
        verification.withIssuer(nameOrganization)
        return verification
    }
}
