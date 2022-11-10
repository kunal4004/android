package za.co.woolworths.financial.services.android.ui.activities.voc

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ActivityVoiceOfCustomerBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.views.actionsheet.GenericActionOrCancelDialogFragment
import za.co.woolworths.financial.services.android.util.KeyboardUtils
import za.co.woolworths.financial.services.android.util.Utils

class VoiceOfCustomerActivity : AppCompatActivity(R.layout.activity_voice_of_customer), VoiceOfCustomerInterface, GenericActionOrCancelDialogFragment.IActionOrCancel {

    companion object {
        const val EXTRA_SURVEY_DETAILS = "extraSurveyDetails"
        const val EXTRA_SURVEY_ANSWERS = "extraSurveyAnswers"
        const val DIALOG_SKIP_ID = 1
        const val DEFAULT_VALUE_RATE_SLIDER_MIN = 1
        const val DEFAULT_VALUE_RATE_SLIDER_MAX = 11
    }

    private lateinit var binding: ActivityVoiceOfCustomerBinding
    private var navigationHost: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)

        Utils.updateStatusBarBackground(this)
        binding = ActivityVoiceOfCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val vocNavHostFrag = supportFragmentManager.findFragmentById(R.id.vocNavHostFrag) as NavHostFragment?
        if (vocNavHostFrag != null) {
            navigationHost = vocNavHostFrag.navController
        }

        setActionBar()
        setNavHostStartDestination()

        // Hide keyboard in case it was visible from a previous screen
        KeyboardUtils.hideKeyboardIfVisible(this)
    }

    private fun setActionBar() {
        setSupportActionBar(binding.vocToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    private fun setNavHostStartDestination() {
        val graph = navigationHost?.graph ?: return
        graph.startDestination = R.id.surveyVocFragment
        navigationHost?.setGraph(graph, if (intent != null) intent.extras else null)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return false
    }

    override fun onBackPressed() {
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.VOC_SKIP, this)
        val currentDestination = navigationHost?.currentDestination ?: return
        when (currentDestination.id) {
            R.id.surveyVocFragment -> finishActivity()
        }
    }

    fun finishActivity() {
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    override fun setToolbarSkipVisibility(show: Boolean) {
        binding.apply {
            if (vocToolbar == null) return
            tvSkipSurvey.visibility = if (show) View.VISIBLE else View.GONE
            tvSkipSurvey.setOnClickListener { onSkipSurveyClicked() }
        }
    }

    fun hideToolbarBackButton() {
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun onSkipSurveyClicked() {
        val dialog = GenericActionOrCancelDialogFragment.newInstance(
                dialogId = DIALOG_SKIP_ID,
                title = getString(R.string.voc_skip_dialog_title),
                desc = getString(R.string.voc_skip_dialog_desc),
                actionButtonText = getString(R.string.voc_skip_dialog_action),
                cancelButtonText = getString(R.string.voc_skip_dialog_cancel),
                this
        )
        dialog.show(supportFragmentManager, GenericActionOrCancelDialogFragment::class.java.simpleName)
    }

    override fun onDialogActionClicked(dialogId: Int) {
        if (dialogId == DIALOG_SKIP_ID) {
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.VOC_SKIP, this)
            finishActivity()
        }
    }
}