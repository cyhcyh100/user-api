package kr.younghwan.userapi.domain.user.controller.dto

import jakarta.validation.constraints.Email
import kr.younghwan.userapi.domain.user.service.dto.UserUpdateDto

data class UserUpdateRequest(
    @field:Email(message = "유효하지 않은 이메일 형식입니다.")
    val email: String? = null,
    val name: String? = null,
) {
    fun toServiceDto(userId: String): UserUpdateDto {
        return UserUpdateDto(
            userId = userId.toLong(),
            email = email,
            name = name
        )
    }
}
