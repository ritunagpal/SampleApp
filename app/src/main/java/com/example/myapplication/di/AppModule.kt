package com.example.myapplication.di

import android.app.Application
import com.example.myapplication.data.remote.NewsApi
import com.example.myapplication.database.ArticleDatabase
import com.example.myapplication.data.repo.NewsRepositoryImpl
import com.example.myapplication.domain.repo.NewsRepo
import com.example.myapplication.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRetrofitInstance(): NewsApi {
        return Retrofit.Builder().baseUrl(Constants.BASE_URL)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(NewsApi::class.java)
    }

    private fun getOkHttpClient() = OkHttpClient()
        .newBuilder()
        .addNetworkInterceptor(RequestInterceptor())
        .build()

    @Provides
    @Singleton
    fun provideRepoObj(context: Application, api: NewsApi): NewsRepo {
        val articleDatabase = ArticleDatabase(context)
        return NewsRepositoryImpl(articleDatabase, api)
    }
}