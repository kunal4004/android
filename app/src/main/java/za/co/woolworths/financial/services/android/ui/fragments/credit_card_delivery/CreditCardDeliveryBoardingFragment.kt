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
import kotlinx.android.synthetic.main.credit_card_delivery_boarding_layout.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.activities.credit_card_delivery.CreditCardDeliveryActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.Utils

class CreditCardDeliveryBoardingFragment : Fragment() {

    private var navController: NavController? = null
    var bundle: Bundle? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_card_delivery_boarding_layout, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        setupToolbar()
        setUpDeliveryNow?.setOnClickListener {
            navController?.navigate(R.id.action_to_creditCardDeliveryRecipientDetailsFragment, bundleOf("bundle" to bundle))
        }
        activateNow?.setOnClickListener {
            activity?.apply { Utils.makeCall(WoolworthsApplication.getCreditCardDelivery().callCenterNumber) }
        }
    }

    private fun setupToolbar() {
        if (activity is CreditCardDeliveryActivity) {
            (activity as? CreditCardDeliveryActivity)?.apply {
                setToolbarTitle(bindString(R.string.my_card))
                changeToolbarBackground(R.color.grey_bg)
            }
        }
    }
}