package com.example.myapplication.data.repo

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.LiveData
import com.example.myapplication.NewsApplication
import com.example.myapplication.data.remote.NewsApi
import com.example.myapplication.database.ArticleDatabase
import com.example.myapplication.domain.model.Article
import com.example.myapplication.domain.model.NewsResponse
import com.example.myapplication.domain.repo.NewsRepo
import com.example.myapplication.util.Resource
import retrofit2.Response
import java.io.IOException

class NewsRepositoryImpl(
    private val db: ArticleDatabase,
    private val api: NewsApi,
    val app: NewsApplication,
) : NewsRepo {
    private var list: NewsResponse? = null

    override suspend fun getBreakingNews(
        pageNumber: Int,
        countryCode: String,
    ): Resource<NewsResponse> {
        return getSafeBreakingNews(pageNumber, countryCode)
    }

    override suspend fun getNewsFromSearch(query: String, pageNumber: Int): Response<NewsResponse> {
        return api.searchForNews(query, pageNumber)
    }

    override suspend fun upsert(article: Article) {
        db.getArticleDao().insertNewsForLocal(article)
    }

    override fun getAllArticle(): LiveData<List<Article>> {
        return db.getArticleDao()
            .getAllNews()
    }

    override suspend fun deleteArticle(article: Article) {
        db.getArticleDao().deleteNews(article)
    }

    private fun getResponseFromApi(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { it ->
                it.articles.filter { article ->
                    article.url != null
                }
                if (list == null) {
                    list = it
                } else {
                    val oldArticles = list?.articles
                    val newArticles = it.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(list ?: it)
            }
        }
        return Resource.Error(response.message())
    }

    private suspend fun getSafeBreakingNews(pageNumber: Int, countryCode: String): Resource<NewsResponse> {
        return try {
            if (hasInternetConnection()) {
                val response = api.getBreakingNews(countryCode, pageNumber)
                getResponseFromApi(response)
            } else {
                Resource.Error("No internet Connection")
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> Resource.Error("Io Exception")
                else -> Resource.Error("Parsing Exception")
            }

        }
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = app.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwrok = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwrok) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    ConnectivityManager.TYPE_MOBILE -> true

                    else -> false
                }
            }
        }
        return false
    }

}
