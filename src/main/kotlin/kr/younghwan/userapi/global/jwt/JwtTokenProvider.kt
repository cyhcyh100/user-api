package kr.younghwan.userapi.global.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}")
    private val secretKey: String,
    @Value("\${jwt.access-token-expiration-hour}")
    private val accessTokenExpirationHour: Long = 1L,
    @Value("\${jwt.refresh-token-expiration-hour}")
    private val refreshTokenExpirationHour: Long = 24L

) {
    private val hourToMilliseconds = 3_600_000L
    private val key = Keys.hmacShaKeyFor(secretKey.toByteArray())

    fun createToken(userId: String, roles: List<String>): Pair<String, String> {
        val now = Date()

        val accessToken = Jwts.builder()
            .subject(userId)
            .claim("roles", roles)
            .issuedAt(now)
            .expiration(Date(now.time + accessTokenExpirationHour * hourToMilliseconds))
            .signWith(key)
            .compact()

        val refreshToken = Jwts.builder()
            .subject(userId)
            .issuedAt(now)
            .expiration(Date(now.time + refreshTokenExpirationHour * hourToMilliseconds))
            .signWith(key)
            .compact()

        return accessToken to refreshToken
    }

    fun validate(token: String): Boolean {
        return try {
            val claims = parseClaims(token)

            !claims.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    fun parseClaims(token: String): Claims = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .payload
}
