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
import kotlinx.android.synthetic.main.credit_card_delivery_recipient_address_layout.*
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.UserDetailsForCreditCardDelivery

class CreditCardDeliveryAddressDetailsFragment : Fragment(), View.OnClickListener {

    var navController: NavController? = null
    var bundle: Bundle? = null
    var userDetails: UserDetailsForCreditCardDelivery? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_card_delivery_recipient_address_layout, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        submitAddress?.setOnClickListener(this)
        configureUI()
    }

    private fun configureUI() {

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.submitAddress -> {
                navController?.navigate(R.id.action_to_creditCardDeliveryValidateAddressRequestFragment, bundleOf("bundle" to bundle))

            }
        }
    }
}