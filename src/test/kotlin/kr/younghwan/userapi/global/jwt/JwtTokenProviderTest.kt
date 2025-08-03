package kr.younghwan.userapi.global.jwt

import io.kotest.matchers.shouldNotBe
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

        // then
        accessToken shouldNotBe null
        refreshToken shouldNotBe null
        accessToken shouldNotBe refreshToken
    }
}