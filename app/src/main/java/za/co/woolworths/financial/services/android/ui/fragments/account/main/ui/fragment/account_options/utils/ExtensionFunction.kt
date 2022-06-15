package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.NavigationRes
import androidx.annotation.Nullable
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment

fun Fragment.setupGraph(
    @NavigationRes graphResId: Int,
    @IdRes containerId: Int,
    @IdRes startDestination: Int,
    @Nullable startDestinationArgs: Bundle =  bundleOf()
) {
    val navHostFragment = childFragmentManager.findFragmentById(containerId) as? NavHostFragment
    val navController = navHostFragment?.navController
    val navGraph = navController?.navInflater?.inflate(graphResId)
    navGraph?.setStartDestination(startDestination)
    navGraph?.let { navController.setGraph(it, startDestinationArgs) }
}
