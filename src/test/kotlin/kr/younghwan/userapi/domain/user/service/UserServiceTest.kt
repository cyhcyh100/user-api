package kr.younghwan.userapi.domain.user.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kr.younghwan.userapi.domain.user.entity.UserEntity
import kr.younghwan.userapi.domain.user.exception.EmailAlreadyExistsException
import kr.younghwan.userapi.domain.user.repository.UserRepository
import kr.younghwan.userapi.domain.user.service.dto.SignupDto
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.Test

class UserServiceTest {
    private val userRepository: UserRepository = mock()
    private val passwordEncoder: PasswordEncoder = mock()
    private val userService = UserService(userRepository, passwordEncoder)

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
}