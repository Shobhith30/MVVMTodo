package com.codinginflow.mvvmtodo.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.mvvmtodo.ADD_TASK_RESULT_OK
import com.codinginflow.mvvmtodo.EDIT_TASK_RESULT_OK
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddEditTaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @Assisted private val state : SavedStateHandle
)  : ViewModel() {

    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()

    val task = state.get<Task>("task")
    var taskName = state.get<String>("taskName")?:task?.getTaskName()?:""
        set(value) {
            field = value
            state.set("taskName",value)
        }
    var taskImportance = state.get<Boolean>("taskImportance")?:task?.getImportant()?:false
        set(value) {
            field = value
            state.set("taskImportance",value)
        }
    fun onSaveClick() {
        if(taskName.isBlank()){
            showInvalidMessage("Name Cannot be empty")
            return
        }
        if(task != null){
            val updatedTask = task.copy(taskName = taskName,important = taskImportance)
            updateTask(updatedTask)

        }else{
            val newTask = Task(taskName = taskName,important = taskImportance)
            createTask(newTask)
        }
    }

    private  fun showInvalidMessage(message: String) {
        viewModelScope.launch {
            addEditTaskEventChannel.send(AddEditTaskEvent.showInvalidInputMessage(message))
        }
    }

    private fun createTask(newTask: Task) {
        viewModelScope.launch {
            taskDao.addTask(newTask)
            addEditTaskEventChannel.send(AddEditTaskEvent.NavigateWithResult(ADD_TASK_RESULT_OK))
        }
    }

    private fun updateTask(updatedTask: Task) {
        viewModelScope.launch {
            taskDao.updateTask(updatedTask)
            addEditTaskEventChannel.send(AddEditTaskEvent.NavigateWithResult(EDIT_TASK_RESULT_OK))
        }
    }

    sealed class AddEditTaskEvent{
        data class showInvalidInputMessage(val msg : String) : AddEditTaskEvent()
        data class NavigateWithResult(val result : Int) : AddEditTaskEvent()
    }

}