package com.example.myapplication.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.ui.adapters.NewsAdapter
import com.example.myapplication.ui.NewsActivity
import com.example.myapplication.ui.NewsViewModel
import com.example.myapplication.util.Constants
import com.example.myapplication.util.Resource
import kotlinx.android.synthetic.main.fragment_serach_news.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchNewsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchNewsFragment : Fragment() {
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var newsViewModel: NewsViewModel

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_serach_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newsViewModel = (activity as NewsActivity).newsViewModel
        setUpRecyclerView()
        var job:Job?=null
        edit_query.addTextChangedListener {
            job?.cancel()
            job= MainScope().launch {
                delay(500)
            }
            if(it.toString().isNotEmpty()){
                newsViewModel.getNewsFromSearch(it.toString())
            }


        }
        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                this.putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_serachNewsFragment_to_articleFragment,
                bundle
            )

        }
        newsViewModel.searchNews.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    isLoding=false
                    searchPaginationProgressBar.visibility = View.GONE
                    it.data?.let {
                        newsAdapter.differ.submitList(it.articles.toList())
                        val totalPages = it.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage= newsViewModel.searchPageNumber==totalPages
                        println("$totalPages >>>>${it.totalResults}")
                        println(">>>>>> ${newsViewModel.searchPageNumber}")

                        if(isLastPage)
                            rvSearchNews.setPadding(0,0,0,0)
                    }

                }
                is Resource.Error -> {
                    isLoding=false
                    searchPaginationProgressBar.visibility = View.GONE
                    it.message?.let {
                        Log.e("SearchNews", it)
                        Toast.makeText(activity,"Error Occured: $it", Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading -> {
                    isLoding=true
                    searchPaginationProgressBar.visibility = View.VISIBLE
                }
            }
        })


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
            val itemMoreThanVisible = totalItemCountInList >= Constants.QUERY_PAGE_SIZE

            val shouldPaginate = notLoadingNotLastPage && isAtLastItem && notAtBeginning
                    && itemMoreThanVisible
            if (shouldPaginate) {
                newsViewModel.getNewsFromSearch(edit_query.text.toString())
                isScrolling = false
            }

        }
    }


    fun setUpRecyclerView() {
        newsAdapter = NewsAdapter()
        rvSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(scrollstate)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SerachNewsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchNewsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}