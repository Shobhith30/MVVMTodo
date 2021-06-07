package com.codinginflow.mvvmtodo.data

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codinginflow.mvvmtodo.databinding.ItemTaskBinding

class TaskAdapter(private val listener : OnItemClickListener) : ListAdapter<Task,TaskAdapter.TaskViewHolder>(DIFF_CALLBACK) {

    inner class TaskViewHolder(private val binding : ItemTaskBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(task : Task){
            binding.apply {
                checkboxCompleted.isChecked = task.getIsCompleted()
                textName.text = task.getTaskName()
                textName.paint.isStrikeThruText = task.getIsCompleted()
                imagePriority.isVisible = task.getImportant()
            }
        }
        init {
            binding.apply {
                checkboxCompleted.setOnClickListener {
                    val position = adapterPosition
                    if(position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onCheckBoxClick(task,checkboxCompleted.isChecked)
                    }
                }
                root.setOnClickListener {
                    val position = adapterPosition
                    if(position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onItemClick(task)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        if(task != null){
            holder.bind(task)
        }
    }

    interface OnItemClickListener{
        fun onItemClick(task : Task)
        fun onCheckBoxClick(task: Task,isChecked : Boolean)
    }

    companion object{
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Task>(){
            override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem.getId() == newItem.getId()
            }

            override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
               return oldItem == newItem
            }

        }
    }
}