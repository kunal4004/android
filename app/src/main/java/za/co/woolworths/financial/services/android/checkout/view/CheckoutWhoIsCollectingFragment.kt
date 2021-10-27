package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.checkout_new_user_recipient_details.*
import kotlinx.android.synthetic.main.vehicle_details_layout.*
import za.co.woolworths.financial.services.android.ui.extension.afterTextChanged
import za.co.woolworths.financial.services.android.ui.extension.bindString
import java.util.regex.Pattern

/**
 * Created by Kunal Uttarwar on 26/10/21.
 */
class CheckoutWhoIsCollectingFragment : CheckoutAddressManagementBaseFragment() {

    companion object {
        const val REGEX_VEHICLE_TEXT: String = "^\$|^[a-zA-Z0-9\\s<!>@#\$&().+,-/\\\"']+\$"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.checkout_who_is_collecting_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        recipientDetailsTitle.text = bindString(R.string.who_is_collecting)

        recipientNameEditText?.apply {
            afterTextChanged {
                val length = it.length
                if (length > 0 && !Pattern.matches(REGEX_VEHICLE_TEXT, it)) {
                    text?.delete(length - 1, length)
                }
                /*if (it.isNotEmpty())
                    showErrorInputField(this, View.GONE)*/
            }
        }

        vehicleColourEditText?.apply {
            afterTextChanged {
                val length = it.length
                if (length > 0 && !Pattern.matches(REGEX_VEHICLE_TEXT, it)) {
                    text?.delete(length - 1, length)
                }
                /*if (it.isNotEmpty())
                    showErrorInputField(this, View.GONE)*/
            }
        }

        vehicleModelEditText?.apply {
            afterTextChanged {
                val length = it.length
                if (length > 0 && !Pattern.matches(REGEX_VEHICLE_TEXT, it)) {
                    text?.delete(length - 1, length)
                }
                /*if (it.isNotEmpty())
                    showErrorInputField(this, View.GONE)*/
            }
        }

        vehicleRegistrationEditText?.apply {
            afterTextChanged {
                val length = it.length
                if (length > 0 && !Pattern.matches(REGEX_VEHICLE_TEXT, it)) {
                    text?.delete(length - 1, length)
                }
                /*if (it.isNotEmpty())
                    showErrorInputField(this, View.GONE)*/
            }
        }
    }
}