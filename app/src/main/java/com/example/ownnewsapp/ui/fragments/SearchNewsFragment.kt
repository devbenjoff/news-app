package com.example.ownnewsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ownnewsapp.R
import com.example.ownnewsapp.databinding.FragmentBreakingNewsBinding
import com.example.ownnewsapp.databinding.FragmentSearchNewsBinding
import com.example.ownnewsapp.ui.MainActivity
import com.example.ownnewsapp.ui.NewsViewModelProviderFactory
import com.example.ownnewsapp.ui.adapters.NewsAdapter
import com.example.ownnewsapp.ui.db.ArticleDatabase
import com.example.ownnewsapp.ui.models.NewsViewModel
import com.example.ownnewsapp.ui.repositories.NewsRepository
import com.example.ownnewsapp.ui.utils.Resource

class SearchNewsFragment : Fragment(R.layout.fragment_search_news) {
    private var _binding: FragmentSearchNewsBinding? = null
    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchNewsBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val newsRepository = NewsRepository(ArticleDatabase.createDatabase(requireContext()))
        val viewModelProviderFactory = NewsViewModelProviderFactory(newsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[NewsViewModel::class.java]

        setupRecycleView()

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_searchNewsFragment_to_articleFragment,
                bundle
            )
        }

        binding.svSearchNews.addTextChangedListener { editable ->
            editable?.let {
                if (editable.toString().isNotEmpty()) {
                    viewModel.searchNews(editable.toString())
                }
            }
        }

        viewModel.searchNews.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { response ->
                        newsAdapter.differ.submitList(response.articles)
                    }
                }
                is Resource.Error -> {
                    response.message?.let { message ->
                        Log.d("ERROR", message)
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun hideProgressBar() {

    }

    private fun showProgressBar() {

    }


    private fun setupRecycleView() {
        newsAdapter = NewsAdapter()
        binding.apply {
            rvSearchNews.adapter = newsAdapter
            rvSearchNews.layoutManager = LinearLayoutManager(activity)
        }
    }
}