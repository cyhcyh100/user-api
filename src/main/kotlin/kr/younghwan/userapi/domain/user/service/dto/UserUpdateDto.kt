package kr.younghwan.userapi.domain.user.service.dto

data class UserUpdateDto(
    val userId: Long,
    val email: String? = null,
    val name: String? = null,
) {
    init {
        name?.let {
            require(it.isNotBlank()) { "name must not be blank" }
        }
    }
}
