package kr.younghwan.userapi.domain.user.controller

import jakarta.validation.Valid
import kr.younghwan.userapi.domain.user.controller.dto.UserUpdateRequest
import kr.younghwan.userapi.domain.user.service.UserService
import kr.younghwan.userapi.domain.user.service.dto.UserResponse
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
class UserController(
    private val userService: UserService,
) {

    @GetMapping("/users")
    @PreAuthorize(value = "hasRole('ADMIN')")
    fun getUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<Page<UserResponse>> {
        return ResponseEntity.ok(userService.getUsers(page, size))
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize(value = "hasRole('ADMIN') or #userId == authentication.principal.username")
    fun getUser(@PathVariable userId: String): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.getUser(userId.toLong()))
    }

    @PutMapping("/users/{userId}")
    @PreAuthorize(value = "hasRole('ADMIN') or #userId == authentication.principal.username")
    fun updateUser(@PathVariable userId: String, @Valid @RequestBody request: UserUpdateRequest): ResponseEntity<String> {
        userService.updateUser(request.toServiceDto(userId))
        return ResponseEntity.ok(HttpStatus.OK.reasonPhrase)
    }
}