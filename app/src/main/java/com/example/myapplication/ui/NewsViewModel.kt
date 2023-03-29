package com.example.myapplication.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.*
import com.example.myapplication.NewsApplication
import com.example.myapplication.domain.model.Article
import com.example.myapplication.domain.model.NewsResponse
import com.example.myapplication.data.repo.NewsRepositoryImpl
import com.example.myapplication.domain.repo.NewsRepo
import com.example.myapplication.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(private val newsRepo: NewsRepo, app: Application) :
    AndroidViewModel(app) {
    var pageNumber = 1
    val livedata = MutableLiveData<Resource<NewsResponse>>()
    var list: NewsResponse? = null

    var searchPageNumber = 1

    val searchNews = MutableLiveData<Resource<NewsResponse>>()
    var searchResponseList: NewsResponse? = null

    init {
        getBreakingNews("us")
    }

    fun getBreakingNews(countryCode: String) {
        livedata.postValue(Resource.Loading())
        viewModelScope.launch {
            var response = newsRepo.getBreakingNews(pageNumber, countryCode)
            livedata.postValue(getResponseFromApi(response))
        }
    }

    fun getNewsFromSearch(query: String) {
        searchNews.postValue(Resource.Loading())
        viewModelScope.launch {
            println(query)
            var response = newsRepo.getNewsFromSearch(query, searchPageNumber)
            searchNews.postValue(handleSearchNewsResponse(response))
        }
    }

    private fun getResponseFromApi(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { it ->
                pageNumber++
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


    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let {
                searchPageNumber++
                it.articles.filter { article ->
                    article.url != null
                }
                if (searchResponseList == null) {
                    searchResponseList = it
                } else {
                    val oldArticles = searchResponseList?.articles
                    val newArticles = it.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchResponseList ?: it)
            }
        }
        return Resource.Error(response.message())
    }

    fun insertArticle(article: Article) {
        viewModelScope.launch {
            newsRepo.upsert(article)
        }
    }

    fun getSavedNews(): LiveData<List<Article>> {
        return newsRepo.getAllArticle()
    }

    fun deleteArticle(article: Article) {
        viewModelScope.launch {
            newsRepo.deleteArticle(article)
        }
    }
    suspend fun getSafeSearchNews(searchQuery: String) {
        searchNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                var response = newsRepo.getNewsFromSearch(searchQuery,searchPageNumber)
                searchNews.postValue(handleSearchNewsResponse(response))
            } else {
                searchNews.postValue(Resource.Error("No internet Connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> searchNews.postValue(Resource.Error("Io Exception"))
                else -> searchNews.postValue(Resource.Error("Parsing Exception"))

            }

        }
    }
    suspend fun getSafeBreakingNews(countryCode: String) {
        livedata.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                    var response = newsRepo.getBreakingNews(pageNumber, countryCode)
                    livedata.postValue(getResponseFromApi(response))
            } else {
                livedata.postValue(Resource.Error("No internet Connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> livedata.postValue(Resource.Error("Io Exception"))
                else -> livedata.postValue(Resource.Error("Parsing Exception"))

            }

        }
    }

    fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwrok = connectivityManager.activeNetwork ?: return false
            var capabilities =
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
                    TYPE_WIFI -> true
                    TYPE_ETHERNET -> true
                    TYPE_MOBILE -> true

                    else -> false
                }
            }
        }
        return false
    }
}