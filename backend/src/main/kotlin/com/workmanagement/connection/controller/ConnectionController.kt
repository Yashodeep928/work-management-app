package com.workmanagement.backend.connection.controller

import com.workmanagement.backend.connection.dto.*
import com.workmanagement.backend.connection.service.ConnectionService
import com.workmanagement.backend.user.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/connections")
class ConnectionController(
    private val connectionService: ConnectionService,
    private val userService: UserService
) {
    @PostMapping("/request")
    @ResponseStatus(HttpStatus.CREATED)
    fun sendRequest(principal: Principal, @RequestBody request: ConnectionRequestDto): ConnectionResponseDto =
        connectionService.sendRequest(userService.getCurrentUser(principal.name).id, request)

    @PatchMapping("/{id}/accept")
    fun acceptRequest(
        principal: Principal,
        @PathVariable("id") id: Long,
        @RequestBody request: AcceptConnectionRequest
    ): ConnectionResponseDto =
        connectionService.acceptRequest(userService.getCurrentUser(principal.name).id, id, request)

    @PostMapping("/connect")
    fun connect(principal: Principal, @RequestBody request: ConnectUserRequest): ConnectionResponseDto =
        connectionService.connect(userService.getCurrentUser(principal.name).id, request)

    @GetMapping("/user/{userId}")
    fun getConnectedUsers(principal: Principal, @PathVariable("userId") userId: Long): List<ConnectedUserDto> =
        connectionService.getConnectedUsers(userService.getCurrentUser(principal.name).id, userId)
}
