package kr.younghwan.userapi.domain.user.service

import kr.younghwan.userapi.domain.user.entity.UserEntity
import kr.younghwan.userapi.domain.user.exception.EmailAlreadyExistsException
import kr.younghwan.userapi.domain.user.repository.UserRepository
import kr.younghwan.userapi.domain.user.service.dto.SigninDto
import kr.younghwan.userapi.domain.user.service.dto.SigninResponse
import kr.younghwan.userapi.domain.user.service.dto.SignupDto
import kr.younghwan.userapi.domain.user.service.dto.UserResponse
import kr.younghwan.userapi.global.jwt.JwtTokenProvider
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val authenticationManager: AuthenticationManager,
) {

    @Transactional
    fun signup(dto: SignupDto) {
        if (userRepository.existsByEmail(dto.email)) {
            throw EmailAlreadyExistsException(dto.email)
        }

        val user = UserEntity(
            email = dto.email,
            password = passwordEncoder.encode(dto.password),
            name = dto.name
        )
        userRepository.save(user)
    }

    fun signin(dto: SigninDto): SigninResponse {
        val authToken = UsernamePasswordAuthenticationToken(dto.email, dto.password)
        val authentication = authenticationManager.authenticate(authToken)

        val userDetails = authentication.principal as UserDetails
        val roles = userDetails.authorities.map { it.authority }

        val (accessToken, refreshToken) = jwtTokenProvider.createToken(userDetails.username, roles)

        return SigninResponse(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    @Transactional(readOnly = true)
    fun getUser(userId: Long): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { UsernameNotFoundException("User not found") }

        return UserResponse(
            id = user.id,
            email = user.email,
            name = user.name,
        )
    }
}