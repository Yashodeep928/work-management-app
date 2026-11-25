
package com.workmanagement.backend.connection

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "connections")
class Connection(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "requester_id", nullable = false)
    var requesterId: Long = 0,

    @Column(name = "receiver_id", nullable = false)
    var receiverId: Long = 0,

    @Column(nullable = false)
    var status: String = "PENDING",

    @Column(
        name = "created_at",
        insertable = false,
        updatable = false
    )
    var createdAt: OffsetDateTime? = null
)
