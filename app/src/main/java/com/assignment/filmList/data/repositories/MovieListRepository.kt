package com.assignment.filmList.data.repositories

import com.assignment.filmList.data.model.SearchResults
import com.assignment.filmList.data.network.ApiInterface
import com.assignment.filmList.data.network.SafeApiRequest

class MovieListRepository(private val api: ApiInterface) : SafeApiRequest()
{
    suspend fun getMovies(searchTitle: String, apiKey: String, pageIndex: Int): SearchResults
    {
        return apiRequest{ api.getSearchResultData(searchTitle, apiKey, pageIndex) }
    }
}