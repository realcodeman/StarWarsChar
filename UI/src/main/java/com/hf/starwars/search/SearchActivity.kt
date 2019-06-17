package com.hf.starwars.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.hf.starwars.Constants.PERSON_EXTRA_KEY
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.hf.presentation.search.SearchViewModel
import com.hf.presentation.state.ResourceState
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_search.*
import javax.inject.Inject
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.hf.starwars.Constants
import com.hf.starwars.R
import com.hf.starwars.ViewModelFactory
import com.hf.starwars.details.PersonDetailsActivity
import kotlinx.android.synthetic.main.content_search.*
import timber.log.Timber


class SearchActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel : SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        AndroidInjection.inject(this)

        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(SearchViewModel::class.java)

        setUpTextInput()
        setUpObservers()
        setUpResultsRecyclerView()
    }

    private fun setUpTextInput() {
        queryEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                p0?.let {
                    val query = it.toString()
                    viewModel.search(query)

                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })
    }

    private fun setUpResultsRecyclerView() {
        resultsRecyclerView.layoutManager = LinearLayoutManager(this)

        resultsRecyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        val itemDecorator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(this, R.drawable.search_results_divider)?.let {
            itemDecorator.setDrawable(it)
        }

        resultsRecyclerView.adapter = SearchResultsRecyclerAdapter {
            viewModel.onPersonSelected(it)
        }

    }

    private fun setUpObservers() {

        viewModel.navToDetailsPage.observe(this, Observer {
            it?.getContentIfNotHandled()?.let {
                Toast.makeText(this, it.name, Toast.LENGTH_SHORT).show()

                val intent = Intent(this, PersonDetailsActivity::class.java)
                intent.putExtra(PERSON_EXTRA_KEY, it)
                startActivity(intent)
            }
        })


        viewModel.liveData.observe(this, Observer {
            it?.let {
                when (it.status) {
                    ResourceState.SUCCESS -> {

                        it.data?.let { items ->
                            (resultsRecyclerView.adapter as SearchResultsRecyclerAdapter?)?.let {
                                it.updateItems(items)
                            }
                        }

                    }
                    ResourceState.LOADING -> {
                        Timber.d("loading", "loading")
                    }
                    ResourceState.ERROR -> {
                        Timber.d("error", it.message)
                    }
                }
            }
        })
    }
}