package kr.younghwan.userapi.domain.user.service.dto

data class UserResponse(
    val id: Long,
    val email: String,
    val name: String,
)