package za.co.woolworths.financial.services.android.chanel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import za.co.woolworths.financial.services.android.chanel.services.repository.ChanelRepository
import za.co.woolworths.financial.services.android.chanel.utils.ChanelResource

class ChanelViewModel(private val chanelRepository: ChanelRepository) : ViewModel() {

    fun getChanelResposne(
        searchTerm: String,
        searhType: String,
        filterContent: Boolean
    ) = liveData(Dispatchers.IO) {
        emit(ChanelResource.loading(data = null))
        try {
            emit(
                ChanelResource.success(
                    data = chanelRepository.getChanelBannerData(
                        searchTerm, searhType, filterContent
                    )
                )
            )
        } catch (exception: Exception) {
            emit(ChanelResource.error(data = null, msg = exception.toString()))
        }
    }
}
