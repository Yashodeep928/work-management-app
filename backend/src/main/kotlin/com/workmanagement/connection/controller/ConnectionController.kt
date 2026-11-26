
package com.workmanagement.backend.connection.controller

import com.workmanagement.backend.connection.dto.*
import com.workmanagement.backend.connection.service.ConnectionService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/connections")
class ConnectionController(
    private val connectionService: ConnectionService
) {

    @PostMapping("/request")
    @ResponseStatus(HttpStatus.CREATED)
    fun sendRequest(
        @RequestBody request: ConnectionRequestDto
    ): ConnectionResponseDto {
        return connectionService.sendRequest(request)
    }


@PatchMapping("/{id}/accept")
fun acceptRequest(
    @PathVariable("id") id: Long,
    @RequestBody request: AcceptConnectionRequest
): ConnectionResponseDto {

    return connectionService.acceptRequest(id, request)
}

}
