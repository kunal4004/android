package za.co.woolworths.financial.services.android.ui.activities.voc

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_voice_of_customer.*
import za.co.woolworths.financial.services.android.ui.views.actionsheet.GenericActionOrCancelDialogFragment
import za.co.woolworths.financial.services.android.util.KeyboardUtils
import za.co.woolworths.financial.services.android.util.Utils

class VoiceOfCustomerActivity : AppCompatActivity(), VoiceOfCustomerInterface, GenericActionOrCancelDialogFragment.IActionOrCancel {

    companion object {
        const val DIALOG_SKIP_ID = 1
    }

    private var navigationHost: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: add param to be either right to left, or bottom to top
//        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
        overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)

        Utils.updateStatusBarBackground(this)
        setContentView(R.layout.activity_voice_of_customer)

        val vocNavHostFrag = supportFragmentManager.findFragmentById(R.id.vocNavHostFrag) as NavHostFragment?
        if (vocNavHostFrag != null) {
            navigationHost = vocNavHostFrag.navController
        }

        setActionBar()
        setNavHostStartDestination()

        // Hide keyboard in case it was visible from a previous screen
        KeyboardUtils.hideKeyboard(this)
    }

    private fun setActionBar() {
        setSupportActionBar(vocToolbar)
        val mActionBar = supportActionBar
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true)
            mActionBar.setDisplayShowTitleEnabled(false)
            mActionBar.setDisplayUseLogoEnabled(false)
            mActionBar.setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    private fun setNavHostStartDestination() {
        if (navigationHost == null) {
            return
        }
        val graph = navigationHost!!.graph
        graph.startDestination = R.id.surveyVocFragment
        navigationHost!!.setGraph(graph, if (intent != null) intent.extras else null)
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
        if (navigationHost!!.currentDestination == null) return
        when (navigationHost!!.currentDestination!!.id) {
            R.id.surveyVocFragment -> finishActivity()
            else -> navigationHost!!.popBackStack()
        }
    }

    private fun finishActivity() {
        finish()
        // TODO: add param to be either left to right, or top to bottom
//        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    override fun setToolbarSkipVisibility(show: Boolean) {
        if (vocToolbar == null) return
        tvSkipSurvey.visibility = if (show) View.VISIBLE else View.GONE
        tvSkipSurvey.setOnClickListener { onSkipSurveyClicked() }
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
            Toast.makeText(this, "On Skip Survey Clicked", Toast.LENGTH_SHORT).show()
        }
    }
}