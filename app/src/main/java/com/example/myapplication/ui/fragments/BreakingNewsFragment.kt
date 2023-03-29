package com.example.myapplication.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.ui.adapters.NewsAdapter
import com.example.myapplication.databinding.FragmentBreakingnewsBinding
import com.example.myapplication.domain.model.Article
import com.example.myapplication.ui.NewsActivity
import com.example.myapplication.ui.NewsViewModel
import com.example.myapplication.util.Constants.QUERY_PAGE_SIZE
import com.example.myapplication.util.Resource
import kotlinx.android.synthetic.main.fragment_breakingnews.*
import kotlinx.android.synthetic.main.fragment_serach_news.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class BreakingNewsFragment : Fragment() {

    private lateinit var newsAdapter: NewsAdapter
    private var _binding: FragmentBreakingnewsBinding? = null
    lateinit var viewModel: NewsViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentBreakingnewsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).newsViewModel
        setUpRecyclerView()
        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                this.putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_breakingNewsFragment_to_articleFragment,
                bundle
            )

        }

        viewModel.livedata.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    isLoding = false
                    paginationProgressBar.visibility = View.GONE
                    response.data?.let {
                        newsAdapter.differ.submitList(it.articles.toList())
                        val totalPages = response.data.totalResults / QUERY_PAGE_SIZE + 2
                        println("$totalPages >>>>${response.data.totalResults}")
                        isLastPage= viewModel.pageNumber==totalPages
                        println(">>>>>> ${viewModel.pageNumber}")
                        if(isLastPage)
                            rvBreakingNews.setPadding(0,0,0,0)

                    }

                }
                is Resource.Error -> {
                    isLoding = false
                    paginationProgressBar.visibility = View.GONE
                    response.message?.let {
                        Log.e("Error", it)
                        Toast.makeText(activity,"Error Occured: $it",Toast.LENGTH_LONG).show()
                    }
                }

                is Resource.Loading -> {
                    isLoding = true
                    paginationProgressBar.visibility = View.VISIBLE
                }

            }
        })
    }

    private fun setUpRecyclerView() {
        newsAdapter = NewsAdapter()
        rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(scrollstate)

        }
    }

    var isLoding = false
    var isLastPage = false
    var isScrolling = false

    var scrollstate = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val totalVisibleItemCount = layoutManager.childCount
            val totalItemCountInList = layoutManager.itemCount
            val isAtLastItem =
                firstVisibleItemPosition + totalVisibleItemCount >= totalItemCountInList
            val notLoadingNotLastPage = !isLoding && !isLastPage
            val notAtBeginning = firstVisibleItemPosition > 0
            val itemMoreThanVisible = totalItemCountInList >= QUERY_PAGE_SIZE


            val shouldPaginate = notLoadingNotLastPage && isAtLastItem && notAtBeginning
                    && itemMoreThanVisible
            if (shouldPaginate) {
                viewModel.getBreakingNews("us")
                isScrolling = false
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}