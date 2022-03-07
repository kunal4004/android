package za.co.woolworths.financial.services.android.ui.fragments.account.main.component

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.NavigationRes
import androidx.navigation.NavController
import javax.inject.Inject

interface INavigationGraph {
    fun setupNavigationGraph(
        navController: NavController?,
        @NavigationRes graphResId: Int,
        @IdRes startDestinationId: Int,
        startDestinationArgs: Bundle?
    )

}

class NavigationGraph @Inject constructor() : INavigationGraph {

    override fun setupNavigationGraph(
        navController: NavController?,
        @NavigationRes graphResId: Int,
        startDestinationId: Int,
        startDestinationArgs: Bundle?
    ) {
        setUpNavigation(
            navController,
            graphResId,
            startDestinationId,
            startDestinationArgs
        )
    }

    private fun setUpNavigation(
        navController: NavController?,
        @NavigationRes graphResId: Int,
        startDestinationId: Int,
        startDestinationArgs: Bundle?
    ) {
        navController?.apply {
            val graph = navInflater.inflate(graphResId)
            graph.startDestination = startDestinationId
            setGraph(graph, startDestinationArgs)
        }
    }
}