package za.co.woolworths.financial.services.android.chanel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.woolworths.financial.services.android.chanel.services.repository.ChanelRepository

class ChanelViewModelFactory(private val chanelRepository: ChanelRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChanelViewModel::class.java)) {
            return ChanelViewModel(chanelRepository) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}