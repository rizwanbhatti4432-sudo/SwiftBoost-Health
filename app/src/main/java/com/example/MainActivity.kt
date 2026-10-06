package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.OptimizationSuccessDialog
import com.example.ui.screens.CleanerMasterScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DiagnosticsHealthScreen
import com.example.ui.screens.SpecsHistoryScreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkNavyBackground
import com.example.ui.theme.DarkNavyBorder
import com.example.ui.theme.DarkNavySurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: MainViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val showSuccessDialog by viewModel.showCleanSuccessDialog.collectAsState()
    val successTitle by viewModel.cleanSuccessTitle.collectAsState()
    val successBytes by viewModel.cleanSuccessBytes.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = DarkNavyBackground,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar"),
                containerColor = DarkNavySurface,
                tonalElevation = androidx.compose.ui.unit.Dp(8f)
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.DASHBOARD,
                    onClick = { viewModel.setTab(AppTab.DASHBOARD) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Health Core"
                        )
                    },
                    label = {
                        Text(
                            text = "Health Core",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.DASHBOARD) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary,
                        unselectedIconColor = TextTertiary,
                        unselectedTextColor = TextTertiary
                    ),
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.CLEANER,
                    onClick = { viewModel.setTab(AppTab.CLEANER) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = "Cleaner Master"
                        )
                    },
                    label = {
                        Text(
                            text = "Cleaner",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.CLEANER) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary,
                        unselectedIconColor = TextTertiary,
                        unselectedTextColor = TextTertiary
                    ),
                    modifier = Modifier.testTag("nav_tab_cleaner")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.DIAGNOSTICS,
                    onClick = { viewModel.setTab(AppTab.DIAGNOSTICS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = "Diagnostics"
                        )
                    },
                    label = {
                        Text(
                            text = "Diagnostics",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.DIAGNOSTICS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary,
                        unselectedIconColor = TextTertiary,
                        unselectedTextColor = TextTertiary
                    ),
                    modifier = Modifier.testTag("nav_tab_diagnostics")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.SPECS_HISTORY,
                    onClick = { viewModel.setTab(AppTab.SPECS_HISTORY) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = "Device Specs"
                        )
                    },
                    label = {
                        Text(
                            text = "Specs",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.SPECS_HISTORY) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary,
                        unselectedIconColor = TextTertiary,
                        unselectedTextColor = TextTertiary
                    ),
                    modifier = Modifier.testTag("nav_tab_specs")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                AppTab.CLEANER -> CleanerMasterScreen(viewModel = viewModel)
                AppTab.DIAGNOSTICS -> DiagnosticsHealthScreen(viewModel = viewModel)
                AppTab.SPECS_HISTORY -> SpecsHistoryScreen(viewModel = viewModel)
            }
        }

        if (showSuccessDialog) {
            OptimizationSuccessDialog(
                title = successTitle,
                freedBytes = successBytes,
                onDismiss = { viewModel.dismissSuccessDialog() }
            )
        }
    }
}

