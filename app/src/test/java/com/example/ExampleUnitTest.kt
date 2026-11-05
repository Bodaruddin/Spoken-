package com.example

import com.example.data.model.Role
import com.example.data.repository.LinguaQuestRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testAdminAddFacultyWithSecurityPin() {
    val repository = LinguaQuestRepository()

    // Admin adds teacher with custom security PIN
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

    // Verify present in adminTeachers list
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

    // Verify student is stored in adminStudents
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
}
