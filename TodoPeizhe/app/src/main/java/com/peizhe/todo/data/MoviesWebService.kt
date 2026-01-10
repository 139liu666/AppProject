package com.peizhe.todo.data

import com.peizhe.todo.list.AccountDetails
import com.peizhe.todo.list.CreateSessionBody
import com.peizhe.todo.list.FavoriteBody
import com.peizhe.todo.list.ListResponse
import com.peizhe.todo.list.LoginBody
import com.peizhe.todo.list.RequestTokenResponse
import com.peizhe.todo.list.SessionResponse
import com.peizhe.todo.list.WatchlistBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MoviesWebService {
    //Movies
    @GET("3/movie/popular")
    suspend fun fetchMovies(
        @Query("language") lang: String = "en-US",
        @Query("page") page: Int
    ): Response<ListResponse>
    //TV
    @GET("3/tv/popular")
    suspend fun fetchTVs(
        @Query("language") lang: String = "en-US",
        @Query("page") page: Int
    ): Response<ListResponse>
    @GET("3/search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("language") lang: String = "en-US",
        @Query("page") page: Int = 1
    ): Response<ListResponse>


    @GET("3/account/{account_id}/favorite/movies")
    suspend fun getFavoriteMovies(
        @Path("account_id") accountId: Int,
        @Query("page") page: Int = 1
    ): Response<ListResponse>
    @GET("3/account/{account_id}/favorite/tv")
    suspend fun getFavoriteTV(
        @Path("account_id") accountId: Int,
        @Query("page") page: Int = 1
    ): Response<ListResponse>
    @POST("3/account/{account_id}/favorite")
    suspend fun markAsFavorite(
        @Path("account_id") accountId: Int,
        @Body body: FavoriteBody
    ): Response<Unit>


    @GET("3/account/{account_id}/watchlist/movies")
    suspend fun getWatchlistMovies(
        @Path("account_id") accountId: Int,
        @Query("page") page: Int = 1
    ): Response<ListResponse>
    @GET("3/account/{account_id}/watchlist/tv")
    suspend fun getWatchlistTV(
        @Path("account_id") accountId: Int,
        @Query("page") page: Int = 1
    ): Response<ListResponse>
    @POST("3/account/{account_id}/watchlist")
    suspend fun addToWatchlist(
        @Path("account_id") accountId: Int,
        @Body body: WatchlistBody
    ): Response<Unit>

    @GET("3/authentication/token/new")
    suspend fun createRequestToken(): Response<RequestTokenResponse>

    @POST("3/authentication/session/new")
    suspend fun createSession(@Body body: CreateSessionBody): Response<SessionResponse>

    @GET("3/account")
    suspend fun getAccountDetails(): Response<AccountDetails>

    @POST("3/authentication/token/validate_with_login")
    suspend fun validateWithLogin(@Body body: LoginBody): Response<RequestTokenResponse>
}