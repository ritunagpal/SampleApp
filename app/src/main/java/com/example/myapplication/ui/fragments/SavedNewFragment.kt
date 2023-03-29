package com.example.myapplication.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.ui.adapters.NewsAdapter
import com.example.myapplication.databinding.FragmentSavednewBinding
import com.example.myapplication.ui.NewsActivity
import com.example.myapplication.ui.NewsViewModel
import kotlinx.android.synthetic.main.fragment_breakingnews.*
import kotlinx.android.synthetic.main.fragment_savednew.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import java.time.Duration

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SavedNewFragment : Fragment() {

    private lateinit var newsViewModel: NewsViewModel
    private var _binding: FragmentSavednewBinding? = null
    private lateinit var newsAdapter: NewsAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSavednewBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newsViewModel=(activity as NewsActivity).newsViewModel
        setUpRecyclerView()
        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                this.putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_savedNewFragment_to_articleFragment,
                bundle
            )
        }

        val itemTouchHelper= object :ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN or
        ItemTouchHelper.UP,
        ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val article=newsAdapter.differ.currentList[viewHolder.adapterPosition]
                newsViewModel.deleteArticle(article)
                Snackbar.make(view,"Article Deleted",Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") {
                        newsViewModel.insertArticle(article = article)
                    }
                }.show()
            }

        }

        ItemTouchHelper(itemTouchHelper).apply {
            attachToRecyclerView(rvSavedNews)
        }

        newsViewModel.getSavedNews().observe(viewLifecycleOwner, Observer {
            newsAdapter.differ.submitList(it)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpRecyclerView() {
        newsAdapter = NewsAdapter()
        rvSavedNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)

        }
    }

}