package za.co.woolworths.financial.services.android.recommendations.presentation

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.util.BundleKeysConstants

interface RecommendationLoader {
    fun loadRecommendations(bundle: Bundle, fragmentManager: FragmentManager)
}

class RecommendationLoaderImpl: RecommendationLoader {

    override fun loadRecommendations(bundle: Bundle, fragmentManager: FragmentManager) {
        val navHostFragment = fragmentManager.findFragmentById(R.id.navHostRecommendation) as? NavHostFragment
        val navController = navHostFragment?.navController
        val navGraph = navController?.navInflater?.inflate(R.navigation.nav_recommendation_graph)

        navGraph?.startDestination = R.id.recommendationFragment
        navGraph?.let {
            navController.setGraph(
                it, bundleOf(BundleKeysConstants.BUNDLE to bundle)
            )
        }
    }
}