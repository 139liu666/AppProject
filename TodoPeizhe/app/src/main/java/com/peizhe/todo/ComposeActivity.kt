package com.peizhe.todo

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todo.ListScreen
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.peizhe.todo.data.Api
import com.peizhe.todo.list.Task
import com.peizhe.todo.ui.theme.TodoPeizheTheme
import kotlinx.coroutines.launch
import java.util.UUID

data object ListNavScreen
data class DetailNavScreen(val task: Task)

class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodoPeizheTheme {
                App()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val backStack = remember { mutableStateListOf<Any>(ListNavScreen) }

    var userName by remember { mutableStateOf("Loading user info...") }

    LaunchedEffect(Unit) {
        try {
            val userResponse = Api.userWebService.fetchUser()
            val user = userResponse.body()!!
            userName = "Hello, ${user.name}!"
        } catch (e: Exception) {
            userName = "Error: ${e.message}"
            e.printStackTrace()
        }
    }
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Todo List $userName") },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Go to classic app"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        val currentScreen = backStack.lastOrNull()
        when (currentScreen) {
            is ListNavScreen -> {
                val taskListViewModel: TaskListViewModel = viewModel()
                ListScreen(
                    viewModel = taskListViewModel,
                    modifier = Modifier.padding(innerPadding), 
                    onAddTask = { taskListViewModel.add(it) },
                    onDeleteTask = { taskListViewModel.remove(it) },
                    onNavigateToDetail = { task ->
                        backStack.add(DetailNavScreen(task))
                    }
                )
            }
            is DetailNavScreen -> {
                Surface(modifier = Modifier.padding(innerPadding)) {
                    DetailScreen(
                        task = currentScreen.task,
                        onBack = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
            }
        }
    }
}

