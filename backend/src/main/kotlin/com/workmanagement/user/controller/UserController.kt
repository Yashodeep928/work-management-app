
package com.workmanagement.backend.user.controller

import com.workmanagement.backend.user.dto.*
import com.workmanagement.backend.user.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(
        @RequestBody request: CreateUserRequest
    ): UserResponse = userService.createUser(request)

    @GetMapping
    fun getAllUsers(): List<UserResponse> =
        userService.getAllUsers()
}
