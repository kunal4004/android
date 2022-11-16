package za.co.woolworths.financial.services.android.ui.fragments.click_and_collect

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.awfs.coordination.R
import com.awfs.coordination.databinding.EditDeliveryLocationConfirmationFragmentBinding
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.Province
import za.co.woolworths.financial.services.android.models.dto.Suburb
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DELIVERY_TYPE
import za.co.woolworths.financial.services.android.util.DeliveryType
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class EditDeliveryLocationConfirmationFragment : BaseFragmentBinding<EditDeliveryLocationConfirmationFragmentBinding>(EditDeliveryLocationConfirmationFragmentBinding::inflate) {
    var selectedSuburb: Suburb? = null
    var selectedProvince: Province? = null
    var deliveryType: DeliveryType? = null
    var bundle: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
        bundle?.apply {
            deliveryType = DeliveryType.valueOf(getString(DELIVERY_TYPE, DeliveryType.DELIVERY.name))
            selectedSuburb = Utils.jsonStringToObject(getString("SUBURB"), Suburb::class.java) as Suburb?
            selectedProvince = Utils.jsonStringToObject(getString("PROVINCE"), Province::class.java) as Province?
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            deliveryOptionImage?.setBackgroundResource(if (deliveryType == DeliveryType.STORE_PICKUP) R.drawable.icon_basket else R.drawable.icon_delivery)
            deliveryOption?.text =
                activity?.resources?.getString(if (deliveryType == DeliveryType.DELIVERY) R.string.delivering_to else R.string.collecting_from)
            suburbName?.text =
                if (deliveryType == DeliveryType.STORE_PICKUP) activity?.resources?.getString(R.string.store) + selectedSuburb?.name else selectedSuburb?.name + ", " + selectedProvince?.name
            address?.text =
                if (deliveryType == DeliveryType.STORE_PICKUP) selectedSuburb?.storeAddress?.let { it.address1 + ", " + it.address2 } else ""
            AppConfigSingleton.clickAndCollect?.maxItemsAllowedText?.let {
                maxItemsInfoMessageLayout.maxItemsInfoMessage.text = it
            }
            maxItemsInfoMessageLayout.root.visibility =
                if (deliveryType == DeliveryType.STORE_PICKUP && !AppConfigSingleton.clickAndCollect?.maxItemsAllowedText.isNullOrEmpty()) View.VISIBLE else View.GONE
            dismissActivity()
        }
    }

    private fun dismissActivity() {
        Handler().postDelayed({
            activity?.apply {
                setResult(RESULT_OK)
                finish()
            }
        }, 2000)
    }
}