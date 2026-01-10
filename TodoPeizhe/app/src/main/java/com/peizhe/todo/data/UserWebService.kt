package com.peizhe.todo.data

import retrofit2.Response
import retrofit2.http.GET

interface UserWebService {
    @GET("/sync/v9/user/")
    suspend fun fetchUser(): Response<User>
}