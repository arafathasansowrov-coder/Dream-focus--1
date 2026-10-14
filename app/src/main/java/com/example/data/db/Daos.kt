package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BlockedAppRule
import com.example.data.model.GoalItem
import com.example.data.model.RoutineItem
import com.example.data.model.StudentProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM student_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<StudentProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: StudentProfile)

    @Query("UPDATE student_profile SET currentStreak = currentStreak + 1, totalFocusMinutes = totalFocusMinutes + :additionalMinutes WHERE id = 1")
    suspend fun addFocusTime(additionalMinutes: Int)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY isCompleted ASC, id DESC")
    fun getAllGoals(): Flow<List<GoalItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalItem): Long

    @Update
    suspend fun updateGoal(goal: GoalItem)

    @Delete
    suspend fun deleteGoal(goal: GoalItem)

    @Query("UPDATE goals SET progressPercentage = :progress, isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateGoalProgress(id: Long, progress: Int, isCompleted: Boolean)
}

@Dao
interface GoalMilestoneDao {
    @Query("SELECT * FROM goal_milestones ORDER BY orderIndex ASC, id ASC")
    fun getAllMilestones(): Flow<List<com.example.data.model.GoalMilestoneEntity>>

    @Query("SELECT * FROM goal_milestones WHERE goalId = :goalId ORDER BY orderIndex ASC, id ASC")
    fun getMilestonesForGoal(goalId: Long): Flow<List<com.example.data.model.GoalMilestoneEntity>>

    @Query("SELECT * FROM goal_milestones WHERE goalId = :goalId")
    suspend fun getMilestonesForGoalSync(goalId: Long): List<com.example.data.model.GoalMilestoneEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: com.example.data.model.GoalMilestoneEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<com.example.data.model.GoalMilestoneEntity>): List<Long>

    @Update
    suspend fun updateMilestone(milestone: com.example.data.model.GoalMilestoneEntity)

    @Delete
    suspend fun deleteMilestone(milestone: com.example.data.model.GoalMilestoneEntity)

    @Query("DELETE FROM goal_milestones WHERE goalId = :goalId")
    suspend fun deleteMilestonesForGoal(goalId: Long)

    @Query("UPDATE goal_milestones SET isCompleted = :completed WHERE id = :id")
    suspend fun toggleMilestoneCompletion(id: Long, completed: Boolean)
}

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY isCompletedToday ASC, id ASC")
    fun getAllRoutines(): Flow<List<RoutineItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineItem): Long

    @Update
    suspend fun updateRoutine(routine: RoutineItem)

    @Delete
    suspend fun deleteRoutine(routine: RoutineItem)

    @Query("UPDATE routines SET isCompletedToday = :completed WHERE id = :id")
    suspend fun toggleRoutineCompletion(id: Long, completed: Boolean)

    @Query("UPDATE routines SET alarmEnabled = :enabled WHERE id = :id")
    suspend fun toggleAlarm(id: Long, enabled: Boolean)

    @Query("UPDATE routines SET isCompletedToday = 0")
    suspend fun resetDailyRoutines()
}

@Dao
interface BlockedAppDao {
    @Query("SELECT * FROM blocked_apps ORDER BY timesBlockedToday DESC, appName ASC")
    fun getAllBlockedApps(): Flow<List<BlockedAppRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBlockedApp(app: BlockedAppRule)

    @Delete
    suspend fun deleteBlockedApp(app: BlockedAppRule)

    @Query("UPDATE blocked_apps SET isBlockActiveDuringFocus = :isActive WHERE packageName = :packageName")
    suspend fun toggleAppBlock(packageName: String, isActive: Boolean)

    @Query("UPDATE blocked_apps SET timesBlockedToday = timesBlockedToday + 1 WHERE packageName = :packageName")
    suspend fun recordBlockAttempt(packageName: String)
}

@Dao
interface StudyAppDao {
    @Query("SELECT * FROM designated_study_apps ORDER BY isEnabled DESC, appName ASC")
    fun getAllStudyApps(): Flow<List<com.example.data.model.DesignatedStudyApp>>

    @Query("SELECT * FROM designated_study_apps WHERE isEnabled = 1")
    suspend fun getEnabledStudyAppsSync(): List<com.example.data.model.DesignatedStudyApp>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStudyApp(app: com.example.data.model.DesignatedStudyApp)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyApps(apps: List<com.example.data.model.DesignatedStudyApp>)

    @Query("UPDATE designated_study_apps SET isEnabled = :isEnabled WHERE packageName = :packageName")
    suspend fun toggleStudyApp(packageName: String, isEnabled: Boolean)

    @Delete
    suspend fun deleteStudyApp(app: com.example.data.model.DesignatedStudyApp)

    @Query("DELETE FROM designated_study_apps WHERE packageName = :packageName")
    suspend fun deleteStudyAppByPackage(packageName: String)
}

@Dao
interface StudyHistoryDao {
    @Query("SELECT * FROM study_history ORDER BY timestampMillis DESC")
    fun getAllHistory(): Flow<List<com.example.data.model.StudyHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: com.example.data.model.StudyHistoryItem): Long

    @Delete
    suspend fun deleteHistory(item: com.example.data.model.StudyHistoryItem)

    @Query("DELETE FROM study_history")
    suspend fun clearAllHistory()
}

@Dao
interface JournalOpinionDao {
    @Query("SELECT * FROM journal_opinions ORDER BY timestampMillis DESC")
    fun getAllOpinions(): Flow<List<com.example.data.model.JournalOpinionItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpinion(item: com.example.data.model.JournalOpinionItem): Long

    @Update
    suspend fun updateOpinion(item: com.example.data.model.JournalOpinionItem)

    @Delete
    suspend fun deleteOpinion(item: com.example.data.model.JournalOpinionItem)
}

@Dao
interface CustomInstructionDao {
    @Query("SELECT * FROM custom_instructions ORDER BY id DESC")
    fun getAllInstructions(): Flow<List<com.example.data.model.CustomInstructionItem>>

    @Query("SELECT * FROM custom_instructions WHERE isActive = 1 ORDER BY priorityLevel ASC, id DESC")
    fun getActiveInstructions(): Flow<List<com.example.data.model.CustomInstructionItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstruction(item: com.example.data.model.CustomInstructionItem): Long

    @Update
    suspend fun updateInstruction(item: com.example.data.model.CustomInstructionItem)

    @Delete
    suspend fun deleteInstruction(item: com.example.data.model.CustomInstructionItem)

    @Query("UPDATE custom_instructions SET isActive = :isActive WHERE id = :id")
    suspend fun toggleInstructionActive(id: Long, isActive: Boolean)
}

