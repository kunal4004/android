package za.co.woolworths.financial.services.android.checkout.service.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import retrofit2.Response
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.ValidateSelectedSuburbResponse
import za.co.woolworths.financial.services.android.models.dto.SuburbsResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.ConfirmDeliveryAddressBody
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig

/**
 * Created by Kunal Uttarwar on 04/06/21.
 */
class CheckoutAddAddressNewUserApiHelper : RetrofitConfig() {

    fun getSuburbs(provinceId: String): Response<SuburbsResponse> =
        OneAppService.getSuburbs(provinceId).execute()

    fun validateSelectedSuburb(
        suburbId: String,
        isStore: Boolean
    ): Response<ValidateSelectedSuburbResponse> =
        OneAppService.validateSelectedSuburb(suburbId, isStore).execute()

    fun addAddress(addAddressRequestBody: AddAddressRequestBody): LiveData<Any> {
        val addAddressData = MutableLiveData<Any>()
        OneAppService.addAddress(addAddressRequestBody)?.enqueue(CompletionHandler(object :
            IResponseListener<AddAddressResponse> {
            override fun onSuccess(addAddressResponse: AddAddressResponse?) {
                addAddressData.value = addAddressResponse ?: null
            }

            override fun onFailure(error: Throwable?) {
                if (error != null) {
                    addAddressData.value = error!!
                }
            }

        }, AddAddressResponse::class.java))
        return addAddressData
    }

    fun deleteAddress(addressId: String): LiveData<Any> {
        val deleteAddressData = MutableLiveData<Any>()
        OneAppService.deleteAddress(addressId).enqueue(CompletionHandler(object :
            IResponseListener<DeleteAddressResponse> {
            override fun onSuccess(deleteAddressResponse: DeleteAddressResponse?) {
                deleteAddressData.value = deleteAddressResponse ?: null
            }

            override fun onFailure(error: Throwable?) {
                if (error != null) {
                    deleteAddressData.value = error!!
                }
            }

        }, DeleteAddressResponse::class.java))
        return deleteAddressData
    }

    fun changeAddress(nickName: String): LiveData<Any> {
        val changeAddressLiveData = MutableLiveData<Any>()
        OneAppService.changeAddress(nickName).enqueue(CompletionHandler(object :
            IResponseListener<ChangeAddressResponse> {
            override fun onSuccess(changeAddressResponse: ChangeAddressResponse?) {
                changeAddressLiveData.value = changeAddressResponse ?: null
            }

            override fun onFailure(error: Throwable?) {
                if (error != null) {
                    changeAddressLiveData.value = error!!
                }
            }

        }, ChangeAddressResponse::class.java))

        return changeAddressLiveData
    }

    fun getConfirmDeliveryAddressDetails(body: ConfirmDeliveryAddressBody): LiveData<Any> {
        val confirmDeliveryAddress = MutableLiveData<Any>()
        OneAppService.getConfirmDeliveryAddressDetails(body).enqueue(CompletionHandler(object :
            IResponseListener<ConfirmDeliveryAddressResponse> {
            override fun onSuccess(confirmDeliveryAddressResponse: ConfirmDeliveryAddressResponse?) {
                confirmDeliveryAddress.value = confirmDeliveryAddressResponse ?: null
            }

            override fun onFailure(error: Throwable?) {
                if (error != null) {
                    confirmDeliveryAddress.value = error!!
                }
            }

        }, ConfirmDeliveryAddressResponse::class.java))
        return confirmDeliveryAddress

    }
}