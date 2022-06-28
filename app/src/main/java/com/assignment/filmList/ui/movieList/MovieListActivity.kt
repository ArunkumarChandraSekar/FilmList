package com.assignment.filmList.ui.movieList

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.assignment.filmList.R
import com.assignment.filmList.databinding.ActivityMovielistBinding
import com.assignment.filmList.ui.adapter.CustomAdapterMovies
import com.assignment.filmList.util.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class MovieListActivity : AppCompatActivity(), KodeinAware {
    companion object {
        const val ANIMATION_DURATION = 1000.toLong()
    }

    override val kodein by kodein()
    private lateinit var dataBind: ActivityMovielistBinding
    private lateinit var viewModel: MovieListViewModel
    private val factory: MovieListViewModelFactory by instance()
    private lateinit var customAdapterMovies: CustomAdapterMovies

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBind = DataBindingUtil.setContentView(this, R.layout.activity_movielist)


        setupViewModel()
        setupUI()
        initializeObserver()
        handleNetworkChanges()
        setupAPICall()

    }

    private fun setupUI() {
        customAdapterMovies = CustomAdapterMovies()
        dataBind.recyclerViewMovies.apply {
            layoutManager = StaggeredGridLayoutManager(
                2, StaggeredGridLayoutManager.VERTICAL
            )
            itemAnimator = DefaultItemAnimator()
            adapter = customAdapterMovies


            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager?
                    val visibleItemCount = layoutManager!!.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItems = IntArray(layoutManager.spanCount)
                    val firstVisibleItemPosition =
                        layoutManager.findFirstVisibleItemPositions(firstVisibleItems)

                    viewModel.checkForLoadMoreItems(
                        visibleItemCount,
                        totalItemCount,
                        firstVisibleItems[0]
                    )
                }
            })
        }

        search(dataBind.searchView)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, factory).get(MovieListViewModel::class.java)
    }

    private fun initializeObserver() {
        viewModel.movieNameLiveData.observe(this, Observer {
            Log.i("Info", "Movie Name = $it")
        })
        viewModel.loadMoreListLiveData.observe(this, Observer {
            if (it) {
                customAdapterMovies.setData(null)
                Handler().postDelayed({
                    viewModel.loadMore()
                }, 2000)
            }
        })
    }

    private fun setupAPICall() {
        viewModel.moviesLiveData.observe(this, Observer { state ->
            when (state) {
                is State.Loading -> {
                    showToast( "Loading", Toast.LENGTH_SHORT)
                    dataBind.recyclerViewMovies.hide()
                    // dataBind.linearLayoutSearch.hide()
                    dataBind.progressBar.show()
                }
                is State.Success -> {
                    showToast( "Success", Toast.LENGTH_SHORT)
                    dataBind.recyclerViewMovies.show()
                    //  dataBind.linearLayoutSearch.hide()
                    dataBind.progressBar.hide()
                    customAdapterMovies.setData(state.data)
                }
                is State.Error -> {
                    showToast( "Error", Toast.LENGTH_SHORT)
                    dataBind.progressBar.hide()
                    showToast(state.message)
                }
            }
        })
    }

    private fun handleNetworkChanges() {
        NetworkUtils.getNetworkLiveData(applicationContext).observe(this, Observer { isConnected ->
            if (!isConnected) {

                dataBind.networkStatusLayout.apply {

                    snackbar(getString(R.string.text_no_connectivity))

                }

            } else {
                if (viewModel.moviesLiveData.value is State.Error || customAdapterMovies.itemCount == 0)
                    viewModel.getMovies()


                dataBind.networkStatusLayout.apply {

                    snackbar(getString(R.string.text_connectivity))

                }
            }
        })
    }

    private fun search(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                dismissKeyboard(searchView)
                searchView.clearFocus()
                viewModel.searchMovie(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
    }
}