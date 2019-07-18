package za.co.woolworths.financial.services.android.ui.fragments.shop.list

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SingleButtonDialogFragment
import za.co.woolworths.financial.services.android.util.NetworkManager

open class DepartmentExtensionFragment : Fragment() {

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
                inputManager.hideSoftInputFromWindow(currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }

    fun convertStringToObject(mAddToListArgs: String?) = Gson().fromJson<MutableList<AddToListRequest>>(mAddToListArgs, object : TypeToken<MutableList<AddToListRequest>>() {}.type)!!

    fun showErrorDialog(message: String) {
        activity?.let {
            val fm = it.supportFragmentManager
            val singleButtonDialogFragment = SingleButtonDialogFragment.newInstance(message)
            singleButtonDialogFragment.show(fm, SingleButtonDialogFragment::class.java.simpleName)
        }
    }

    fun cancelRequest(call: Call<*>?){
        call?.apply {
            if (!isCanceled)
                cancel()
        }
    }

    fun networkConnectionAvailable(it: FragmentActivity) = NetworkManager.getInstance().isConnectedToNetwork(it)
}