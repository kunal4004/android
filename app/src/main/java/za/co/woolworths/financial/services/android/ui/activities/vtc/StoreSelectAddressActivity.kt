package za.co.woolworths.financial.services.android.ui.activities.vtc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_confirm_store.*

class StoreSelectAddressActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_select_address)

        ivNavigateBack?.setOnClickListener{
            onBackPressed()
        }

    }

    override fun onBackPressed() {
        finish()
    }
}