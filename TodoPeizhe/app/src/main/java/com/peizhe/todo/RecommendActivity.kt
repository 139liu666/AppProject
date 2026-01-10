package com.peizhe.todo

import android.R.attr.name
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Bundle
import android.util.Log.w
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peizhe.todo.data.MovieApi
import com.peizhe.todo.list.Item
import com.peizhe.todo.ui.theme.TodoPeizheTheme

data object ItemsNavScreen
data class DetailItemScreen(val item: Item)
enum class AppScreen(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Default.Home),
    Favorites("Favorites", Icons.Default.Star),
    Watched("Watched", Icons.Default.CheckCircle)
}
class RecommendActivity : ComponentActivity() {
    private var mainViewModel: ItemsViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodoPeizheTheme {
                val viewModel: ItemsViewModel = viewModel()
                mainViewModel = viewModel

                LaunchedEffect(Unit) {
                    handleIntent(intent)
                }

                val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
                val loginUrl by viewModel.loginUrl.collectAsStateWithLifecycle()

                LaunchedEffect(loginUrl) {
                    loginUrl?.let { url ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    }
                }
                if(isLoggedIn){
                    MyApp(viewModel)
                }else{
                    LoginScreen(viewModel, { viewModel.startLogin() })
                }
            }
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data
        if (uri != null && uri.scheme == "tmdbapp" && uri.host == "redirect") {
            val approved = uri.getQueryParameter("approved") == "true"
            val requestToken = uri.getQueryParameter("request_token")

            if (approved && requestToken != null) {
                mainViewModel?.handleLoginCallback(requestToken)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp(viewModel: ItemsViewModel) {
    val backStack = remember { mutableStateListOf<Any>(ItemsNavScreen) }
    val context = LocalContext.current
    val viewModel: ItemsViewModel = viewModel

    val items by viewModel.itemsStateFlow.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    var searchText by remember { mutableStateOf("") }

    val favMovies by viewModel.favMoviesStateFlow.collectAsStateWithLifecycle()
    val favTV by viewModel.favTVStateFlow.collectAsStateWithLifecycle()

    val watchedMovies by viewModel.watchedMoviesStateFlow.collectAsStateWithLifecycle()
    val watchedTV by viewModel.watchedTVStateFlow.collectAsStateWithLifecycle()

    var currentScreenType by remember { mutableStateOf(AppScreen.Home) }

    BackHandler(enabled = searchText.isNotEmpty()) {
        searchText = ""
        viewModel.search("")
    }
    Scaffold(
        topBar = {
            if (backStack.last() is ItemsNavScreen){
                Column {
                    if (currentScreenType == AppScreen.Home) {
                        TopAppBar(title = { Text("Movie Explorer") },
                                 actions = {
                                    IconButton(onClick = { viewModel.logout() }) {
                                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                                    }
                                 }
                        )

                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { newText ->
                                searchText = newText
                                viewModel.search(newText)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            placeholder = { Text("Search movies, TV shows...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            },
                            trailingIcon = {
                                if (searchText.isNotEmpty()) {
                                    IconButton(onClick = {
                                        searchText = ""
                                        viewModel.search("")
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium
                        )
                    }else{
                        TopAppBar(
                            title = { Text(currentScreenType.label) },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                        )
                    }
                    val showTabs = if (currentScreenType == AppScreen.Home) searchText.isBlank() else true

                    if (showTabs) {
                        TabRow(selectedTabIndex = currentTab) {
                            Tab(
                                selected = currentTab == 0,
                                onClick = {
                                    viewModel.switchTab(0)
                                },
                                text = { Text("Movies") },
                                icon = { Icon(Icons.Default.Movie, contentDescription = null) }
                            )
                            Tab(
                                selected = currentTab == 1,
                                onClick = {
                                    viewModel.switchTab(1)
                                },
                                text = { Text("TV Series") },
                                icon = { Icon(Icons.Default.Tv, contentDescription = null) }
                            )
                        }
                    } else if (currentScreenType == AppScreen.Home && searchText.isNotBlank()) {
                        Text(
                            text = "Search Results",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (backStack.lastOrNull() is ItemsNavScreen) {
                NavigationBar {
                    AppScreen.values().forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentScreenType == screen,
                            onClick = {
                                currentScreenType = screen
                                if (searchText.isNotEmpty()) {
                                    searchText = ""
                                    viewModel.search("")
                                }
                                viewModel.setScreen(screen.label)
                            }
                        )
                    }
                }
            }
        }
    ){innerPadding ->
        val currentScreen = backStack.lastOrNull()
        when(currentScreen){
            is ItemsNavScreen -> {
                val finalDisplayList = if (currentScreenType == AppScreen.Home) {
                    items
                } else {
                    when (currentScreenType) {
                        AppScreen.Favorites -> if (currentTab == 0) favMovies else favTV
                        AppScreen.Watched -> if (currentTab == 0) watchedMovies else watchedTV
                        else -> emptyList()
                    }
                }
                if (finalDisplayList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                        val msg = if (currentScreenType == AppScreen.Home) "Loading..." else "No items in ${currentScreenType.label}"
                        Text(text = msg, style = MaterialTheme.typography.bodyLarge)
                    }
                }else{
                    ItemGridScreen(
                        items = finalDisplayList,
                        viewModel=viewModel,
                        modifier = Modifier.padding(innerPadding),
                        onMovieClick = { item ->
                            backStack.add(DetailItemScreen(item))
                        }
                    )
                }
            }
            is DetailItemScreen -> {
                Surface(modifier = Modifier.padding(innerPadding)) {
                    ItemDetailScreen(
                        item = currentScreen.item,
                        viewModel=viewModel,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
            }
        }
    }
}
