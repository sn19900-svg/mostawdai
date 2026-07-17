package com.mostawdai.core.navigation

object Routes {
    const val MATERIALS_LIST = "materials_list"
    const val ADD_MATERIAL = "add_material"
    const val MATERIAL_DETAIL = "material_detail/{materialId}"

    fun materialDetail(materialId: Long) = "material_detail/$materialId"
}
