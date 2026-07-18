package com.mostawdai.core.navigation

object Routes {
    const val MATERIALS_LIST = "materials_list"
    const val ADD_MATERIAL = "add_material"
    const val MATERIAL_DETAIL = "material_detail/{materialId}"
    const val EDIT_MATERIAL = "edit_material/{materialId}"
    const val BACKUP = "backup"

    fun materialDetail(materialId: Long) = "material_detail/$materialId"
    fun editMaterial(materialId: Long) = "edit_material/$materialId"
}
