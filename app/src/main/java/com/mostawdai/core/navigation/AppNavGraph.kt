package com.mostawdai.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mostawdai.feature.materials.AddMaterialScreen
import com.mostawdai.feature.materials.MaterialDetailScreen
import com.mostawdai.feature.materials.MaterialsListScreen

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.MATERIALS_LIST) {
        composable(Routes.MATERIALS_LIST) {
            MaterialsListScreen(
                onAddMaterialClick = { navController.navigate(Routes.ADD_MATERIAL) },
                onMaterialClick = { id -> navController.navigate(Routes.materialDetail(id)) }
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
        ) {
            MaterialDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
