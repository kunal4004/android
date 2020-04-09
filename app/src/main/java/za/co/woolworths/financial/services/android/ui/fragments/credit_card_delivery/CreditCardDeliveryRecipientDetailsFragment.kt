package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_delivery_recipient_details_layout.*
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.UserDetailsForCreditCardDelivery
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardDeliveryRecipientDetailsFragment : Fragment(), View.OnClickListener {

    var navController: NavController? = null
    var bundle: Bundle? = null
    private var userDetails: UserDetailsForCreditCardDelivery? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_card_delivery_recipient_details_layout, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
        bundle?.apply {
            if (containsKey("UserDetails")) {
                userDetails = Utils.jsonStringToObject(getString("UserDetails"), UserDetailsForCreditCardDelivery::class.java) as UserDetailsForCreditCardDelivery
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        confirm?.setOnClickListener(this)
        configureUI()
    }

    fun configureUI() {
        recipientName.setText(SessionUtilities.getInstance().jwt?.name?.get(0))
        userDetails?.let {
            cellphoneNumber.setText(it.phoneNumber ?: "")
            alternativeNumber.setText(it.alternativeNumber ?: "")
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.confirm -> {
                if (userDetails == null)
                    userDetails = UserDetailsForCreditCardDelivery()
                userDetails?.let {
                    it.name = recipientName.text.toString()
                    it.phoneNumber = cellphoneNumber.text.toString()
                    it.alternativeNumber = alternativeNumber.text.toString()
                }
                bundle?.putString("UserDetails", Utils.toJson(userDetails))
                navController?.navigate(R.id.action_to_creditCardDeliveryAddressDetailsFragment, bundleOf("bundle" to bundle))
            }
        }
    }
}