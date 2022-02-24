package za.co.woolworths.financial.services.android.ui.fragments.account.main.component

import android.app.Activity
import android.os.Bundle
import androidx.annotation.IntegerRes
import androidx.annotation.NavigationRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import javax.inject.Inject

interface INavigationGraph {
    fun setupNavigationGraph(
        activity: AppCompatActivity?,
        @IntegerRes navHostFragmentId: Int,
        @NavigationRes graphResId: Int,
        @IntegerRes startDestinationId: Int,
        startDestinationArgs: Bundle?
    )

    fun setupNavigationGraph(
        fragment: Fragment?,
        @IntegerRes navHostFragmentId: Int,
        @NavigationRes graphResId: Int,
        @IntegerRes startDestinationId: Int,
        startDestinationArgs: Bundle?
    )
}

class NavigationGraph @Inject constructor() : INavigationGraph {

    private lateinit var graph: NavGraph
    private lateinit var navController: NavController

    override fun setupNavigationGraph(
        activity: AppCompatActivity?,
        navHostFragmentId: Int,
        @NavigationRes graphResId: Int,
        startDestinationId: Int,
        startDestinationArgs: Bundle?
    ) {
        activity ?: return
        setUpNavigation(
            activity,
            navHostFragmentId,
            graphResId,
            startDestinationId,
            startDestinationArgs
        )
    }

    override fun setupNavigationGraph(
        fragment: Fragment?,
        navHostFragmentId: Int,
        @NavigationRes graphResId: Int,
        startDestinationId: Int,
        startDestinationArgs: Bundle?
    ) {
        fragment ?: return
        setUpNavigation(
            fragment.activity,
            navHostFragmentId,
            graphResId,
            startDestinationId,
            startDestinationArgs
        )
    }

    private fun setUpNavigation(
        activity: Activity?,
        navHostFragmentId: Int,
        @NavigationRes graphResId: Int,
        startDestinationId: Int,
        startDestinationArgs: Bundle?
    ) {
        val navHostFragment = (activity as? AppCompatActivity)?.supportFragmentManager?.findFragmentById(navHostFragmentId) as? NavHostFragment
        if (navHostFragment != null) {
            navController = navHostFragment.navController
            val graphInflater = navController.navInflater
            graph = graphInflater.inflate(graphResId)
            graph.setStartDestination(startDestinationId)
            navController.setGraph(graph, startDestinationArgs)
        }
    }
}