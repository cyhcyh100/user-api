package kr.younghwan.userapi.domain.user.service

import io.kotest.matchers.shouldBe
import kr.younghwan.userapi.domain.user.entity.UserEntity
import kr.younghwan.userapi.domain.user.enums.UserRole
import kr.younghwan.userapi.domain.user.repository.UserRepository
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import kotlin.test.Test

class UserDetailsServiceImplTest {

    private val userRepository: UserRepository = mock()
    private val userDetailsService = UserDetailsServiceImpl(userRepository)

    @Test
    fun `loadUserByUsername should return UserDetails when user exists`() {
        // given
        val user = UserEntity(
            id = 1L,
            email = "test@example.com",
            password = "encodedPassword",
            name = "홍길동",
            role = UserRole.MEMBER
        )

        whenever(userRepository.findByEmail(user.email)).thenReturn(user)

        // when
        val result: UserDetails = userDetailsService.loadUserByUsername(user.email)

        // then
        result.username shouldBe "1"
        result.password shouldBe "encodedPassword"
        result.authorities shouldBe listOf(SimpleGrantedAuthority("ROLE_MEMBER"))
    }

    @Test
    fun `loadUserByUsername should throw UsernameNotFoundException when user not found`() {
        // given
        val email = "notfound@example.com"
        whenever(userRepository.findByEmail(email)).thenReturn(null)

        // when
        val exception = assertThrows<UsernameNotFoundException> {
            userDetailsService.loadUserByUsername(email)
        }

        // then
        exception.message shouldBe "User not found"
    }
}