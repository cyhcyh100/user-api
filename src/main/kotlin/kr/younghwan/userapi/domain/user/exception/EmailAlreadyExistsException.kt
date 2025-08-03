package kr.younghwan.userapi.domain.user.exception

class EmailAlreadyExistsException(email: String) :
    RuntimeException("Email already exists: $email")