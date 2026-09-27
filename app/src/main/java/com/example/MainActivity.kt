package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.data.model.DocType
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.ConstructionEstimateFormScreen
import com.example.ui.screens.CustomerListScreen
import com.example.ui.screens.DocDetailScreen
import com.example.ui.screens.DocFormScreen
import com.example.ui.screens.DocListScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PartyStatementScreen
import com.example.ui.screens.PaymentEntryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.SwamiGold
import com.example.ui.theme.SwamiNavy
import com.example.ui.theme.SwamiSolarTheme
import com.example.ui.viewmodel.BillingViewModel

sealed interface AppDestination {
    data object TabView : AppDestination
    data object FirmSelector : AppDestination
    data object StartupWizard : AppDestination
    data class DocDetail(val docId: Int) : AppDestination
    data class DocForm(val docType: String, val editDocId: Int? = null) : AppDestination
    data object PaymentEntry : AppDestination
    data class PartyStatement(val partyId: Int) : AppDestination
    data class FirmEditor(val firmId: Long? = null) : AppDestination
    data object SubscriptionHub : AppDestination
    data object Analytics : AppDestination
    data object Settings : AppDestination
    data object CustomerList : AppDestination
    data object PrinterSetup : AppDestination
}

enum class NavigationTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Dashboard),
    DOCS("Docs", Icons.Default.ReceiptLong),
    MORE("More", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    var isReady by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        splash.setKeepOnScreenCondition { !isReady }
        splash.setOnExitAnimationListener { provider ->
            provider.view.animate()
                .alpha(0f).setDuration(250L)
                .withEndAction { provider.remove() }.start()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            com.example.utils.AppLanguageManager.init(this@MainActivity)
            isReady = true
        }

        enableEdgeToEdge()
        setContent {
            SwamiSolarTheme {
                val billingViewModel: BillingViewModel = viewModel()
                SwamiSolarApp(billingViewModel)
            }
        }
    }
}

@Composable
fun SwamiSolarApp(viewModel: BillingViewModel) {
    val currentLang by com.example.utils.AppLanguageManager.currentLanguageFlow.collectAsState()

    androidx.compose.runtime.key(currentLang) {
        var currentTab by rememberSaveable { mutableStateOf(NavigationTab.HOME) }
        val backStack = remember { mutableStateListOf<AppDestination>(AppDestination.TabView) }

        val isOnboardingCompleted by viewModel.isOnboardingCompletedState.collectAsState()
        val profiles by viewModel.businessProfilesState.collectAsState()

        androidx.compose.runtime.LaunchedEffect(isOnboardingCompleted, profiles) {
            if (!isOnboardingCompleted && profiles.isEmpty() && !backStack.contains(AppDestination.StartupWizard)) {
                backStack.add(AppDestination.StartupWizard)
            }
        }

        val currentDestination = backStack.lastOrNull() ?: AppDestination.TabView

        // Back handling
        BackHandler(enabled = backStack.size > 1 || currentTab != NavigationTab.HOME) {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.size - 1)
            } else if (currentTab != NavigationTab.HOME) {
                currentTab = NavigationTab.HOME
            }
        }

    AnimatedContent(
        targetState = currentDestination,
        contentKey = { it },
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "AppNavigation"
    ) { destination ->
        when (destination) {
            is AppDestination.FirmSelector -> {
                com.example.ui.screens.FirmSelectorStartupScreen(
                    viewModel = viewModel,
                    onEnterDashboard = {
                        backStack.clear()
                        backStack.add(AppDestination.TabView)
                    },
                    onCreateNewFirm = {
                        backStack.add(AppDestination.FirmEditor(firmId = null))
                    },
                    onEditFirm = { firmId ->
                        backStack.add(AppDestination.FirmEditor(firmId = firmId))
                    }
                )
            }

            is AppDestination.StartupWizard -> {
                com.example.ui.screens.StartupOnboardingScreen(
                    viewModel = viewModel,
                    onComplete = {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.size - 1)
                        } else {
                            backStack.clear()
                            backStack.add(AppDestination.TabView)
                        }
                    }
                )
            }

            is AppDestination.DocDetail -> {
                DocDetailScreen(
                    docId = destination.docId,
                    viewModel = viewModel,
                    onNavigateBack = {
                        if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                    },
                    onNavigateToEdit = { editId, docType ->
                        backStack.add(AppDestination.DocForm(docType = docType, editDocId = editId))
                    },
                    onNavigateToLinkedDoc = { linkedId ->
                        backStack.add(AppDestination.DocDetail(docId = linkedId))
                    }
                )
            }

            is AppDestination.DocForm -> {
                if (destination.docType == DocType.CONST_ESTIMATE) {
                    ConstructionEstimateFormScreen(
                        editDocId = destination.editDocId,
                        viewModel = viewModel,
                        onNavigateBack = {
                            if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                        },
                        onNavigateToDetail = { newDocId ->
                            if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                            backStack.add(AppDestination.DocDetail(docId = newDocId))
                        }
                    )
                } else {
                    DocFormScreen(
                        docType = destination.docType,
                        editDocId = destination.editDocId,
                        viewModel = viewModel,
                        onNavigateBack = {
                            if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                        },
                        onNavigateToDetail = { newDocId ->
                            // Pop form and push detail
                            if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                            backStack.add(AppDestination.DocDetail(docId = newDocId))
                        }
                    )
                }
            }

            is AppDestination.PaymentEntry -> {
                PaymentEntryScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                    },
                    onNavigateToDocDetail = { docId ->
                        backStack.add(AppDestination.DocDetail(docId = docId))
                    }
                )
            }

            is AppDestination.PartyStatement -> {
                PartyStatementScreen(
                    partyId = destination.partyId,
                    viewModel = viewModel,
                    onNavigateBack = {
                        if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                    },
                    onNavigateToDocDetail = { docId ->
                        backStack.add(AppDestination.DocDetail(docId = docId))
                    },
                    onNavigateToNewDoc = { docType ->
                        backStack.add(AppDestination.DocForm(docType = docType))
                    }
                )
            }

            is AppDestination.FirmEditor -> {
                com.example.ui.screens.FirmMasterFormScreen(
                    firmId = destination.firmId,
                    viewModel = viewModel,
                    onNavigateBack = {
                        if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                    }
                )
            }

            is AppDestination.SubscriptionHub -> {
                com.example.ui.screens.SubscriptionScreen(
                    onNavigateBack = {
                        if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                    }
                )
            }

            is AppDestination.Analytics -> {
                AnalyticsScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                    }
                )
            }

            is AppDestination.Settings -> {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                    },
                    onNavigateToFirmEditor = { firmId ->
                        backStack.add(AppDestination.FirmEditor(firmId = firmId))
                    },
                    onNavigateToSubscription = {
                        backStack.add(AppDestination.SubscriptionHub)
                    },
                    onNavigateToAnalytics = {
                        backStack.add(AppDestination.Analytics)
                    },
                    onNavigateToPrinter = {
                        backStack.add(AppDestination.PrinterSetup)
                    }
                )
            }

            is AppDestination.CustomerList -> {
                CustomerListScreen(
                    viewModel = viewModel,
                    onViewStatement = { partyId ->
                        backStack.add(AppDestination.PartyStatement(partyId = partyId))
                    }
                )
            }

            is AppDestination.PrinterSetup -> {
                com.example.ui.screens.PrinterSetupScreen(
                    onNavigateBack = {
                        if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                    }
                )
            }

            AppDestination.TabView -> {
                Scaffold(
                    bottomBar = {
                        androidx.compose.material3.Surface(
                            shadowElevation = 8.dp,
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
                        ) {
                            NavigationBar(
                                containerColor = com.example.ui.theme.pro.Surface,
                                contentColor = com.example.ui.theme.pro.Ink900,
                                tonalElevation = 0.dp
                            ) {
                                NavigationTab.values().forEach { tab ->
                                    val selected = currentTab == tab
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { currentTab = tab },
                                        icon = {
                                            Icon(
                                                imageVector = tab.icon,
                                                contentDescription = tab.title,
                                                modifier = Modifier.size(if (selected) 24.dp else 22.dp)
                                            )
                                        },
                                        label = {
                                            val tabLabel = when (tab) {
                                                NavigationTab.HOME -> com.example.utils.appString("nav_home")
                                                NavigationTab.DOCS -> com.example.utils.appString("nav_docs")
                                                NavigationTab.MORE -> com.example.utils.appString("nav_more")
                                            }
                                            Text(
                                                text = tabLabel,
                                                fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                                                fontSize = 11.sp
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = com.example.ui.theme.pro.Brand600,
                                            selectedTextColor = com.example.ui.theme.pro.Brand600,
                                            indicatorColor = com.example.ui.theme.pro.Brand100,
                                            unselectedIconColor = com.example.ui.theme.pro.Ink400,
                                            unselectedTextColor = com.example.ui.theme.pro.Ink600
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        when (currentTab) {
                            NavigationTab.HOME -> {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToNewDoc = { type ->
                                        backStack.add(AppDestination.DocForm(docType = type))
                                    },
                                    onNavigateToDocDetail = { docId ->
                                        backStack.add(AppDestination.DocDetail(docId = docId))
                                    },
                                    onNavigateToTab = { tabIdx ->
                                        currentTab = when (tabIdx) {
                                            0 -> NavigationTab.HOME
                                            1 -> NavigationTab.DOCS
                                            2 -> NavigationTab.MORE
                                            else -> NavigationTab.HOME
                                        }
                                    },
                                    onNavigateToPaymentReceived = {
                                        backStack.add(AppDestination.PaymentEntry)
                                    },
                                    onNavigateToFirmEditor = { firmId ->
                                        backStack.add(AppDestination.FirmEditor(firmId = firmId))
                                    },
                                    onNavigateToSubscription = {
                                        backStack.add(AppDestination.SubscriptionHub)
                                    }
                                )
                            }
                            NavigationTab.DOCS -> {
                                DocListScreen(
                                    docType = DocType.INVOICE,
                                    viewModel = viewModel,
                                    onNavigateToNewDoc = {
                                        backStack.add(AppDestination.DocForm(docType = DocType.INVOICE))
                                    },
                                    onNavigateToNewConstructionDoc = {
                                        backStack.add(AppDestination.DocForm(docType = DocType.CONST_ESTIMATE))
                                    },
                                    onNavigateToDocDetail = { docId ->
                                        backStack.add(AppDestination.DocDetail(docId = docId))
                                    }
                                )
                            }
                            NavigationTab.MORE -> {
                                com.example.ui.screens.MoreScreen(
                                    viewModel = viewModel,
                                    onNavigateToFirmEditor = { firmId ->
                                        backStack.add(AppDestination.FirmEditor(firmId = firmId))
                                    },
                                    onNavigateToCustomers = {
                                        backStack.add(AppDestination.CustomerList)
                                    },
                                    onNavigateToSettings = {
                                        backStack.add(AppDestination.Settings)
                                    },
                                    onNavigateToSubscription = {
                                        backStack.add(AppDestination.SubscriptionHub)
                                    },
                                    onNavigateToPrinter = {
                                        backStack.add(AppDestination.PrinterSetup)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}
