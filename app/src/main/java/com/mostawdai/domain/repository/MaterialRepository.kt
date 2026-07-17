package com.mostawdai.domain.repository

import com.mostawdai.domain.model.Material
import kotlinx.coroutines.flow.Flow

interface MaterialRepository {
    fun observeAllMaterials(): Flow<List<Material>>
    suspend fun getMaterialById(id: Long): Material?
    suspend fun insertMaterial(material: Material): Long
    suspend fun updateMaterial(material: Material)
    suspend fun deleteMaterial(id: Long)
    suspend fun materialNameExists(name: String, excludeId: Long = 0): Boolean
}
