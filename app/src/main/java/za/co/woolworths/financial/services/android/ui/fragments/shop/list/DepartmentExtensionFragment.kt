package za.co.woolworths.financial.services.android.ui.fragments.shop.list

import android.content.Context
import android.location.Location
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.adapters.DepartmentAdapter
import za.co.woolworths.financial.services.android.ui.fragments.shop.ShopFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SingleButtonDialogFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.Utils

abstract class DepartmentExtensionFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

    private var rootCategoryCall: Call<RootCategories>? = null
    var version: String? = ""

    fun showKeyboard(editText: EditText) {
        editText.requestFocus()
        activity?.let {
            editText.requestFocus()
            editText.isFocusableInTouchMode = true
            val imm = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun hideKeyboard() {
        activity?.apply {
            val inputManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // check if no view has focus:
            val currentFocusedView = currentFocus
            if (currentFocusedView != null) {
                inputManager.hideSoftInputFromWindow(
                    currentFocusedView.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        }
    }

    fun convertStringToObject(mAddToListArgs: String?) =
        Gson().fromJson<MutableList<AddToListRequest>>(
            mAddToListArgs,
            object : TypeToken<MutableList<AddToListRequest>>() {}.type
        )!!

    fun showErrorDialog(message: String) {
        activity?.let {
            val fm = it.supportFragmentManager
            val singleButtonDialogFragment = SingleButtonDialogFragment.newInstance(message)
            singleButtonDialogFragment?.show(fm, SingleButtonDialogFragment::class.java.simpleName)
        }
    }

    fun cancelRequest(call: Call<*>?) {
        call?.apply {
            if (!isCanceled)
                cancel()
        }
    }

    fun networkConnectionStatus(): Boolean =
        activity?.let { NetworkManager.getInstance().isConnectedToNetwork(it) }
            ?: false

    abstract fun noConnectionLayout(isVisible: Boolean)

    fun executeDepartmentRequest(mDepartmentAdapter: DepartmentAdapter?, parentFragment: ShopFragment?, location: Location?=null) {
        if (networkConnectionStatus()) {
            noConnectionLayout(false)
            val isLocationEnabled = if (context != null) Utils.isLocationEnabled(context) else false
            rootCategoryCall = OneAppService().getRootCategory(
                isLocationEnabled,
                location,
                KotlinUtils.browsingDeliveryType?.type
            )
            rootCategoryCall?.enqueue(CompletionHandler(object : IResponseListener<RootCategories> {
                override fun onSuccess(response: RootCategories?) {
                    when (response?.httpCode) {
                        200 -> {
                            version = response.response?.version
                            parentFragment?.setCategoryResponseData(response)
                            bindDepartment(mDepartmentAdapter, parentFragment)
                        }
                        else -> response?.response?.desc?.let { showErrorDialog(it) }
                    }
                }

                override fun onFailure(error: Throwable?) {
                    if (isAdded) {
                        activity?.runOnUiThread {
                            if (networkConnectionStatus())
                                noConnectionLayout(true)
                        }
                    }
                }
            }, RootCategories::class.java))
        } else {
            noConnectionLayout(true)
        }
    }

    fun bindDepartment(mDepartmentAdapter: DepartmentAdapter?, parentFragment: ShopFragment?) {
        mDepartmentAdapter?.setRootCategories(parentFragment?.getCategoryResponseData()?.rootCategories)
        mDepartmentAdapter?.notifyDataSetChanged()
    }

    fun networkConnectionAvailable(it: FragmentActivity) =
        NetworkManager.getInstance().isConnectedToNetwork(it)
}