package com.example.myapplication.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.domain.model.Article
import javax.inject.Inject

@Database(entities = [Article::class], version = 1)
@TypeConverters(Converters::class)
abstract class ArticleDatabase : RoomDatabase() {

    abstract fun getArticleDao(): ArticleDao

    companion object {
        @Volatile
        private var articleDatabase: ArticleDatabase? = null
        private val lock = Any()

        operator fun invoke(context: Context): ArticleDatabase {
          return  articleDatabase ?: synchronized(lock) {
                articleDatabase ?: createDatabase(context).also { articleDatabase = it }
            }
        }

        private fun createDatabase(context: Context): ArticleDatabase {
            return Room.databaseBuilder(
                context,
                ArticleDatabase::class.java,
                "articledatabase.db"
            ).build()
        }
    }
}