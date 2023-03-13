package za.co.woolworths.financial.services.android.recommendations.presentation

import kotlinx.coroutines.*
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.Product
import za.co.woolworths.financial.services.android.recommendations.presentation.usecase.RecClickUseCase
import javax.inject.Inject
import javax.inject.Singleton

interface RecommendationLogger {
    fun submitRecClicks(products: List<Product>)
}

@Singleton
class RecommendationLoggerImpl @Inject constructor(
    private val recClickUseCase: RecClickUseCase
) : RecommendationLogger {
    private val externalScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun submitRecClicks(products: List<Product>) {
        if (externalScope.isActive) {
            externalScope.launch {
                recClickUseCase(products = products)
            }
        }
    }
}

