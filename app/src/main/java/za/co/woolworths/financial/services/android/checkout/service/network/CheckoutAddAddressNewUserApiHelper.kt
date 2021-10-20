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
        OneAppService.addAddress(addAddressRequestBody).enqueue(CompletionHandler(object :
            IResponseListener<AddAddressResponse> {
            override fun onSuccess(response: AddAddressResponse?) {
                addAddressData.value = response ?: null
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
            override fun onSuccess(response: DeleteAddressResponse?) {
                deleteAddressData.value = response ?: null
            }

            override fun onFailure(error: Throwable?) {
                if (error != null) {
                    deleteAddressData.value = error!!
                }
            }

        }, DeleteAddressResponse::class.java))
        return deleteAddressData
    }

    fun editAddress(
        addAddressRequestBody: AddAddressRequestBody,
        addressId: String
    ): LiveData<Any> {
        val updateAddressData = MutableLiveData<Any>()
        OneAppService.editAddress(addAddressRequestBody, addressId)
            .enqueue(CompletionHandler(object :
                IResponseListener<AddAddressResponse> {
                override fun onSuccess(response: AddAddressResponse?) {
                    updateAddressData.value = response ?: null
                }

                override fun onFailure(error: Throwable?) {
                    if (error != null) {
                        updateAddressData.value = error!!
                    }
                }

            }, AddAddressResponse::class.java))
        return updateAddressData
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

    fun getShippingDetails(body: ShippingDetailsBody): LiveData<Any> {
        val shippingDetailsResp = MutableLiveData<Any>()
        OneAppService.getShippingDetails(body).enqueue(CompletionHandler(object :
            IResponseListener<ShippingDetailsResponse> {
            override fun onSuccess(shippingDetailsResponse: ShippingDetailsResponse?) {
                shippingDetailsResp.value = shippingDetailsResponse ?: null
            }

            override fun onFailure(error: Throwable?) {
                if (error != null) {
                    shippingDetailsResp.value = error!!
                }
            }

        }, ShippingDetailsResponse::class.java))
        return shippingDetailsResp
    }

    fun setConfirmSelection(confirmSelectionRequestBody: ConfirmSelectionRequestBody): LiveData<Any> {
        val confirmSelectionData = MutableLiveData<Any>()
        OneAppService.setConfirmSelection(confirmSelectionRequestBody)
            .enqueue(CompletionHandler(object :
                IResponseListener<ConfirmSelectionResponse> {
                override fun onSuccess(confirmSelectionResponse: ConfirmSelectionResponse?) {
                    confirmSelectionData.value = confirmSelectionResponse ?: null
                }

                override fun onFailure(error: Throwable?) {
                    if (error != null) {
                        confirmSelectionData.value = error!!
                    }
                }
            }, ConfirmSelectionResponse::class.java))
        return confirmSelectionData
    }
}