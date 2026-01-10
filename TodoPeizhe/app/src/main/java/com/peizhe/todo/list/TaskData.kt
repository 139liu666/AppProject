package com.peizhe.todo.list

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Task(
    @SerialName("id")
    val id: String,
    @SerialName("content")
    val title: String,
    // 给 description 设置默认值，这样创建对象时可以不传这个参数
    @SerialName("description")
    val description: String = "default"
): java.io.Serializable