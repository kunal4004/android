package za.co.woolworths.financial.services.android.ui.fragments.bpi.contract

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavArgument
import androidx.navigation.fragment.NavHostFragment

interface NavigationGraphRouterInterface {
    fun createNavigationGraph(
        fragmentContainerView: NavHostFragment?,
        @IdRes navHostFragmentId: Int?,
        @IdRes startDestination: Int,
        extras: Bundle?
    ): NavHostFragment?
    fun navigateTo(destinationId: Int, bundle: Bundle?)
    fun navigate(destinationId: Int, bundle: Bundle?){}
    fun navigateToPreviousFragment()
}