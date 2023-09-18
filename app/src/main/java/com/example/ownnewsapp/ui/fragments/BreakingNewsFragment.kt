package com.example.ownnewsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ownnewsapp.R
import com.example.ownnewsapp.databinding.FragmentBreakingNewsBinding
import com.example.ownnewsapp.ui.models.NewsViewModel
import com.example.ownnewsapp.ui.NewsViewModelProviderFactory
import com.example.ownnewsapp.ui.adapters.NewsAdapter
import com.example.ownnewsapp.ui.db.ArticleDatabase
import com.example.ownnewsapp.ui.repositories.NewsRepository
import com.example.ownnewsapp.ui.utils.Constants.Companion.QUERY_PAGE_SIZE
import com.example.ownnewsapp.ui.utils.Resource

class BreakingNewsFragment : Fragment() {
    private var _binding: FragmentBreakingNewsBinding? = null
    private lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBreakingNewsBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val newsRepository = NewsRepository(ArticleDatabase.createDatabase(requireContext()))
        val viewModelProviderFactory = NewsViewModelProviderFactory(newsRepository)

        setupRecycleView()

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_breakingNewsFragment_to_articleFragment,
                bundle
            )
        }

        viewModel = ViewModelProvider(this, viewModelProviderFactory)[NewsViewModel::class.java]

        viewModel.breakingNews.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { response ->
                        newsAdapter.differ.submitList(response.articles.toList())
                        val totalPages = response.totalResults / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.breakingNewsPage == totalPages
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
        binding.paginationProgressBar.apply {
            visibility = View.INVISIBLE
        }
        isLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.apply {
            visibility = View.VISIBLE
        }
        isLoading = true
    }

    private fun setupRecycleView() {
        newsAdapter = NewsAdapter()
        binding.rvBreakingNews.adapter = newsAdapter
        binding.rvBreakingNews.layoutManager = LinearLayoutManager(activity)
        binding.rvBreakingNews.addOnScrollListener(this@BreakingNewsFragment.scrollListener)
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                Log.d("ZOVEM", "Zovem")
                viewModel.getBreakingNews("us")
                isScrolling = false
            } else {
                binding.rvBreakingNews.setPadding(0, 0, 0, 0)
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }
}