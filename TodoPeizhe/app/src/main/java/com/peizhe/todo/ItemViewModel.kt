package com.peizhe.todo

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peizhe.todo.data.MovieApi
import com.peizhe.todo.data.UserPreference
import com.peizhe.todo.list.CreateSessionBody
import com.peizhe.todo.list.FavoriteBody
import com.peizhe.todo.list.Item
import com.peizhe.todo.list.LoginBody
import com.peizhe.todo.list.WatchlistBody
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ItemsViewModel(application: Application): AndroidViewModel(application) {
    private var currentPage = 1
    var isLoading = false
    var canLoadMore = true
    var isSearching = false
    private val webService = MovieApi.moviesWebService
    val itemsStateFlow = MutableStateFlow<List<Item>>(emptyList())
    val currentTab = MutableStateFlow(0)
    private var searchJob: Job? = null

    private var myAccountId:Int = 22556775

    val loginError = MutableStateFlow<String?>(null)
    var currentScreen = "Home"
    val favMoviesStateFlow = MutableStateFlow<List<Item>>(emptyList())
    val favTVStateFlow = MutableStateFlow<List<Item>>(emptyList())

    val watchedMoviesStateFlow = MutableStateFlow<List<Item>>(emptyList())
    val watchedTVStateFlow = MutableStateFlow<List<Item>>(emptyList())

    val isLoggedIn = MutableStateFlow(false)
    val loginUrl = MutableStateFlow<String?>(null)

    private val userPrefs = UserPreference(application)

    init {
        checkLoginStatus()
    }
    private fun checkLoginStatus() {
        viewModelScope.launch {
            userPrefs.userData.collect { (sessionId, accountId) ->
                if (!sessionId.isNullOrEmpty() && accountId != 0) {
                    MovieApi.sessionId = sessionId
                    myAccountId = accountId
                    isLoggedIn.value = true

                    if (itemsStateFlow.value.isEmpty()) {
                        loadMovies()
                        refreshFavorites()
                        refreshWatchlist()
                    }
                } else {
                    isLoggedIn.value = false
                }
            }
        }
    }

    fun loginWithPassword(username: String, pass: String) {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            loginError.value = null

            try {
                // 1.Request Token
                val tokenResp = webService.createRequestToken()
                val requestToken = tokenResp.body()?.requestToken

                if (tokenResp.isSuccessful && requestToken != null) {
                    val loginBody = LoginBody(username, pass, requestToken)
                    val validateResp = webService.validateWithLogin(loginBody)

                    if (validateResp.isSuccessful && validateResp.body()?.success == true) {
                        val sessionResp = webService.createSession(CreateSessionBody(requestToken))
                        val sessionId = sessionResp.body()?.sessionId

                        if (sessionResp.isSuccessful && sessionId != null) {
                            MovieApi.sessionId = sessionId

                            val accountResp = webService.getAccountDetails()
                            if (accountResp.isSuccessful) {
                                myAccountId = accountResp.body()?.id ?: 0
                                isLoggedIn.value = true
                                userPrefs.saveUser(sessionId, myAccountId)
                                loadMovies()
                                refreshFavorites()
                                refreshWatchlist()
                            }
                        } else {
                            loginError.value = "Failed to create session"
                        }
                    } else {
                        loginError.value = "Invalid username or password"
                    }
                } else {
                    loginError.value = "Network error"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                loginError.value = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    fun startLogin() {
        viewModelScope.launch {
            try {
                val response = webService.createRequestToken()
                if (response.isSuccessful) {
                    val token = response.body()?.requestToken
                    if (token != null) {
                        val redirectUrl = "tmdbapp://redirect"
                        loginUrl.value = "https://www.themoviedb.org/authenticate/$token?redirect_to=$redirectUrl"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPrefs.clearUser()
            MovieApi.sessionId = null
            myAccountId = 0
            isLoggedIn.value = false
            itemsStateFlow.value = emptyList()
            favMoviesStateFlow.value = emptyList()
        }
    }
    fun handleLoginCallback(requestToken: String) {
        viewModelScope.launch {
            try {
                val sessionResp = webService.createSession(CreateSessionBody(requestToken))
                if (sessionResp.isSuccessful) {
                    val sessionId = sessionResp.body()?.sessionId
                    if (sessionId != null) {
                        MovieApi.sessionId = sessionId

                        val accountResp = webService.getAccountDetails()
                        if (accountResp.isSuccessful) {
                            myAccountId = accountResp.body()?.id ?: 0
                            isLoggedIn.value = true

                            loadMovies()
                            refreshFavorites()
                            refreshWatchlist()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun toggleFavorite(item: Item) {
        viewModelScope.launch {
            val isMovie = item.title != null
            val mediaType = if (isMovie) "movie" else "tv"

            val currentList = if (isMovie) favMoviesStateFlow.value else favTVStateFlow.value
            val isCurrentlyFav = currentList.any { it.id == item.id }

            val body = FavoriteBody(
                mediaType = mediaType,
                mediaId = item.id,
                isFavorite = !isCurrentlyFav
            )

            try {
                val response = webService.markAsFavorite(myAccountId, body)
                if (response.isSuccessful) {
                    refreshFavorites()
                    Log.d("API", "Toggle Favorite Success: ${!isCurrentlyFav}")
                } else {
                    Log.e("API", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleWatchlist(item: Item) {
        viewModelScope.launch {
            val isMovie = item.title != null
            val mediaType = if (isMovie) "movie" else "tv"

            val currentList = if (isMovie) watchedMoviesStateFlow.value else watchedTVStateFlow.value
            val isCurrentlyWatched = currentList.any { it.id == item.id }

            val body = WatchlistBody(
                mediaType = mediaType,
                mediaId = item.id,
                isWatchlist = !isCurrentlyWatched
            )

            try {
                val response = webService.addToWatchlist(myAccountId, body)
                if (response.isSuccessful) {
                    refreshWatchlist()
                    Log.d("API", "Toggle Watchlist Success: ${!isCurrentlyWatched}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setScreen(screenLabel: String) {
        currentScreen = screenLabel
        when (currentScreen) {
            "Home" -> {
                if (itemsStateFlow.value.isEmpty()) {
                    loadMovies(isLoadMore = false)
                }
            }
            "Favorites" -> refreshFavorites()
            "Watched" -> refreshWatchlist()
        }
    }
    fun refreshFavorites() {
        viewModelScope.launch {
            try {
                val moviesResp = webService.getFavoriteMovies(myAccountId)
                if (moviesResp.isSuccessful) {
                    favMoviesStateFlow.value = moviesResp.body()?.results ?: emptyList()
                }

                val tvResp = webService.getFavoriteTV(myAccountId)
                if (tvResp.isSuccessful) {
                    favTVStateFlow.value = tvResp.body()?.results ?: emptyList()
                }
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun refreshWatchlist() {
        viewModelScope.launch {
            try {
                val moviesResp = webService.getWatchlistMovies(myAccountId)
                if (moviesResp.isSuccessful) {
                    watchedMoviesStateFlow.value = moviesResp.body()?.results ?: emptyList()
                }

                val tvResp = webService.getWatchlistTV(myAccountId)
                if (tvResp.isSuccessful) {
                    watchedTVStateFlow.value = tvResp.body()?.results ?: emptyList()
                }
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun search(query: String) {
        searchJob?.cancel()

        if (query.isBlank()) {
            isSearching = false
            itemsStateFlow.value = emptyList()
            loadMovies(isLoadMore = false)
            return
        }
        isSearching = true
        searchJob = viewModelScope.launch {
                val response = webService.searchMulti(query = query)
                if (response.isSuccessful) {
                    val allResults = response.body()?.results ?: emptyList()
                    itemsStateFlow.value = allResults
                }
        }
    }

    fun loadMovies(isLoadMore: Boolean = false) {
        if (isLoading) return
        if (currentScreen != "Home") return
        if (isSearching) return

        viewModelScope.launch {
            isLoading = true

            if (isLoadMore) {
                currentPage++
            } else {
                currentPage = 1
                canLoadMore = true
            }

            try {
                val response = if (currentTab.value == 0) {
                    webService.fetchMovies(page = currentPage)
                } else {
                    webService.fetchTVs(page = currentPage)
                }

                if (response.isSuccessful) {
                    val newItems = response.body()?.results ?: emptyList()

                    if (newItems.isEmpty()) {
                        canLoadMore = false
                    }

                    if (isLoadMore) {
                        itemsStateFlow.value = itemsStateFlow.value + newItems
                    } else {
                        itemsStateFlow.value = newItems
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
    fun switchTab(index: Int) {
        currentTab.value = index
        when (currentScreen) {
            "Home" -> {
                itemsStateFlow.value = emptyList()
                loadMovies(isLoadMore = false)
            }
            "Favorites" -> {
                refreshFavorites()
            }
            "Watched" -> {
                refreshWatchlist()
            }
        }
    }
    /*
    fun freshMovies() {
        viewModelScope.launch {
            try {
                val response = webService.fetchMovies(page=1)
                if (response.isSuccessful) {
                    itemsStateFlow.value = response.body()?.results ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun freshTVs() {
        viewModelScope.launch {
            try {
                val response = webService.fetchTVs(page=1)
                if (response.isSuccessful) {
                    itemsStateFlow.value = response.body()?.results ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    */
}