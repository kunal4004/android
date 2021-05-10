package za.co.woolworths.financial.services.android.ui.activities.vtc

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_confirm_store.*
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.models.dto.StoreOfferings
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.WFormatter
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import java.util.*

class ConfirmStoreActivity : AppCompatActivity() {


    private var storeDetails: StoreDetails? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_store)

        storeDetails = Gson().fromJson(intent.getStringExtra("store"), StoreDetails::class.java)

        storeDetails?.apply {
            tvStoreName?.text = name
            tvStoreDesc?.text = WFormatter.formatOfferingString(getOfferingByType(offerings, "Department"))
            tvStoreAddress?.text = address
        }

        ivNavigateBack?.setOnClickListener{
            onBackPressed()
        }

        tvConfirmStoreBtn?.setOnClickListener {
            makeStoreConfirmationCall()
        }
        AnimationUtilExtension.animateViewPushDown(tvConfirmStoreBtn)
    }

    private fun getOfferingByType(offerings: List<StoreOfferings>, type: String?): List<StoreOfferings>? {
        val list: MutableList<StoreOfferings> = ArrayList()
        list.clear()
        for (d in offerings) {
            if (d.type != null && d.type.contains(type!!)) list.add(d)
        }
        return list
    }

    private fun makeStoreConfirmationCall() {

        if (!NetworkManager.getInstance().isConnectedToNetwork(this)) {
            return
        }

        showConfirmationProcessing()

    }

    private fun showConfirmationProcessing() {


    }

    override fun onBackPressed() {
        finish()
    }
}