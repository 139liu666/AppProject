package com.example.todo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.peizhe.todo.TaskListViewModel
import com.peizhe.todo.list.Task
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun ListScreen(
    viewModel: TaskListViewModel,
    onAddTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onNavigateToDetail: (Task) -> Unit, // 新增：导航回调
    modifier: Modifier = Modifier // 接收外部传入的 modifier (主要是 padding)
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val taskList by viewModel.tasksStateFlow.collectAsStateWithLifecycle()

    Scaffold(
        // TopBar 已经被移除了
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val newTask = Task(
                        id = UUID.randomUUID().toString(),
                        title = "Task ${taskList.size}",
                        description = "Desc ${taskList.size}"
                    )
                    onAddTask(newTask)
                    coroutineScope.launch {
                        listState.animateScrollToItem(taskList.size-1)
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = modifier.padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(taskList) { task ->
                TaskItem(
                    task = task,
                    onClick = { onNavigateToDetail(task) }, // 点击时调用
                    onDelete = { onDeleteTask(task) }
                )
            }
        }
    }
}