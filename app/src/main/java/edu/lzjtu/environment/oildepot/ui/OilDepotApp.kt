package edu.lzjtu.environment.oildepot.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.lzjtu.environment.oildepot.ui.screens.AlertsScreen
import edu.lzjtu.environment.oildepot.ui.screens.MeasuresScreen
import edu.lzjtu.environment.oildepot.ui.screens.OverviewScreen
import edu.lzjtu.environment.oildepot.ui.screens.PointsScreen
import edu.lzjtu.environment.oildepot.ui.screens.PredictionScreen
import edu.lzjtu.environment.oildepot.ui.theme.OilDepotTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OilDepotApp(
    initialSection: MainSection,
    onSendDemoNotification: () -> Boolean,
    viewModel: AppViewModel = viewModel(factory = AppViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(initialSection) {
        viewModel.selectSection(initialSection)
    }

    OilDepotTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("兰州油库环境应急监控", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = state.dashboard?.let { "数据更新 ${it.updatedAt}" } ?: "正在加载监测数据",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                )
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    MainSection.entries.forEach { section ->
                        NavigationBarItem(
                            selected = state.selectedSection == section,
                            onClick = { viewModel.selectSection(section) },
                            icon = { Icon(section.icon, contentDescription = section.title) },
                            label = { Text(section.title) },
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            if (state.loading) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 80.dp))
                }
            } else if (state.dashboard == null) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "生产服务尚未接入",
                        modifier = Modifier.padding(top = 80.dp),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = state.loadError ?: "请检查接口配置",
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                val dashboard = requireNotNull(state.dashboard)
                when (state.selectedSection) {
                    MainSection.OVERVIEW -> OverviewScreen(
                        dashboard = dashboard,
                        selectedDepot = state.selectedDepot,
                        onDepotSelected = viewModel::selectDepot,
                        modifier = Modifier.padding(innerPadding),
                    )
                    MainSection.POINTS -> PointsScreen(
                        dashboard = dashboard,
                        selectedDepot = state.selectedDepot,
                        onDepotSelected = viewModel::selectDepot,
                        modifier = Modifier.padding(innerPadding),
                    )
                    MainSection.PREDICTION -> PredictionScreen(
                        input = state.predictionInput,
                        errors = state.predictionErrors,
                        running = state.predictionRunning,
                        result = state.predictionResult,
                        onInputChanged = viewModel::updatePredictionInput,
                        onRunPrediction = viewModel::runPrediction,
                        modifier = Modifier.padding(innerPadding),
                    )
                    MainSection.ALERTS -> AlertsScreen(
                        alerts = dashboard.alerts,
                        selectedDepot = state.selectedDepot,
                        acknowledgedIds = state.acknowledgedAlertIds,
                        onDepotSelected = viewModel::selectDepot,
                        onAcknowledge = viewModel::acknowledgeAlert,
                        onSendDemoNotification = {
                            val sent = onSendDemoNotification()
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (sent) "测试预警已发送到系统通知栏" else "请先允许系统通知权限",
                                )
                            }
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                    MainSection.MEASURES -> MeasuresScreen(
                        measures = dashboard.measures,
                        completedIds = state.completedMeasureIds,
                        onComplete = viewModel::completeMeasure,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

private val MainSection.icon: ImageVector
    get() = when (this) {
        MainSection.OVERVIEW -> Icons.Outlined.Dashboard
        MainSection.POINTS -> Icons.Outlined.Place
        MainSection.PREDICTION -> Icons.Outlined.Science
        MainSection.ALERTS -> Icons.Outlined.WarningAmber
        MainSection.MEASURES -> Icons.Outlined.Checklist
    }
