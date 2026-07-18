package com.mostawdai.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {
    @Query("SELECT * FROM materials ORDER BY name ASC")
    fun observeAll(): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM materials ORDER BY name ASC")
    suspend fun getAllOnce(): List<MaterialEntity>

    @Query("SELECT * FROM materials WHERE id = :id")
    suspend fun getById(id: Long): MaterialEntity?

    @Insert
    suspend fun insert(material: MaterialEntity): Long

    @Update
    suspend fun update(material: MaterialEntity)

    @Query("DELETE FROM materials WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM materials")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM materials WHERE name = :name AND id != :excludeId")
    suspend fun countByName(name: String, excludeId: Long): Int
}
