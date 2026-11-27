
package com.workmanagement.backend.user.repository

import com.workmanagement.backend.user.User
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserRepository : JpaRepository<User, Long> {
    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): User?
    fun findByNameStartingWithIgnoreCaseAndIdNotOrderByNameAsc(name: String, id: Long): List<User>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    fun lockById(@Param("id") id: Long): User?
}
