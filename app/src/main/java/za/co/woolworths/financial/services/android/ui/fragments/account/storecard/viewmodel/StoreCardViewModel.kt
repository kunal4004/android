package za.co.woolworths.financial.services.android.ui.fragments.account.storecard.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.storecard.data.repository.StoreCardRepository
import javax.inject.Inject

@HiltViewModel
class StoreCardViewModel @Inject constructor(private val repository: StoreCardRepository) :
    ViewModel()  {
//    val toDo = repository.fetchService()

}