package za.co.woolworths.financial.services.android.recommendations.analytics

import kotlinx.coroutines.*
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.recommendations.analytics.usecase.RecClickUseCase
import za.co.woolworths.financial.services.android.recommendations.analytics.usecase.RecImpressionUseCase
import za.co.woolworths.financial.services.android.recommendations.data.repository.RecommendationsRepository
import za.co.woolworths.financial.services.android.recommendations.data.repository.RecommendationsRepositoryImpl

object CoroutineScopeProvider {
    val externalScope: CoroutineScope by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}

class RecommendationAnalytics private constructor(
    private val recommendationUseCase: RecommendationUseCases,
    private val coroutineScope: CoroutineScope
) : RecommendationEvents {

    override fun submitRecClicks(products: List<ProductList>) {
        if (coroutineScope.isActive) {
            coroutineScope.launch {
                recommendationUseCase.recClickUseCase()(products = products)
            }
        }
    }

    override fun submitRecImpressions(recTokens: List<String>) {
        if (coroutineScope.isActive) {
            coroutineScope.launch {
                recommendationUseCase.recImpressionUseCase()(recTokens = recTokens)
            }
        }
    }

    companion object :
        SingletonHolder<RecommendationAnalytics, RecommendationUseCases, CoroutineScope>(::RecommendationAnalytics)
}

class RecommendationUseCaseProvider : RecommendationUseCases {

    private var recClickUseCase: RecClickUseCase? = null
    private var recImpressionUseCase: RecImpressionUseCase? = null

    override fun recClickUseCase(): RecClickUseCase {
        if (recClickUseCase == null) {
            val repository: RecommendationsRepository = RecommendationsRepositoryImpl()
            recClickUseCase = RecClickUseCase(repository)
        }
        return recClickUseCase!!
    }

    override fun recImpressionUseCase(): RecImpressionUseCase {
        if (recImpressionUseCase == null) {
            val repository: RecommendationsRepository = RecommendationsRepositoryImpl()
            recImpressionUseCase = RecImpressionUseCase(repository)
        }
        return recImpressionUseCase!!
    }
}

open class SingletonHolder<out T, in A, in B>(private val constructor: (A, B) -> T) {

    @Volatile
    private var instance: T? = null

    fun getInstance(a: A, b: B): T = instance ?: synchronized(this) {
        instance ?: constructor(a, b).also { instance = it }
    }
}