package com.mostawdai.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mostawdai.feature.materials.AddMaterialScreen
import com.mostawdai.feature.materials.MaterialsListScreen

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.MATERIALS_LIST) {
        composable(Routes.MATERIALS_LIST) {
            MaterialsListScreen(
                onAddMaterialClick = { navController.navigate(Routes.ADD_MATERIAL) }
            )
        }
        composable(Routes.ADD_MATERIAL) {
            AddMaterialScreen(
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}
