package za.co.woolworths.financial.services.android.ui.fragments.bpi.helper

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import za.co.woolworths.financial.services.android.ui.fragments.bpi.contract.NavigationGraphRouterInterface

class NavGraphRouterImpl : NavigationGraphRouterInterface {

    private var navHost : NavHostFragment? = null
    // Configure the navigation
    override fun createNavigationGraph(fragmentContainerView: NavHostFragment?, navHostFragmentId: Int?, startDestination: Int, extras: Bundle?): NavHostFragment? {
        navHost = fragmentContainerView
        val navController: NavController? = navHost?.navController
        val navGraph = navHostFragmentId?.let { navController?.navInflater?.inflate(it) }
        navGraph?.startDestination = startDestination
        navGraph?.let { graph -> navController?.setGraph(graph, extras) }
        return navHost
    }

    override fun navigateTo(destinationId: Int, bundle: Bundle?) {
        navHost?.navController?.navigate(destinationId,bundle)
    }

    override fun navigateToPreviousFragment() {
        navHost?.navController?.popBackStack()
    }
}
