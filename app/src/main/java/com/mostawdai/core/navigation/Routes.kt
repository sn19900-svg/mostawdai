package com.mostawdai.core.navigation

object Routes {
    const val MATERIALS_LIST = "materials_list"
    const val ADD_MATERIAL = "add_material"
    const val MATERIAL_DETAIL = "material_detail/{materialId}"
    const val EDIT_MATERIAL = "edit_material/{materialId}"
    const val BACKUP = "backup"
    const val SETTINGS = "settings"
    const val SALES_REPORT = "sales_report"
    const val PROFIT = "profit"

    fun materialDetail(materialId: Long) = "material_detail/$materialId"
    fun editMaterial(materialId: Long) = "edit_material/$materialId"
}
