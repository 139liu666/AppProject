package com.peizhe.todo.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.peizhe.todo.R
import com.peizhe.todo.databinding.ItemTaskBinding

class TaskListAdapter : ListAdapter<Task, TaskListAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskViewHolder {
        //val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        //return TaskViewHolder(itemView)
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: TaskViewHolder,
        position: Int
    ) {
        val task=getItem(position)
        holder.bind(task)
    }

    inner class TaskViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        //val textViewTitle: TextView=itemView.findViewById(R.id.task_title)
        //val textViewDescription: TextView=itemView.findViewById(R.id.task_description)
        fun bind(task: Task) {
            binding.taskTitle.text=task.title
            binding.taskDescription.text=task.description
        }
    }
}
class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem == newItem
    }
}