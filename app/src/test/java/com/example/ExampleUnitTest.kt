package com.example

import com.example.data.model.Role
import com.example.data.repository.LinguaQuestRepository
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testAdminAddFacultyWithSecurityPin() {
    val repository = LinguaQuestRepository()
    val newTeacher = repository.addTeacher(
      name = "Prof. Robert Langdon",
      email = "r.langdon@linguaquest.edu",
      department = "Phonetics Dept",
      securityPin = "8899"
    )
    assertEquals("Prof. Robert Langdon", newTeacher.name)
    assertEquals("r.langdon@linguaquest.edu", newTeacher.email)
    assertEquals(Role.TEACHER, newTeacher.role)
    assertEquals("8899", newTeacher.securityPin)
    val found = repository.adminTeachers.value.find { it.email == "r.langdon@linguaquest.edu" }
    assertNotNull(found)
    assertEquals("8899", found?.securityPin)
  }

  @Test
  fun testStudentSelfRegistrationWithPin() {
    val repository = LinguaQuestRepository()
    val registered = repository.registerStudent(
      name = "Maya Sharma",
      email = "maya.s@linguaquest.edu",
      batch = "Batch Gamma",
      securityPin = "4321"
    )
    assertEquals("Maya Sharma", registered.name)
    assertEquals("maya.s@linguaquest.edu", registered.email)
    assertEquals(Role.STUDENT, registered.role)
    assertEquals("4321", registered.securityPin)
    val studentInfo = repository.adminStudents.value.find { it.email == "maya.s@linguaquest.edu" }
    assertNotNull(studentInfo)
    assertEquals("4321", studentInfo?.securityPin)
  }

  @Test
  fun testRegistrationPermissionsToggle() {
    val repository = LinguaQuestRepository()
    assertTrue(repository.allowStudentRegistration.value)
    assertTrue(repository.allowTeacherRegistration.value)
    repository.setAllowStudentRegistration(false)
    assertEquals(false, repository.allowStudentRegistration.value)
    repository.setAllowTeacherRegistration(false)
    assertEquals(false, repository.allowTeacherRegistration.value)
  }

  @Test
  fun testTeacherAddEditDelete() {
    val repository = LinguaQuestRepository()
    val initialCount = repository.adminTeachers.value.size

    val newTeacher = repository.addTeacher("Prof. John Doe", "john.doe@linguaquest.edu", "Advanced Speaking", "8822")
    assertEquals(initialCount + 1, repository.adminTeachers.value.size)
    assertEquals("8822", newTeacher.securityPin)

    val updatedTeacher = newTeacher.copy(name = "Prof. Johnathan Doe", batch = "Phonetics Lab", securityPin = "9933")
    repository.updateTeacher(updatedTeacher)
    val foundTeacher = repository.adminTeachers.value.find { it.id == newTeacher.id }
    assertNotNull(foundTeacher)
    assertEquals("Prof. Johnathan Doe", foundTeacher?.name)
    assertEquals("Phonetics Lab", foundTeacher?.batch)
    assertEquals("9933", foundTeacher?.securityPin)

    repository.deleteTeacher(newTeacher.id)
    assertEquals(initialCount, repository.adminTeachers.value.size)
    assertNull(repository.adminTeachers.value.find { it.id == newTeacher.id })
  }

  @Test
  fun testAdminSwitchRoleWithoutPin() {
    val repository = LinguaQuestRepository()
    repository.switchRole(Role.ADMIN)
    assertEquals(Role.ADMIN, repository.currentUser.value.role)

    // Switch directly to Teacher
    repository.switchRole(Role.TEACHER)
    assertEquals(Role.TEACHER, repository.currentUser.value.role)
  }
}
