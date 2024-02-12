package za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.domain.usecases

interface BaseUseCase<In, Out> {
    fun execute(input: In): Out
}