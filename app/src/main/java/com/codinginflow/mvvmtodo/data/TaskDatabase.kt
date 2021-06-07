package com.codinginflow.mvvmtodo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codinginflow.mvvmtodo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class],version = 1)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun getTaskDao() : TaskDao

    class Callback @Inject constructor(
        private val database : Provider<TaskDatabase>,
        @ApplicationScope private val applicationScope : CoroutineScope
    ) : RoomDatabase.Callback(){
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val dao = database.get().getTaskDao()
            applicationScope.launch {
                dao.addTask(Task("Clean the room"))
                dao.addTask(Task("Wash Clothes"))
                dao.addTask(Task("Call jim",isCompleted = true))
                dao.addTask(Task("Repair bike",true))
            }

        }
    }
}