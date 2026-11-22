
package com.workmanagement.backend.user.dto

data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String
)

data class UserResponse(
    val id: Long,
    val name: String,
    val email: String
)
