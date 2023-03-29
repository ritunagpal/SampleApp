package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.NewsApplication
import com.example.myapplication.data.repo.NewsRepositoryImpl

class NewsViewModelProviderFactory(
    val newsRepository: NewsRepositoryImpl,
    val application: NewsApplication
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewsViewModel(newsRepo = newsRepository, application) as T
    }
}