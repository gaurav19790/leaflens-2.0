package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.ui.CollectionScreen
import com.example.ui.HomeScreen
import com.example.ui.LoginScreen
import com.example.ui.ProfileScreen
import com.example.ui.ScannerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavBarBg
import com.example.ui.theme.SurfaceVariant
import com.example.ui.theme.TextPrimary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            com.stripe.android.PaymentConfiguration.init(this, "pk_test_TYooMQauvdEDq54NiTphI7jx")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        try {
            if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApiKey(BuildConfig.FIREBASE_API_KEY)
                    .setApplicationId(BuildConfig.FIREBASE_APP_ID)
                    .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                    .build()
                com.google.firebase.FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Exception) {
            // Ignore init errors
        }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = try { com.google.firebase.auth.FirebaseAuth.getInstance() } catch(e: Exception) { null }
    val isUserLoggedIn = auth?.currentUser != null
    
    NavHost(navController = navController, startDestination = if (isUserLoggedIn) "main" else "login") {
        composable("login") {
            LoginScreen(onLoginSuccess = {
                navController.navigate("main") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }
        composable("main") {
            MainScreen(rootNavController = navController)
        }
    }
}

@Composable
fun MainScreen(rootNavController: NavController) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = NavBarBg) {
                val navColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TextPrimary,
                    selectedTextColor = TextPrimary,
                    unselectedIconColor = TextPrimary.copy(alpha = 0.4f),
                    unselectedTextColor = TextPrimary.copy(alpha = 0.4f),
                    indicatorColor = SurfaceVariant
                )
                
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentDestination?.hierarchy?.any { it.route == "home" } == true,
                    onClick = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = navColors
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.AddCircle, contentDescription = "Scan") },
                    label = { Text("Scan") },
                    selected = currentDestination?.hierarchy?.any { it.route == "scan" } == true,
                    onClick = {
                        navController.navigate("scan") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = navColors
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Collection") },
                    label = { Text("Collection") },
                    selected = currentDestination?.hierarchy?.any { it.route == "dictionary" } == true,
                    onClick = {
                        navController.navigate("dictionary") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = navColors
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = currentDestination?.hierarchy?.any { it.route == "profile" } == true,
                    onClick = {
                        navController.navigate("profile") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = navColors
                )
                NavigationBarItem(
                    icon = { Text("✨", fontSize = 20.sp) },
                    label = { Text("AI") },
                    selected = currentDestination?.hierarchy?.any { it.route == "aichat" } == true,
                    onClick = {
                        navController.navigate("aichat") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = navColors
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToScan = { navController.navigate("scan") },
                    onNavigateToPlant = { plantName -> navController.navigate("plantProfile/$plantName") }
                )
            }
            composable("scan") {
                ScannerScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToPricing = { navController.navigate("pricing") }
                )
            }
            composable("dictionary") {
                CollectionScreen(
                    onPlantClick = { plant ->
                        navController.navigate("plantProfile/${plant.name}")
                    }
                )
            }
            composable("aichat") {
                com.example.ui.AiChatScreen(
                    onNavigateToPricing = { navController.navigate("pricing") }
                )
            }
            composable("plantProfile/{plantName}") { backStackEntry ->
                val plantName = backStackEntry.arguments?.getString("plantName") ?: ""
                com.example.ui.PlantProfileScreen(
                    plantName = plantName,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("profile") {
                ProfileScreen(
                    onLogout = {
                        rootNavController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onUpgradeClick = {
                        navController.navigate("pricing")
                    },
                    onScanHistoryClick = {
                        navController.navigate("scanHistory")
                    }
                )
            }
            composable("pricing") {
                com.example.ui.PricingScreen(onClose = { navController.popBackStack() })
            }
            composable("scanHistory") {
                com.example.ui.ScanHistoryScreen(onBackClick = { navController.popBackStack() })
            }
        }
    }
}
