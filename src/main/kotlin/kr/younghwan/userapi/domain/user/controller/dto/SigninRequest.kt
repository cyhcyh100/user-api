package kr.younghwan.userapi.domain.user.controller.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import kr.younghwan.userapi.domain.user.service.dto.SigninDto

data class SigninRequest(
    @field:Email(message = "유효하지 않은 이메일 형식입니다.")
    @field:NotBlank(message = "이메일은 필수입니다.")
    val email: String?,
    @field:NotBlank(message = "비밀번호는 필수입니다.")
    val password: String?
) {
    fun toServiceDto(): SigninDto =
        SigninDto(
            email = email!!,
            password = password!!
        )
}