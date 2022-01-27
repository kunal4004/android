package za.co.woolworths.financial.services.android.chanel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import za.co.woolworths.financial.services.android.chanel.services.repository.ChanelRepository
import za.co.woolworths.financial.services.android.chanel.utils.ChanelResource

class ChanelViewModel(private val chanelRepository: ChanelRepository): ViewModel() {

    fun getChanelResposne() = liveData(Dispatchers.IO) {
        emit(ChanelResource.loading(data = null))
        try {
            // this values are added for testing purpose.
            // later this will be removed once view part is added
            emit(ChanelResource.success(data = chanelRepository.getChanelBannerData(
                "chanel", "navigate", false
            )))
        } catch (exception: Exception) {
            emit(ChanelResource.error(data = null, msg = exception.toString()))
        }
    }
}