package com.example.ownnewsapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.ownnewsapp.R
import com.example.ownnewsapp.databinding.FragmentArticleBinding
import com.example.ownnewsapp.ui.MainActivity
import com.example.ownnewsapp.ui.NewsViewModelProviderFactory
import com.example.ownnewsapp.ui.db.ArticleDatabase
import com.example.ownnewsapp.ui.models.NewsViewModel
import com.example.ownnewsapp.ui.repositories.NewsRepository
import com.google.android.material.snackbar.Snackbar

class ArticleFragment: Fragment() {
    private var _binding: FragmentArticleBinding? = null
    private lateinit var viewModel: NewsViewModel
    private val args: ArticleFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentArticleBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()

        val article = args.article
        binding.wvArticle.apply {
            webViewClient = WebViewClient()
            loadUrl(article.url)
        }

        binding.fabFavorite.setOnClickListener {
            viewModel.saveArticle(article)
            Snackbar.make(binding.root, "Article saved successfully", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupViewModel() {
        val newsRepository = NewsRepository(ArticleDatabase.createDatabase(requireContext()))
        val viewModelProviderFactory = NewsViewModelProviderFactory(newsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[NewsViewModel::class.java]
    }
}