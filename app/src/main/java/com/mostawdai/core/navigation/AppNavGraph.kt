package com.mostawdai.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mostawdai.feature.backup.BackupScreen
import com.mostawdai.feature.materials.AddMaterialScreen
import com.mostawdai.feature.materials.EditMaterialScreen
import com.mostawdai.feature.materials.MaterialDetailScreen
import com.mostawdai.feature.materials.MaterialsListScreen
import com.mostawdai.feature.profit.ProfitScreen
import com.mostawdai.feature.sales.SalesReportScreen
import com.mostawdai.feature.settings.SettingsScreen

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.MATERIALS_LIST) {
        composable(Routes.MATERIALS_LIST) {
            MaterialsListScreen(
                onAddMaterialClick = { navController.navigate(Routes.ADD_MATERIAL) },
                onMaterialClick = { id -> navController.navigate(Routes.materialDetail(id)) },
                onBackupClick = { navController.navigate(Routes.BACKUP) },
                onSalesClick = { navController.navigate(Routes.SALES_REPORT) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                onProfitClick = { navController.navigate(Routes.PROFIT) }
            )
        }
        composable(Routes.ADD_MATERIAL) {
            AddMaterialScreen(
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.MATERIAL_DETAIL,
            arguments = listOf(navArgument("materialId") { type = NavType.LongType })
        ) { backStackEntry ->
            val materialId = backStackEntry.arguments?.getLong("materialId") ?: 0L
            MaterialDetailScreen(
                onBack = { navController.popBackStack() },
                onEditClick = { navController.navigate(Routes.editMaterial(materialId)) }
            )
        }
        composable(
            route = Routes.EDIT_MATERIAL,
            arguments = listOf(navArgument("materialId") { type = NavType.LongType })
        ) {
            EditMaterialScreen(
                onSaved = { navController.popBackStack() },
                onDeleted = { navController.popBackStack(Routes.MATERIALS_LIST, false) },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(Routes.BACKUP) {
            BackupScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SALES_REPORT) {
            SalesReportScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PROFIT) {
            ProfitScreen(onBack = { navController.popBackStack() })
        }
    }
}
