package za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.woolworths.financial.services.android.enhancedSubstitution.repository.ProductSubstitutionRepository

class ProductSubstitutionViewModelFactory(
        private val repository: ProductSubstitutionRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductSubstitutionViewModel::class.java)) {
            return ProductSubstitutionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}