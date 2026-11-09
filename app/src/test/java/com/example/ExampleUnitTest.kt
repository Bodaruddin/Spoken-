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

    @Test
    fun testMcqExamCreationAndGradingWithCertificate() {
        val repository = LinguaQuestRepository(null)
        val initialExams = repository.exams.value
        assertTrue(initialExams.isNotEmpty())

        val testQuestions = listOf(
            com.example.data.model.ExamQuestion("q1", "What is the capital of UK?", listOf("Paris", "London", "Berlin", "Rome"), 1, 10),
            com.example.data.model.ExamQuestion("q2", "Identify the noun:", listOf("Run", "Happiness", "Quickly", "Very"), 1, 10)
        )

        val createdExam = repository.createExam(
            title = "Unit Test English Assessment",
            description = "Quick assessment",
            teacherName = "Prof. Test",
            targetClass = "Class 10",
            subjectCategory = "English Grammar",
            durationMinutes = 10,
            passingPercentage = 60,
            questions = testQuestions
        )

        assertEquals("Unit Test English Assessment", createdExam.title)
        assertEquals(20, createdExam.totalMarks)
        assertEquals(12, createdExam.passingMarks)

        // Submit exam with 1 correct out of 2 (10/20 = 50% -> Fail if pass is 60%)
        val subFail = repository.submitExam(
            examId = createdExam.id,
            studentId = "stu_1",
            studentName = "Test Student",
            studentClass = "Class 10",
            studentAvatar = "🎓",
            selectedAnswers = mapOf("q1" to 1, "q2" to 0), // q1 correct (1), q2 wrong (0 instead of 1)
            timeTakenSeconds = 120
        )
        assertEquals(10, subFail.scoreObtained)
        assertEquals(50, subFail.percentage)
        assertFalse(subFail.isPassed)

        // Submit exam with 2 correct out of 2 (20/20 = 100% -> Grade O Passed)
        val subPass = repository.submitExam(
            examId = createdExam.id,
            studentId = "stu_1",
            studentName = "Test Student",
            studentClass = "Class 10",
            studentAvatar = "🎓",
            selectedAnswers = mapOf("q1" to 1, "q2" to 1),
            timeTakenSeconds = 150
        )
        assertEquals(20, subPass.scoreObtained)
        assertEquals(100, subPass.percentage)
        assertTrue(subPass.isPassed)
        assertTrue(subPass.certificateId.startsWith("SPK-2026-"))
    }

    @Test
    fun testFarihaYesminCertificateFromScreenshotDetails() {
        val repository = LinguaQuestRepository(null)
        val submissions = repository.examSubmissions.value
        val farihaCert = submissions.firstOrNull { it.studentName == "Fariha Yesmin" }

        assertNotNull(farihaCert)
        farihaCert?.let { cert ->
            assertEquals("Fariha Yesmin", cert.studentName)
            assertEquals("English Grammar Test", cert.examTitle)
            assertEquals("VIII", cert.studentClass)
            assertEquals(100, cert.totalMarks)
            assertEquals(88, cert.scoreObtained)
            assertEquals(88, cert.percentage)
            assertEquals("A", cert.grade)
            assertEquals("Excellent Performance", cert.performanceTitle)
            assertEquals("27 Nov 2026", cert.completedAt)
            assertEquals("SPK-2026-000368", cert.certificateId)
            assertTrue(cert.isPassed)
        }
    }
}

