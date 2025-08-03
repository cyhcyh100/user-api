package kr.younghwan.userapi.global.initial

import kr.younghwan.userapi.domain.user.entity.UserEntity
import kr.younghwan.userapi.domain.user.enums.UserRole
import kr.younghwan.userapi.domain.user.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class AdminUserInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (!userRepository.existsByEmail("admin@example.com")) {
            val admin = UserEntity(
                email = "admin@example.com",
                password = passwordEncoder.encode("password"),
                name = "admin",
                role = UserRole.ADMIN,
            )
            userRepository.save(admin)
        }
    }
}