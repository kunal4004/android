package za.co.woolworths.financial.services.android.ui.fragments.product.detail.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.component.MatchingSetData
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.service.MatchingSetRepository
import javax.inject.Inject

/**
 * Created by Kunal Uttarwar on 06/02/24.
 */

@HiltViewModel
class MatchingSetViewModel @Inject constructor(private val matchingSetRepository: MatchingSetRepository) :
    ViewModel() {

    val matchingSetData =
        mutableStateOf(MatchingSetData(arrayListOf(), emptyList(), emptyList(), emptyList(), emptyList()))

}