package com.peizhe.todo

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.peizhe.todo.data.MovieApi
import com.peizhe.todo.data.MoviesWebService
import com.peizhe.todo.data.UserPreference
import com.peizhe.todo.list.AccountDetails
import com.peizhe.todo.list.CreateSessionBody
import com.peizhe.todo.list.Item
import com.peizhe.todo.list.ListResponse
import com.peizhe.todo.list.LoginBody
import com.peizhe.todo.list.RequestTokenResponse
import com.peizhe.todo.list.SessionResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import retrofit2.Response

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UnitTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockApplication: Application
    private lateinit var mockWebService: MoviesWebService

    private lateinit var viewModel: ItemsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock Application Context
        mockApplication = mockk(relaxed = true)


        // A. Mock MovieApi
        mockkObject(MovieApi)
        mockWebService = mockk(relaxed = true)
        every { MovieApi.moviesWebService } returns mockWebService

        // B. Mock UserPreference
        mockkConstructor(UserPreference::class)

        every { anyConstructed<UserPreference>().userData } returns flowOf(Pair(null, 0))

        coEvery { anyConstructed<UserPreference>().saveUser(any<String>(), any<Int>()) } returns Unit
        coEvery { anyConstructed<UserPreference>().clearUser() } returns Unit

        viewModel = ItemsViewModel(mockApplication)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `init checks login status and stays logged out if no data`() = runTest {
        advanceUntilIdle()
        assertFalse(viewModel.isLoggedIn.value)
    }

    @Test
    fun `loginWithPassword success flow updates state and saves user`() = runTest {
        val username = "user"
        val password = "password"
        val requestToken = "token_123"
        val sessionId = "session_abc"
        val accountId = 12345

        // 1.Request Token
        coEvery { mockWebService.createRequestToken() } returns Response.success(
            RequestTokenResponse(true, "2024-12-31", requestToken)
        )

        // 2.Validate Login
        coEvery { mockWebService.validateWithLogin(any<LoginBody>()) } returns Response.success(
            RequestTokenResponse(true, "2024-12-31", requestToken)
        )

        // 3.Create Session
        coEvery { mockWebService.createSession(any<CreateSessionBody>()) } returns Response.success(
            SessionResponse(true, sessionId)
        )

        // 4.Get Account
        coEvery { mockWebService.getAccountDetails() } returns Response.success(
            AccountDetails(id = accountId, username = "testuser")
        )

        val emptyListResp = ListResponse(page = 1, results = emptyList(), totalPages = 1)

        coEvery { mockWebService.fetchMovies(any<String>(), any<Int>()) } returns Response.success(emptyListResp)

        coEvery { mockWebService.getFavoriteMovies(any<Int>(), any<Int>()) } returns Response.success(emptyListResp)
        coEvery { mockWebService.getWatchlistMovies(any<Int>(), any<Int>()) } returns Response.success(emptyListResp)

        coEvery { mockWebService.getFavoriteTV(any<Int>(), any<Int>()) } returns Response.success(emptyListResp)
        coEvery { mockWebService.getWatchlistTV(any<Int>(), any<Int>()) } returns Response.success(emptyListResp)


        viewModel.loginWithPassword(username, password)
        advanceUntilIdle()

        assertTrue(viewModel.isLoggedIn.value)
        assertNull(viewModel.loginError.value)
        assertEquals(sessionId, MovieApi.sessionId)

        coVerify { anyConstructed<UserPreference>().saveUser(sessionId, accountId) }
    }

    @Test
    fun `loginWithPassword failure on invalid credentials`() = runTest {
        val requestToken = "token_123"

        coEvery { mockWebService.createRequestToken() } returns Response.success(
            RequestTokenResponse(true, "expires", requestToken)
        )

        coEvery { mockWebService.validateWithLogin(any<LoginBody>()) } returns Response.success(
            RequestTokenResponse(false, "", "")
        )

        viewModel.loginWithPassword("user", "wrong_pass")
        advanceUntilIdle()

        assertFalse(viewModel.isLoggedIn.value)
        assertEquals("Invalid username or password", viewModel.loginError.value)

        coVerify(exactly = 0) { mockWebService.createSession(any<CreateSessionBody>()) }
    }

    @Test
    fun `loadMovies fetches data and updates stateFlow`() = runTest {
        val mockItems = listOf(
            Item(id = 1, title = "Movie A", overview = "Desc A"),
            Item(id = 2, title = "Movie B", overview = "Desc B", voteAverage = 8.5)
        )

        coEvery { mockWebService.fetchMovies(any<String>(), any<Int>()) } returns Response.success(
            ListResponse(page = 1, results = mockItems, totalPages = 10)
        )

        viewModel.switchTab(0)

        viewModel.loadMovies()
        advanceUntilIdle()

        assertEquals(2, viewModel.itemsStateFlow.value.size)
        assertEquals("Movie A", viewModel.itemsStateFlow.value[0].title)
        assertFalse(viewModel.isLoading)
    }

    @Test
    fun `search updates itemsStateFlow with results`() = runTest {
        val query = "Avatar"
        val searchResults = listOf(
            Item(id = 10, title = "Avatar", overview = "Blue people")
        )

        coEvery {
            mockWebService.searchMulti(
                query = any<String>(),
                lang = any<String>(),
                page = any<Int>()
            )
        } returns Response.success(
            ListResponse(page = 1, results = searchResults, totalPages = 1)
        )

        viewModel.search(query)
        advanceUntilIdle()

        assertTrue(viewModel.isSearching)
        assertEquals(1, viewModel.itemsStateFlow.value.size)
        assertEquals("Avatar", viewModel.itemsStateFlow.value[0].title)
    }

    @Test
    fun `logout clears user data and resets state`() = runTest {
        viewModel.isLoggedIn.value = true
        MovieApi.sessionId = "old_session"

        viewModel.logout()
        advanceUntilIdle()

        assertFalse(viewModel.isLoggedIn.value)
        assertNull(MovieApi.sessionId)

        coVerify { anyConstructed<UserPreference>().clearUser() }

        assertTrue(viewModel.itemsStateFlow.value.isEmpty())
        assertTrue(viewModel.favMoviesStateFlow.value.isEmpty())
    }
}