package com.example.todo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.peizhe.todo.list.Task

@Composable
fun TaskItem(
    task: Task,
    onClick: (Task) -> Unit, // 点击回调
    onDelete: () -> Unit     // 删除回调
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(task) } // 点击整个行触发
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = task.title, style = MaterialTheme.typography.titleMedium)
            Text(text = task.description, style = MaterialTheme.typography.bodySmall)
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}