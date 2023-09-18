package com.example.ownnewsapp.ui.fragments

import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ownnewsapp.R
import com.example.ownnewsapp.databinding.FragmentSavedNewsBinding
import com.example.ownnewsapp.ui.MainActivity
import com.example.ownnewsapp.ui.NewsViewModelProviderFactory
import com.example.ownnewsapp.ui.adapters.NewsAdapter
import com.example.ownnewsapp.ui.db.ArticleDatabase
import com.example.ownnewsapp.ui.models.NewsViewModel
import com.example.ownnewsapp.ui.repositories.NewsRepository
import com.google.android.material.snackbar.Snackbar
import java.lang.Float.min

class SavedNewsFragment : Fragment() {
    private var _binding: FragmentSavedNewsBinding? = null
    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSavedNewsBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecycleView()
        setAdapterClickListener()

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (direction == ItemTouchHelper.LEFT) {
                    val position = viewHolder.adapterPosition
                    val article = newsAdapter.differ.currentList[position]
                    viewModel.deleteArticle(article)
                    Snackbar.make(
                        binding.root,
                        "Article deleted successfully",
                        Snackbar.LENGTH_LONG
                    )
                        .apply {
                            setAction("Undo") {
                                viewModel.saveArticle(article)
                            }
                            show()
                        }
                }
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.rvSavedNews)
        }

        viewModel.getSavedNews().observe(viewLifecycleOwner) { articles ->
            newsAdapter.differ.submitList(articles)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecycleView() {
        newsAdapter = NewsAdapter()
        binding.rvSavedNews.adapter = newsAdapter
        binding.rvSavedNews.layoutManager = LinearLayoutManager(activity)
    }

    private fun setupViewModel() {
        val newsRepository = NewsRepository(ArticleDatabase.createDatabase(requireContext()))
        val viewModelProviderFactory = NewsViewModelProviderFactory(newsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[NewsViewModel::class.java]
    }

    private fun setAdapterClickListener() {
        newsAdapter.setOnItemClickListener { article ->
            val bundle = Bundle().apply {
                putSerializable("article", article)
            }
            findNavController().navigate(
                R.id.action_savedNewsFragment_to_articleFragment,
                bundle
            )
        }
    }
}