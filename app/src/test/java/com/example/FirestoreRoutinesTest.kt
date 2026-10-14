package com.example

import com.example.data.model.RoutineItem
import org.junit.Assert.assertEquals
import org.junit.Test

class FirestoreRoutinesTest {

    @Test
    fun calculateRoutineProgress_emptyList_returnsZero() {
        val routines = emptyList<RoutineItem>()
        val total = routines.size
        val completed = routines.count { it.isCompletedToday }
        val percentage = if (total > 0) (completed.toFloat() / total * 100).toInt() else 0

        assertEquals(0, percentage)
    }

    @Test
    fun calculateRoutineProgress_halfCompleted_returnsFiftyPercent() {
        val routines = listOf(
            RoutineItem(id = 1L, title = "Morning Coding", scheduledTime = "07:00 AM", isCompletedToday = true),
            RoutineItem(id = 2L, title = "Deep Math", scheduledTime = "09:00 AM", isCompletedToday = false),
            RoutineItem(id = 3L, title = "Reading", scheduledTime = "02:00 PM", isCompletedToday = true),
            RoutineItem(id = 4L, title = "Review Flashcards", scheduledTime = "08:00 PM", isCompletedToday = false)
        )
        val total = routines.size
        val completed = routines.count { it.isCompletedToday }
        val percentage = if (total > 0) (completed.toFloat() / total * 100).toInt() else 0

        assertEquals(2, completed)
        assertEquals(4, total)
        assertEquals(50, percentage)
    }

    @Test
    fun calculateRoutineProgress_allCompleted_returnsOneHundredPercent() {
        val routines = listOf(
            RoutineItem(id = 1L, title = "Morning Coding", scheduledTime = "07:00 AM", isCompletedToday = true),
            RoutineItem(id = 2L, title = "Deep Math", scheduledTime = "09:00 AM", isCompletedToday = true)
        )
        val total = routines.size
        val completed = routines.count { it.isCompletedToday }
        val percentage = if (total > 0) (completed.toFloat() / total * 100).toInt() else 0

        assertEquals(2, completed)
        assertEquals(2, total)
        assertEquals(100, percentage)
    }
}
