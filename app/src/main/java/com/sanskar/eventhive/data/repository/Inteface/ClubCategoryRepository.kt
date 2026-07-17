package com.sanskar.eventhive.data.repository.Inteface

import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.Category
import kotlinx.coroutines.flow.Flow

interface ClubCategoryRepository{
    suspend fun saveCategory(category: Category): Resource<Unit>
    suspend fun updateCategory(category: Category): Resource<Unit>
    suspend fun deleteCategory(categoryId: String): Resource<Unit>

    suspend fun addClubToCategory(categoryId: String, clubId: String): Resource<Unit>
    suspend fun removeClubFromCategory(categoryId: String, clubId: String): Resource<Unit>

    fun getCategory(categoryId: String): Flow<Category?>
    fun getAllCategories(): Flow<List<Category>>


}