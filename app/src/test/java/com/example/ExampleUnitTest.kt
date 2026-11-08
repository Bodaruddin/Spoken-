package com.example

import com.example.data.model.Role
import com.example.data.repository.LinguaQuestRepository
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testAdminSwitchToTeacherAndBackWithoutPin() {
        val repository = LinguaQuestRepository(null)
        // Set user to Admin
        repository.switchRole(Role.ADMIN)
        assertEquals(Role.ADMIN, repository.currentUser.value.role)
        assertTrue(repository.isAdminSession.value)

        // Admin switches to Teacher
        repository.switchRole(Role.TEACHER)
        assertEquals(Role.TEACHER, repository.currentUser.value.role)
        // Admin session must remain active!
        assertTrue(repository.isAdminSession.value)

        // Can switch back to Admin without PIN
        repository.switchRole(Role.ADMIN)
        assertEquals(Role.ADMIN, repository.currentUser.value.role)
        assertTrue(repository.isAdminSession.value)
    }

    @Test
    fun testAdminSwitchToStudentAndBackWithoutPin() {
        val repository = LinguaQuestRepository(null)
        // Set user to Admin
        repository.switchRole(Role.ADMIN)
        assertEquals(Role.ADMIN, repository.currentUser.value.role)
        assertTrue(repository.isAdminSession.value)

        // Admin switches to Student
        repository.switchRole(Role.STUDENT)
        assertEquals(Role.STUDENT, repository.currentUser.value.role)
        // Admin session must remain active!
        assertTrue(repository.isAdminSession.value)

        // Can switch back to Admin directly!
        repository.switchRole(Role.ADMIN)
        assertEquals(Role.ADMIN, repository.currentUser.value.role)
        assertTrue(repository.isAdminSession.value)
    }
}

