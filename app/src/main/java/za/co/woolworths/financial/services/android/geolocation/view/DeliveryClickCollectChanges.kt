package za.co.woolworths.financial.services.android.geolocation.view


import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.checkout_address_confirmation.*
import kotlinx.android.synthetic.main.delivery_or_click_and_collect_selector_dialog.*
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.util.DeliveryType
import za.co.woolworths.financial.services.android.util.Utils


class DeliveryClickCollectChanges : Fragment() {

    companion object {
        fun newInstance() = DeliveryClickCollectChanges()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.geolocation_confirm_address, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        collectionTab?.setOnClickListener(View.OnClickListener {  })
        deliveryTab?.setOnClickListener(View.OnClickListener {  })

    }

    fun onClick(v: View?) {
        when (v?.id) {

            R.id.deliveryTab -> {

            }
            R.id.collectionTab -> {

            }
        }
    }

}