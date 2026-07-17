package com.mostawdai.data.repository

import com.mostawdai.data.local.MaterialDao
import com.mostawdai.data.local.toDomain
import com.mostawdai.data.local.toEntity
import com.mostawdai.domain.model.Material
import com.mostawdai.domain.repository.MaterialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MaterialRepositoryImpl @Inject constructor(
    private val dao: MaterialDao
) : MaterialRepository {

    override fun observeAllMaterials(): Flow<List<Material>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getMaterialById(id: Long): Material? =
        dao.getById(id)?.toDomain()

    override suspend fun insertMaterial(material: Material): Long =
        dao.insert(material.toEntity())

    override suspend fun updateMaterial(material: Material) =
        dao.update(material.toEntity())

    override suspend fun deleteMaterial(id: Long) =
        dao.deleteById(id)

    override suspend fun materialNameExists(name: String, excludeId: Long): Boolean =
        dao.countByName(name, excludeId) > 0
}
