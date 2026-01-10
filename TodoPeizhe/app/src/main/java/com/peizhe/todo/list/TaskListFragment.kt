package com.peizhe.todo.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.peizhe.todo.R
import com.peizhe.todo.databinding.FragmentTaskListBinding
import java.util.UUID


class TaskListFragment : Fragment() {
    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!
    private var taskList = listOf(
        Task(id = "id_1", title = "Task 1", description = "description 1"),
        Task(id = "id_2", title = "Task 2"),
        Task(id = "id_3", title = "Task 3")
    )
    private val adapter = TaskListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        adapter.submitList(taskList)
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerview.adapter=adapter
        //val fab=view.findViewById<FloatingActionButton>(R.id.floatingActionButton)
        binding.floatingActionButton.setOnClickListener {
            val newTask=Task(
                UUID.randomUUID().toString(),
                title= "Task ${taskList.size + 1}"
            )
            taskList=taskList+newTask
            adapter.submitList(taskList)
        }
    }
}

