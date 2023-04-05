package za.co.woolworths.financial.services.android.recommendations.analytics

import kotlinx.coroutines.*
import za.co.woolworths.financial.services.android.recommendations.analytics.usecase.RecClickUseCase
import za.co.woolworths.financial.services.android.recommendations.data.repository.RecommendationsRepository
import za.co.woolworths.financial.services.android.recommendations.data.repository.RecommendationsRepositoryImpl
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.Product

object CoroutineScopeProvider {
    val externalScope: CoroutineScope by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}

class RecommendationAnalytics private constructor(
    private val recommendationUseCase: RecommendationUseCases,
    private val coroutineScope: CoroutineScope
) : RecommendationEvents {

    override fun submitRecClicks(products: List<Product>) {
        if (coroutineScope.isActive) {
            coroutineScope.launch {
                recommendationUseCase.recClickUseCase()(products = products)
            }
        }
    }

    companion object :
        SingletonHolder<RecommendationAnalytics, RecommendationUseCases, CoroutineScope>(::RecommendationAnalytics)
}

class RecommendationUseCaseProvider : RecommendationUseCases {

    private var recClickUseCase: RecClickUseCase? = null

    override fun recClickUseCase(): RecClickUseCase {
        if (recClickUseCase == null) {
            val repository: RecommendationsRepository = RecommendationsRepositoryImpl()
            recClickUseCase = RecClickUseCase(repository)
        }
        return recClickUseCase!!
    }
}

open class SingletonHolder<out T, in A, in B>(private val constructor: (A, B) -> T) {

    @Volatile
    private var instance: T? = null

    fun getInstance(a: A, b: B): T = instance ?: synchronized(this) {
        instance ?: constructor(a, b).also { instance = it }
    }
}