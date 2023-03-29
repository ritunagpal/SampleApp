package com.example.myapplication.domain.usecases

import com.example.myapplication.domain.model.NewsResponse
import com.example.myapplication.domain.repo.NewsRepo
import com.example.myapplication.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NewsUseCases(val newsRepo: NewsRepo) {

    fun getBreakingNews(pageNo: Int, countryCode: String): Flow<Resource<NewsResponse>> =
       flow {
            emit(newsRepo.getBreakingNews(pageNo, countryCode))
        }
}