package com.codinginflow.mvvmtodo.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codinginflow.mvvmtodo.R
import com.codinginflow.mvvmtodo.data.SortOrder
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskAdapter
import com.codinginflow.mvvmtodo.databinding.FragmentTaskBinding
import com.codinginflow.mvvmtodo.util.OnQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_task.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskFragment : Fragment(R.layout.fragment_task),TaskAdapter.OnItemClickListener{
    private lateinit var searchView: SearchView
    private val viewmodel : TasksViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        val binding = FragmentTaskBinding.bind(view)
        val taskAdapter = TaskAdapter(this)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewmodel.taskEvent.collect { event ->
                when(event){
                    is TasksViewModel.TaskEvent.ShowUndoDeleteMessage ->{
                        Snackbar.make(requireView(),"Task Deleted",Snackbar.LENGTH_LONG)
                            .setAction("UNDO"){
                                viewmodel.onUndoDeleteClick(event.task)
                            }
                            .show()
                    }
                    is TasksViewModel.TaskEvent.NavigateToAddTask ->{
                        val action = TaskFragmentDirections.actionTaskFragmentToAddTaskFragment(title = "Add Task")
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TaskEvent.NavigateToEditTask ->{
                        val action = TaskFragmentDirections.actionTaskFragmentToAddTaskFragment(event.task,"Edit Task")
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TaskEvent.ShowTaskSavedMessage -> {
                        Snackbar.make(requireView(),event.message,Snackbar.LENGTH_LONG).show()
                    }
                    is TasksViewModel.TaskEvent.NavigateToDeleteAllCompleted -> {
                        val action = TaskFragmentDirections.actionGlobalDeleteAllCompleted()
                        findNavController().navigate(action)
                    }
                }
            }
        }
        binding.apply {
            recyclerViewTask.apply {
                adapter = taskAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean { return false }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                   val task = taskAdapter.currentList[viewHolder.adapterPosition]
                    viewmodel.onSwiped(task)
                }


            }).attachToRecyclerView(recyclerViewTask)

            fabAddTask.setOnClickListener {
                viewmodel.onAddNewTaskClick()
            }

        }
        setFragmentResultListener("add_edit_request"){_,bundle ->
            val result = bundle.getInt("add_edit_result")
            viewmodel.onAddEditResult(result)

        }
        viewmodel.tasks.observe(viewLifecycleOwner){
            taskAdapter.submitList(it)

            if(it.isEmpty())
                text_no_task.visibility = View.VISIBLE
            else {
                text_no_task.visibility = View.INVISIBLE
            }
        }
    }

    override fun onItemClick(task: Task) {
        viewmodel.onTaskSelected(task)
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        viewmodel.onTaskCompletedChanged(task,isChecked)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_task,menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView
        val pendingQuery = viewmodel.searchQuery.value
        if(pendingQuery!=null && pendingQuery.isNotEmpty()){
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery,false)
        }
        searchView.OnQueryTextChanged {
           viewmodel.searchQuery.value = it
        }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed_task).isChecked =
                    viewmodel.preferenceFlow.first().hideComplted
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_sort_by_name ->{
                viewmodel.onSortOrderSelected(SortOrder.NAME)
                true
            }
            R.id.action_sort_by_date ->{
                viewmodel.onSortOrderSelected(SortOrder.DATE)
                true
            }
            R.id.action_hide_completed_task ->{
                item.isChecked = !item.isChecked
                viewmodel.onHideCompletedSelected(item.isChecked)
                true
            }
            R.id.action_delete_all_completed_task ->{
                viewmodel.deleteAllCompletedTask()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        searchView.setOnQueryTextListener(null)
    }

}