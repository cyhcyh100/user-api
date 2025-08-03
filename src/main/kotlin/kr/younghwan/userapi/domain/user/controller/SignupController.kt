package kr.younghwan.userapi.domain.user.controller

import jakarta.validation.Valid
import kr.younghwan.userapi.domain.user.controller.dto.SignupRequest
import kr.younghwan.userapi.domain.user.exception.EmailAlreadyExistsException
import kr.younghwan.userapi.domain.user.service.UserService
import kr.younghwan.userapi.global.error.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SignupController(
    private val userService: UserService
) {

    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<String> {
        userService.signup(request.toServiceDto())
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다.")
    }

    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleException(e: EmailAlreadyExistsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ErrorResponse(
                code = "USER_EMAIL_ALREADY_EXISTS",
                message = e.message ?: "이미 존재하는 이메일입니다."
            )
        )
    }
}