package com.codinginflow.mvvmtodo.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat

@Entity(tableName = "task_table")
@Parcelize
data class Task(
    private val taskName : String,
    private val important : Boolean = false,
    private val isCompleted : Boolean = false,
    private val dateCreated : Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true)
    private val id : Int = 0) : Parcelable {

    fun getId() = id
    fun getTaskName() = taskName
    fun getImportant() = important
    fun getIsCompleted() = isCompleted
    fun getDateCreated() = dateCreated
    fun getDate() : String{
        return DateFormat.getDateTimeInstance().format(dateCreated)
    }
}