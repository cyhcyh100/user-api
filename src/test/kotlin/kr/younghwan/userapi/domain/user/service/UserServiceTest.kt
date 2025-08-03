package kr.younghwan.userapi.domain.user.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kr.younghwan.userapi.domain.user.entity.UserEntity
import kr.younghwan.userapi.domain.user.exception.EmailAlreadyExistsException
import kr.younghwan.userapi.domain.user.repository.UserRepository
import kr.younghwan.userapi.domain.user.service.dto.SigninDto
import kr.younghwan.userapi.domain.user.service.dto.SignupDto
import kr.younghwan.userapi.domain.user.service.dto.UserUpdateDto
import kr.younghwan.userapi.global.jwt.JwtTokenProvider
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*
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

    @Test
    fun `getUsers should return paginated users`() {
        val entities = listOf(
            UserEntity(id = 1L, email = "user1@example.com", password = "password1", name = "user1"),
            UserEntity(id = 2L, email = "user2@example.com", password = "password2", name = "user2"),
        )
        whenever(userRepository.findAll(any<PageRequest>())).thenReturn(PageImpl(entities))

        val result = userService.getUsers(PageRequest.of(0, 10))

        result.content.size shouldBe 2
        result.content[0].id shouldBe 1L
        result.content[0].email shouldBe "user1@example.com"
        result.content[0].name shouldBe "user1"
        result.content[1].id shouldBe 2L
        result.content[1].email shouldBe "user2@example.com"
        result.content[1].name shouldBe "user2"
        result.totalElements shouldBe 2
        result.totalPages shouldBe 1
        result.size shouldBe 2
        result.number shouldBe 0
    }

    @Test
    fun `getUser should return user info`() {
        // given
        whenever(userRepository.findById(1L)).thenReturn(
            Optional.of(
                UserEntity(
                    id = 1L,
                    email = "test@example.com",
                    password = "encodedPassword",
                    name = "홍길동"
                )
            )
        )

        // when
        val userResponse = userService.getUser(1L)

        // then
        userResponse.id shouldBe 1L
        userResponse.email shouldBe "test@example.com"
        userResponse.name shouldBe "홍길동"
    }

    @Test
    fun `getUser should throw exception when user not found`() {
        // given
        whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

        // when & then
        val exception = assertFailsWith<UsernameNotFoundException> {
            userService.getUser(999L)
        }

        exception.message shouldBe "User not found"
    }

    @Test
    fun `updateUser should update user's email and name`() {
        // given
        val userEntity = UserEntity(
            id = 1L,
            email = "test@example.com",
            password = "encodedPassword",
            name = "홍길동"
        )

        whenever(userRepository.findById(1L)).thenReturn(Optional.of(userEntity))

        // when
        userService.updateUser(UserUpdateDto(1L, "test2@example.com", "변경된 유저"))

        // then
        userEntity.email shouldBe "test2@example.com"
        userEntity.name shouldBe "변경된 유저"
    }
}