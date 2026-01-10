package com.peizhe.todo

import android.util.Log
import android.util.Log.e
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peizhe.todo.data.Api
import com.peizhe.todo.list.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class TaskListViewModel: ViewModel() {
    private val webService = Api.taskWebService

    public val tasksStateFlow = MutableStateFlow<List<Task>>(emptyList())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val response = webService.fetchTasks() // Call HTTP (opération longue)
            if (!response.isSuccessful) { // à cette ligne, on a reçu la réponse de l'API
                Log.e("Network", "Error: ${response.message()}")
                return@launch
            }
            val fetchedTasks = response.body()!!
            tasksStateFlow.value = fetchedTasks // on modifie le flow, ce qui déclenche ses observers
        }
    }

    // à compléter plus tard:
    fun add(task: Task) {
        viewModelScope.launch {
                val response = webService.create(task)
                if (!response.isSuccessful) {
                    Log.e("Network", "Error creating: ${response.message()}")
                    return@launch
                }
                // API 会返回创建好的任务（包含服务器生成的 ID）
                val createdTask = response.body()!!

                // 更新本地列表：旧列表 + 新任务
                tasksStateFlow.value = tasksStateFlow.value + createdTask
            }
    }
    fun edit(task: Task) {
        viewModelScope.launch {
                // 调用 API 更新服务器数据
                val response = webService.update(task)

                if (!response.isSuccessful) {
                    Log.e("Network", "Error updating: ${response.message()}")
                    return@launch
                }

                // 获取服务器返回的最新任务数据
                val updatedTask = response.body()!!

                // 更新本地列表：
                // 遍历当前列表，如果 ID 匹配，就替换成新的 updatedTask，否则保持原样
                val updatedList = tasksStateFlow.value.map {
                    if (it.id == updatedTask.id) updatedTask else it
                }
            tasksStateFlow.value = updatedList
        }
    }
    fun remove(task: Task) {
        viewModelScope.launch {
                // 调用 API 删除
                val response = webService.delete(task.id)
                if (!response.isSuccessful) {
                    Log.e("Network", "Error deleting: ${response.message()}")
                    return@launch
                }

                // 更新本地列表：保留那些 ID 不等于被删除 ID 的任务
                tasksStateFlow.value = tasksStateFlow.value.filter { it.id != task.id }
        }
    }
}