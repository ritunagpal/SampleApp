package com.example.myapplication.database

import androidx.room.TypeConverter
import com.example.myapplication.domain.model.Source

class Converters {

    @TypeConverter
    fun fromSource(source: Source): String {
        return source.name
    }

    @TypeConverter
    fun toSource(name: String): Source {
        return Source(name, name)

    }

}