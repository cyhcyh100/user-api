package kr.younghwan.userapi.domain.user.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kr.younghwan.userapi.domain.user.entity.UserEntity
import kr.younghwan.userapi.domain.user.exception.EmailAlreadyExistsException
import kr.younghwan.userapi.domain.user.repository.UserRepository
import kr.younghwan.userapi.domain.user.service.dto.SigninDto
import kr.younghwan.userapi.domain.user.service.dto.SignupDto
import kr.younghwan.userapi.global.jwt.JwtTokenProvider
import org.mockito.kotlin.*
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.Test
import kotlin.test.assertFailsWith

class UserServiceTest {
    private val userRepository: UserRepository = mock()
    private val passwordEncoder: PasswordEncoder = mock()
    private val jwtTokenProvider: JwtTokenProvider = mock()
    private val authenticationManager: AuthenticationManager = mock()
    private val userService = UserService(userRepository, passwordEncoder, jwtTokenProvider, authenticationManager)

    @Test
    fun `signup should create a user when email does not exist`() {
        // given
        val signupDto = SignupDto(
            email = "test@example.com",
            password = "secure123!",
            name = "홍길동"
        )

        whenever(userRepository.existsByEmail(signupDto.email)).thenReturn(false)
        whenever(passwordEncoder.encode(signupDto.password)).thenReturn("encodedPassword")

        // when
        userService.signup(signupDto)

        // then
        argumentCaptor<UserEntity>().apply {
            verify(userRepository).save(capture())
            firstValue.email shouldBe "test@example.com"
            firstValue.password shouldBe "encodedPassword"
            firstValue.name shouldBe "홍길동"
        }
    }

    @Test
    fun `signup should throw exception when email already exists`() {
        // given
        val signupDto = SignupDto(
            email = "duplicate@example.com",
            password = "pass123",
            name = "홍길동"
        )

        whenever(userRepository.existsByEmail(signupDto.email)).thenReturn(true)

        // when & then
        val exception = shouldThrow<EmailAlreadyExistsException> {
            userService.signup(signupDto)
        }

        exception.message shouldBe "Email already exists: duplicate@example.com"
    }

    @Test
    fun `signin should return tokens when credentials are valid`() {
        // given
        val dto = SigninDto(
            email = "test@example.com",
            password = "password123"
        )

        val userDetails = User(
            dto.email,
            "encodedPassword",
            listOf(SimpleGrantedAuthority("ROLE_MEMBER"))
        )

        val authentication = UsernamePasswordAuthenticationToken(
            userDetails,
            dto.password,
            userDetails.authorities
        )

        whenever(authenticationManager.authenticate(any())).thenReturn(authentication)
        whenever(jwtTokenProvider.createToken(dto.email, listOf("ROLE_MEMBER")))
            .thenReturn("accessToken" to "refreshToken")

        // when
        val response = userService.signin(dto)

        // then
        response.accessToken shouldBe "accessToken"
        response.refreshToken shouldBe "refreshToken"
        response.tokenType shouldBe "Bearer"
    }

    @Test
    fun `signin should throw exception when authentication fails`() {
        // given
        val dto = SigninDto("fail@example.com", "wrongPassword")

        whenever(authenticationManager.authenticate(any()))
            .thenThrow(BadCredentialsException("Bad credentials"))

        // when & then
        val exception = assertFailsWith<BadCredentialsException> {
            userService.signin(dto)
        }

        exception.message shouldBe "Bad credentials"
    }
}