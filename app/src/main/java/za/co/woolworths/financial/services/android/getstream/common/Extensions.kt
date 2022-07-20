package za.co.woolworths.financial.services.android.getstream.common

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController


fun NavController.navigateSafely(directions: NavDirections) {
    currentDestination?.getAction(directions.actionId)?.let { navigate(directions) }
}

fun NavController.navigateSafely(@IdRes resId: Int) {
    if (currentDestination?.id != resId) {
        navigate(resId, null)
    }
}

fun Fragment.navigateSafely(@IdRes resId: Int) {
    findNavController().navigateSafely(resId)
}

fun Fragment.navigateSafely(directions: NavDirections) {
    findNavController().navigateSafely(directions)
}