package kr.younghwan.userapi.global.jwt

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.*
import kotlin.test.Test

class JwtTokenProviderTest {

    private val secret = "secret-secret-secret-secret-secret-secret-secret-secret"
    private val jwtTokenProvider = JwtTokenProvider(
        secretKey = secret,
        accessTokenExpirationHour = 1L,
        refreshTokenExpirationHour = 24L
    )

    @Test
    fun `createToken should return valid access and refresh tokens`() {
        // given
        val username = "test@example.com"
        val roles = listOf("ROLE_MEMBER")

        // when
        val (accessToken, refreshToken) = jwtTokenProvider.createToken(username, roles)
        jwtTokenProvider.validate(accessToken) shouldBe true
        val claims = jwtTokenProvider.parseClaims(accessToken)

        // then
        accessToken shouldNotBe refreshToken
        claims.subject shouldBe "test@example.com"
        claims["roles"] shouldBe listOf("ROLE_MEMBER")
    }

    @Test
    fun `Expired token should return false in validate`() {
        // given
        val expiredToken = io.jsonwebtoken.Jwts.builder()
            .subject("42")
            .expiration(Date(System.currentTimeMillis() - 1000))
            .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(secret.toByteArray()))
            .compact()

        // when
        val isValid = jwtTokenProvider.validate(expiredToken)

        // then
        isValid shouldBe false
    }
}