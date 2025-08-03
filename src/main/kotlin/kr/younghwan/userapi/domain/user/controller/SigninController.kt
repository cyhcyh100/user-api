package kr.younghwan.userapi.domain.user.controller

import jakarta.validation.Valid
import kr.younghwan.userapi.domain.user.controller.dto.SigninRequest
import kr.younghwan.userapi.domain.user.exception.InvalidCredentialsException
import kr.younghwan.userapi.domain.user.service.UserService
import kr.younghwan.userapi.domain.user.service.dto.SigninResponse
import kr.younghwan.userapi.global.error.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SigninController(
    private val userService: UserService
) {

    @PostMapping("/signin")
    fun signin(@Valid @RequestBody request: SigninRequest): ResponseEntity<SigninResponse> =
        ResponseEntity.ok(userService.signin(request.toServiceDto()))

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleException(e: InvalidCredentialsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ErrorResponse(
                code = "INVALID_CREDENTIALS",
                message = e.message ?: "Invalid email or password"
            )
        )
    }
}