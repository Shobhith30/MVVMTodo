package com.codinginflow.mvvmtodo.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.codinginflow.mvvmtodo.ADD_TASK_RESULT_OK
import com.codinginflow.mvvmtodo.EDIT_TASK_RESULT_OK
import com.codinginflow.mvvmtodo.data.PreferenceManager
import com.codinginflow.mvvmtodo.data.SortOrder
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferenceManager: PreferenceManager,
    @Assisted private val state : SavedStateHandle
) : ViewModel() {

    val searchQuery = state.getLiveData("searchQuery","")
    val preferenceFlow = preferenceManager.preferences
    private val taskEventChannel = Channel<TaskEvent>()
    val taskEvent = taskEventChannel.receiveAsFlow()

    private val taskFlow = combine(
        searchQuery.asFlow(),
       preferenceFlow
    ){query,prefernceFlow->

        Pair(query,prefernceFlow)

    }.flatMapLatest { (query,preferenceFlow)->
        taskDao.getTask(query,preferenceFlow.sortOrder,preferenceFlow.hideComplted)
    }
    var tasks = taskFlow.asLiveData()

    fun onSortOrderSelected(sortOrder: SortOrder){
        viewModelScope.launch(Dispatchers.IO) {
            preferenceManager.updateSortOrder(sortOrder)
        }
    }
    fun onHideCompletedSelected(hideCompleted : Boolean){
        viewModelScope.launch {
            preferenceManager.updateHideCompleted(hideCompleted)
        }
    }

    fun deleteAllCompletedTask(){
        viewModelScope.launch {
            taskEventChannel.send(TaskEvent.NavigateToDeleteAllCompleted)
        }
    }

    fun onTaskSelected(task: Task) {
        viewModelScope.launch {
            taskEventChannel.send(TaskEvent.NavigateToEditTask(task))
        }
    }

    fun onTaskCompletedChanged(task: Task, checked: Boolean) {
       viewModelScope.launch {
           taskDao.updateTask(task.copy(isCompleted = checked))
       }
    }

    fun onSwiped(task: Task) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
            taskEventChannel.send(TaskEvent.ShowUndoDeleteMessage(task))
        }
    }

    fun onUndoDeleteClick(task: Task) {
        viewModelScope.launch {
            taskDao.addTask(task)
        }
    }

    fun onAddNewTaskClick() {
        viewModelScope.launch {
            taskEventChannel.send(TaskEvent.NavigateToAddTask)
        }
    }

    fun onAddEditResult(result: Int) {
        when(result) {
            ADD_TASK_RESULT_OK -> showTaskSavedMessage("New Task Added")
            EDIT_TASK_RESULT_OK -> showTaskSavedMessage("Task Updated")
        }
    }

    private fun showTaskSavedMessage(message: String) {
        viewModelScope.launch {
            taskEventChannel.send(TaskEvent.ShowTaskSavedMessage(message))
        }
    }

    sealed class TaskEvent{
        object NavigateToAddTask : TaskEvent()
        data class NavigateToEditTask(val task: Task) : TaskEvent()
        data class ShowUndoDeleteMessage(val task : Task) : TaskEvent()
        data class ShowTaskSavedMessage(val message : String) : TaskEvent()
        object NavigateToDeleteAllCompleted : TaskEvent()
    }

}

