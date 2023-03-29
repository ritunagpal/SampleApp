package com.example.myapplication.domain.repo

import androidx.lifecycle.LiveData
import com.example.myapplication.domain.model.Article
import com.example.myapplication.domain.model.NewsResponse
import com.example.myapplication.util.Resource
import retrofit2.Response

interface NewsRepo {
    suspend fun getBreakingNews(pageNumber: Int, countryCode: String): Resource<NewsResponse>
    suspend fun getNewsFromSearch(query: String, pageNumber: Int): Response<NewsResponse>
    suspend fun upsert(article: Article)
    fun getAllArticle(): LiveData<List<Article>>
    suspend fun deleteArticle(article: Article)
}