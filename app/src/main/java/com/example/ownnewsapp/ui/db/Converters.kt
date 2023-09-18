package com.example.ownnewsapp.ui.db

import androidx.room.TypeConverter
import com.example.ownnewsapp.ui.models.Source

class Converters {
    @TypeConverter
    fun fromSource(source: Source): String = source.name

    @TypeConverter
    fun toSource(name: String): Source = Source(name, name)
}