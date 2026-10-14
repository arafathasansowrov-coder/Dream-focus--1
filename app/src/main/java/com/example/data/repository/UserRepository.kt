package com.example.data.repository

import com.example.data.model.GoalItem
import com.example.data.model.RoutineItem
import com.example.data.model.StudentProfile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Repository to manage Firestore document storage for user profile, goals, and routines.
 * Firestore Document Schema:
 * - users/{userId} (Profile document)
 * - users/{userId}/goals/{goalId} (Subcollection of student goals)
 * - users/{userId}/routines/{routineId} (Subcollection of daily routines)
 */
class UserRepository(
    databaseId: String? = null,
    firestoreInstance: FirebaseFirestore? = null
) {
    private val firestore: FirebaseFirestore? = firestoreInstance ?: run {
        try {
            val dbId = if (!databaseId.isNullOrBlank()) {
                databaseId
            } else {
                "ai-studio-android-dreamfoc-9fd8fe1e-12cd-4e02-b9e6-9eab8003a900"
            }
            FirebaseFirestore.getInstance(dbId)
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Failed to initialize Firestore with custom databaseId: ${e.message}")
            null
        }
    }

    private fun usersCollection() = firestore?.collection("users")
    private fun userGoalsCollection(userId: String) = usersCollection()?.document(userId)?.collection("goals")
    private fun userRoutinesCollection(userId: String) = usersCollection()?.document(userId)?.collection("routines")

    // ==========================================
    // USER PROFILE DOCUMENT STORAGE
    // ==========================================

    suspend fun saveUserProfile(userId: String, profile: StudentProfile): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val col = usersCollection()
            if (col == null) {
                if (continuation.isActive) continuation.resume(Result.success(Unit))
                return@suspendCancellableCoroutine
            }
            val data = hashMapOf(
                "id" to profile.id,
                "name" to profile.name,
                "dreamTitle" to profile.dreamTitle,
                "dreamDescription" to profile.dreamDescription,
                "targetYear" to profile.targetYear,
                "dailyTargetHours" to profile.dailyTargetHours.toDouble(),
                "currentStreak" to profile.currentStreak,
                "longestStreak" to profile.longestStreak,
                "totalFocusMinutes" to profile.totalFocusMinutes,
                "completedMilestonesCount" to profile.completedMilestonesCount,
                "isCloudSyncEnabled" to profile.isCloudSyncEnabled,
                "fcmToken" to profile.fcmToken,
                "updatedAt" to System.currentTimeMillis()
            )

            col.document(userId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) continuation.resume(Result.failure(exception))
                }
        }

    fun observeUserProfile(userId: String): Flow<StudentProfile?> = callbackFlow {
        val col = usersCollection()
        if (col == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val listenerRegistration = col.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val profile = parseProfile(snapshot)
                    trySend(profile)
                } else {
                    trySend(null)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    private fun parseProfile(doc: DocumentSnapshot): StudentProfile {
        return StudentProfile(
            id = (doc.getLong("id") ?: 1L).toInt(),
            name = doc.getString("name") ?: "Future Leader",
            dreamTitle = doc.getString("dreamTitle") ?: "AI Engineer",
            dreamDescription = doc.getString("dreamDescription") ?: "",
            targetYear = (doc.getLong("targetYear") ?: 2028L).toInt(),
            dailyTargetHours = (doc.getDouble("dailyTargetHours") ?: 4.0).toFloat(),
            currentStreak = (doc.getLong("currentStreak") ?: 0L).toInt(),
            longestStreak = (doc.getLong("longestStreak") ?: 0L).toInt(),
            totalFocusMinutes = (doc.getLong("totalFocusMinutes") ?: 0L).toInt(),
            completedMilestonesCount = (doc.getLong("completedMilestonesCount") ?: 0L).toInt(),
            isCloudSyncEnabled = doc.getBoolean("isCloudSyncEnabled") ?: true,
            fcmToken = doc.getString("fcmToken") ?: ""
        )
    }

    // ==========================================
    // USER GOALS DOCUMENT STORAGE
    // ==========================================

    suspend fun saveGoal(userId: String, goal: GoalItem): Result<String> =
        suspendCancellableCoroutine { continuation ->
            val col = userGoalsCollection(userId)
            if (col == null) {
                if (continuation.isActive) continuation.resume(Result.success(goal.id.toString()))
                return@suspendCancellableCoroutine
            }
            val docRef = if (goal.id != 0L) {
                col.document(goal.id.toString())
            } else {
                col.document()
            }

            val data = hashMapOf(
                "id" to if (goal.id != 0L) goal.id else docRef.id.hashCode().toLong(),
                "title" to goal.title,
                "category" to goal.category,
                "priority" to goal.priority,
                "deadlineDate" to goal.deadlineDate,
                "progressPercentage" to goal.progressPercentage,
                "milestonesJson" to goal.milestonesJson,
                "isCompleted" to goal.isCompleted,
                "colorHex" to goal.colorHex,
                "updatedAt" to System.currentTimeMillis()
            )

            docRef.set(data, SetOptions.merge())
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(Result.success(docRef.id))
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) continuation.resume(Result.failure(exception))
                }
        }

    suspend fun updateGoalProgress(
        userId: String,
        goalId: String,
        progress: Int,
        isCompleted: Boolean
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val col = userGoalsCollection(userId)
        if (col == null) {
            if (continuation.isActive) continuation.resume(Result.success(Unit))
            return@suspendCancellableCoroutine
        }
        val updates = mapOf<String, Any>(
            "progressPercentage" to progress,
            "isCompleted" to isCompleted,
            "updatedAt" to System.currentTimeMillis()
        )

        col.document(goalId)
            .update(updates)
            .addOnSuccessListener {
                if (continuation.isActive) continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                if (continuation.isActive) continuation.resume(Result.failure(exception))
            }
    }

    suspend fun deleteGoal(userId: String, goalId: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val col = userGoalsCollection(userId)
            if (col == null) {
                if (continuation.isActive) continuation.resume(Result.success(Unit))
                return@suspendCancellableCoroutine
            }
            col.document(goalId)
                .delete()
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) continuation.resume(Result.failure(exception))
                }
        }

    fun observeUserGoals(userId: String): Flow<List<GoalItem>> = callbackFlow {
        val col = userGoalsCollection(userId)
        if (col == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val listenerRegistration = col
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = mutableListOf<GoalItem>()
                querySnapshot?.documents?.forEach { doc ->
                    val goal = GoalItem(
                        id = doc.getLong("id") ?: (doc.id.hashCode().toLong()),
                        title = doc.getString("title") ?: "",
                        category = doc.getString("category") ?: "Academics",
                        priority = doc.getString("priority") ?: "Medium",
                        deadlineDate = doc.getString("deadlineDate") ?: "",
                        progressPercentage = (doc.getLong("progressPercentage") ?: 0L).toInt(),
                        milestonesJson = doc.getString("milestonesJson") ?: "",
                        isCompleted = doc.getBoolean("isCompleted") ?: false,
                        colorHex = doc.getLong("colorHex") ?: 0xFF6366F1
                    )
                    list.add(goal)
                }
                trySend(list)
            }
        awaitClose { listenerRegistration.remove() }
    }

    // ==========================================
    // USER ROUTINES DOCUMENT STORAGE
    // ==========================================

    suspend fun saveRoutine(userId: String, routine: RoutineItem): Result<String> =
        suspendCancellableCoroutine { continuation ->
            val col = userRoutinesCollection(userId)
            if (col == null) {
                if (continuation.isActive) continuation.resume(Result.success(routine.id.toString()))
                return@suspendCancellableCoroutine
            }
            val docRef = if (routine.id != 0L) {
                col.document(routine.id.toString())
            } else {
                col.document()
            }

            val data = hashMapOf(
                "id" to if (routine.id != 0L) routine.id else docRef.id.hashCode().toLong(),
                "title" to routine.title,
                "scheduledTime" to routine.scheduledTime,
                "category" to routine.category,
                "durationMinutes" to routine.durationMinutes,
                "isCompletedToday" to routine.isCompletedToday,
                "alarmEnabled" to routine.alarmEnabled,
                "daysOfWeek" to routine.daysOfWeek,
                "reminderType" to routine.reminderType,
                "psychologicalTone" to routine.psychologicalTone,
                "alarmSound" to routine.alarmSound,
                "snoozeMinutes" to routine.snoozeMinutes,
                "customPsychologicalMessage" to routine.customPsychologicalMessage,
                "updatedAt" to System.currentTimeMillis()
            )

            docRef.set(data, SetOptions.merge())
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(Result.success(docRef.id))
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) continuation.resume(Result.failure(exception))
                }
        }

    suspend fun toggleRoutineCompletion(
        userId: String,
        routineId: String,
        isCompleted: Boolean
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val col = userRoutinesCollection(userId)
        if (col == null) {
            if (continuation.isActive) continuation.resume(Result.success(Unit))
            return@suspendCancellableCoroutine
        }
        col.document(routineId)
            .update("isCompletedToday", isCompleted, "updatedAt", System.currentTimeMillis())
            .addOnSuccessListener {
                if (continuation.isActive) continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                if (continuation.isActive) continuation.resume(Result.failure(exception))
            }
    }

    suspend fun deleteRoutine(userId: String, routineId: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val col = userRoutinesCollection(userId)
            if (col == null) {
                if (continuation.isActive) continuation.resume(Result.success(Unit))
                return@suspendCancellableCoroutine
            }
            col.document(routineId)
                .delete()
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) continuation.resume(Result.failure(exception))
                }
        }

    fun observeUserRoutines(userId: String): Flow<List<RoutineItem>> = callbackFlow {
        val col = userRoutinesCollection(userId)
        if (col == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val listenerRegistration = col
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = mutableListOf<RoutineItem>()
                querySnapshot?.documents?.forEach { doc ->
                    val routine = RoutineItem(
                        id = doc.getLong("id") ?: (doc.id.hashCode().toLong()),
                        title = doc.getString("title") ?: "",
                        scheduledTime = doc.getString("scheduledTime") ?: "08:00 AM",
                        category = doc.getString("category") ?: "Deep Work",
                        durationMinutes = (doc.getLong("durationMinutes") ?: 45L).toInt(),
                        isCompletedToday = doc.getBoolean("isCompletedToday") ?: false,
                        alarmEnabled = doc.getBoolean("alarmEnabled") ?: true,
                        daysOfWeek = doc.getString("daysOfWeek") ?: "Mon,Tue,Wed,Thu,Fri,Sat,Sun",
                        reminderType = doc.getString("reminderType") ?: "DIRECT_ALARM",
                        psychologicalTone = doc.getString("psychologicalTone") ?: "Growth Mindset & Discipline",
                        alarmSound = doc.getString("alarmSound") ?: "Chime",
                        snoozeMinutes = (doc.getLong("snoozeMinutes") ?: 5L).toInt(),
                        customPsychologicalMessage = doc.getString("customPsychologicalMessage") ?: ""
                    )
                    list.add(routine)
                }
                trySend(list)
            }
        awaitClose { listenerRegistration.remove() }
    }

    // ==========================================
    // BATCH CLOUD SYNC
    // ==========================================

    suspend fun syncAllUserData(
        userId: String,
        profile: StudentProfile,
        goals: List<GoalItem>,
        routines: List<RoutineItem>
    ): Result<Unit> {
        try {
            saveUserProfile(userId, profile)
            for (goal in goals) {
                saveGoal(userId, goal)
            }
            for (routine in routines) {
                saveRoutine(userId, routine)
            }
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
