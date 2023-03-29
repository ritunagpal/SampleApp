package com.example.myapplication.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myapplication.domain.model.Article

@Dao
interface ArticleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewsForLocal(article: Article):Long

    @Query("SELECT * FROM news")
    fun getAllNews():LiveData<List<Article>>

    @Delete
    suspend fun deleteNews(article: Article)
}