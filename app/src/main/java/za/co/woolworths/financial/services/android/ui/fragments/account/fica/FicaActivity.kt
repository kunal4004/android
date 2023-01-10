package za.co.woolworths.financial.services.android.ui.fragments.account.fica

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ActivityFicaBinding
import com.awfs.coordination.databinding.FicaDialogBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.base.BaseActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BindingBaseActivity
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils

@AndroidEntryPoint
class FicaActivity : BindingBaseActivity<ActivityFicaBinding>(ActivityFicaBinding::inflate), View.OnClickListener {
    private val ficaViewModel: FicaViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ficaViewModel.start(intent)
        setViews()
    }

    private fun setViews() {
        setClickListeners(binding.ficaDialog.btnFicaMaybeLater,binding.ficaDialog.btnFicaVerify)
        setUpActionBar(binding.toolbarCreditReport)
    }

    override fun onClick(view: View?) {
        binding.ficaDialog.let {
        when (view) {
            it.btnFicaMaybeLater -> {
                onBackPressedDispatcher.onBackPressed()
            }
            it.btnFicaVerify -> {
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.FICA_VERIFY_START,
                    this
                );
                ficaViewModel.handleVerify(this)
            }
        }
        }
    }
    override fun onBackPressed() {
        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.FICA_VERIFY_SKIP,
            this
        )
        onBackPressedDispatcher.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            KotlinUtils.RESULT_CODE_CLOSE_VIEW->{
                when(resultCode){
                    RESULT_OK->{
                        finish()
                    }
                }
            }
        }
    }
}