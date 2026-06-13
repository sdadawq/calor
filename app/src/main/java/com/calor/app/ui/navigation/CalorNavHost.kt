package com.calor.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.calor.app.data.repository.CalorRepository
import com.calor.app.ui.screens.catalog.CatalogScreen
import com.calor.app.ui.screens.catalog.ProductFormScreen
import com.calor.app.ui.screens.dishes.CreateDishScreen
import com.calor.app.ui.screens.dishes.DishDetailScreen
import com.calor.app.ui.screens.dishes.DishesScreen
import com.calor.app.ui.screens.fridge.AddFridgeScreen
import com.calor.app.ui.screens.fridge.FridgeScreen
import com.calor.app.ui.screens.history.HistoryScreen
import com.calor.app.ui.screens.home.HomeScreen
import com.calor.app.ui.screens.onboarding.OnboardingScreen
import com.calor.app.ui.screens.quickeat.QuickEatSheet
import com.calor.app.ui.screens.settings.SettingsScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    repository: CalorRepository,
) : ViewModel() {
    val onboardingCompleted: StateFlow<Boolean> = repository.observeOnboardingCompleted()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
}

private val bottomTabs = listOf(
    Routes.HOME to ("Главная" to Icons.Default.Home),
    Routes.FRIDGE to ("Холодильник" to Icons.Default.Kitchen),
    Routes.DISHES to ("Блюда" to Icons.Default.Restaurant),
    Routes.CATALOG to ("Продукты" to Icons.Default.ShoppingBasket),
)

@Composable
fun CalorNavHost(
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val onboardingDone by appViewModel.onboardingCompleted.collectAsState()
    var showQuickEat by remember { mutableStateOf(false) }
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = bottomTabs.any { it.first == currentRoute }

    val startDestination = if (onboardingDone) Routes.HOME else Routes.ONBOARDING

    if (showQuickEat) {
        QuickEatSheet(onDismiss = { showQuickEat = false })
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomTabs.forEach { (route, labelIcon) ->
                        val (label, icon) = labelIcon
                        NavigationBarItem(
                            selected = currentRoute == route,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.HOME) {
                HomeScreen(
                    onQuickEat = { showQuickEat = true },
                    onHistory = { navController.navigate(Routes.HISTORY) },
                    onSettings = { navController.navigate(Routes.SETTINGS) },
                )
            }
            composable(Routes.FRIDGE) {
                FridgeScreen(onAdd = { navController.navigate(Routes.ADD_FRIDGE) })
            }
            composable(Routes.DISHES) {
                DishesScreen(
                    onCreate = { navController.navigate(Routes.CREATE_DISH) },
                    onOpen = { navController.navigate(Routes.dishDetail(it)) },
                )
            }
            composable(Routes.CATALOG) {
                CatalogScreen(
                    onAdd = { navController.navigate(Routes.ADD_PRODUCT) },
                    onEdit = { navController.navigate(Routes.editProduct(it)) },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.HISTORY) {
                HistoryScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.ADD_PRODUCT) {
                ProductFormScreen(productId = null, onBack = { navController.popBackStack() })
            }
            composable(
                route = Routes.EDIT_PRODUCT,
                arguments = listOf(navArgument("productId") { type = NavType.LongType }),
            ) { entry ->
                ProductFormScreen(
                    productId = entry.arguments?.getLong("productId"),
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.CREATE_DISH) {
                CreateDishScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Routes.DISH_DETAIL,
                arguments = listOf(navArgument("dishId") { type = NavType.LongType }),
            ) { entry ->
                DishDetailScreen(
                    dishId = entry.arguments?.getLong("dishId") ?: 0L,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.ADD_FRIDGE) {
                AddFridgeScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
