package com.codinginflow.mvvmtodo.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    fun getTask(search: String, sortOrder: SortOrder, hideCompleted: Boolean): Flow<List<Task>> {
        return when (sortOrder) {
            SortOrder.DATE -> getTaskSortedByDate(search, hideCompleted)
            SortOrder.NAME -> getTaskSortedByName(search, hideCompleted)
        }
    }

    @Query("SELECT * FROM task_table WHERE (isCompleted != :hideCompleted OR isCompleted = 0) AND taskName LIKE '%'||:search||'%' ORDER BY important DESC,taskName")
    fun getTaskSortedByName(search: String, hideCompleted: Boolean): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE (isCompleted != :hideCompleted OR isCompleted = 0) AND taskName LIKE '%'||:search||'%' ORDER BY important DESC,dateCreated DESC")
    fun getTaskSortedByDate(search: String, hideCompleted: Boolean): Flow<List<Task>>

    @Query("DELETE FROM task_table WHERE isCompleted = 1")
    suspend fun deleteAllCompletedTask()
}