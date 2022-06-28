package com.assignment.filmList

import android.app.Application
import com.assignment.filmList.data.network.ApiInterface
import com.assignment.filmList.data.network.NetworkConnectionInterceptor
import com.assignment.filmList.data.repositories.MovieListRepository
import com.assignment.filmList.ui.movieList.MovieListViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class FilmListApplication : Application(), KodeinAware
{
    override val kodein = Kodein.lazy{
        import(androidXModule(this@FilmListApplication))

        bind() from singleton { NetworkConnectionInterceptor(instance()) }
        bind() from singleton { ApiInterface(instance()) }

        bind() from singleton { MovieListRepository(instance()) }
        bind() from provider { MovieListViewModelFactory(instance()) }
    }
}