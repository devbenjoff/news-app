package com.example.ownnewsapp.ui.repositories

import com.example.ownnewsapp.ui.api.RetrofitInstance
import com.example.ownnewsapp.ui.db.ArticleDatabase
import com.example.ownnewsapp.ui.models.Article

class NewsRepository(
    val db: ArticleDatabase
) {
    suspend fun getBreakingNews(country: String, pageNumber: Int) =
        RetrofitInstance.api.getBreakingNews(country, pageNumber)

    suspend fun searchNews(query: String) = RetrofitInstance.api.searchNews(query)

    suspend fun upsert(article: Article) = db.articleDao().upsert(article)

    fun getSavedNews() = db.articleDao().getAllArticles()

    suspend fun deleteArticle(article: Article) = db.articleDao().deleteArticle(article)
}