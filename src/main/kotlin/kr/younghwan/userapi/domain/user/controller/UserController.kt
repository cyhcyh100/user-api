package kr.younghwan.userapi.domain.user.controller

import kr.younghwan.userapi.domain.user.service.UserService
import kr.younghwan.userapi.domain.user.service.dto.UserResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/users/{userId}")
    @PreAuthorize(value = "hasRole('ADMIN') or #userId == authentication.principal.username")
    fun getUser(
        @PathVariable userId: String,
    ): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.getUser(userId.toLong()))
    }
}