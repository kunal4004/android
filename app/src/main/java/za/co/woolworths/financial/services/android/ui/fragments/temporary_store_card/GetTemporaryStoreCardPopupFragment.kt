package za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.get_temp_store_card_popup_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.npc.Transition
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardActivityExtension
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.activities.temporary_store_card.HowToUseTemporaryStoreCardActivity
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.npc.EnterOtpFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.LinkStoreCardFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ErrorDialogFragment
import za.co.woolworths.financial.services.android.util.Utils

class GetTemporaryStoreCardPopupFragment : Fragment(), View.OnClickListener {
    private var mStoreCardDetail: String? = null
    private var mStoreCardsResponse: StoreCardsResponse? = null

    companion object {
        fun newInstance(storeCardDetail: String?) = GetTemporaryStoreCardPopupFragment().withArgs {
            putString(MyCardDetailActivity.STORE_CARD_DETAIL, storeCardDetail)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.apply {
            mStoreCardDetail = getString(MyCardDetailActivity.STORE_CARD_DETAIL, "")

            activity?.let {
                Utils.updateStatusBarBackground(it, R.color.grey_bg)
                mStoreCardDetail?.let { cardValue ->
                    mStoreCardsResponse = Gson().fromJson(cardValue, StoreCardsResponse::class.java)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.get_temp_store_card_popup_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getTempStoreCardButton.setOnClickListener(this)
        (activity as? MyCardActivityExtension)?.apply {
            showBackIcon()
        }
        howItWorks?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener(this@GetTemporaryStoreCardPopupFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.getTempStoreCardButton -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MY_ACCOUNTS_VTC_GET)
                when (mStoreCardsResponse?.oneTimePinRequired?.linkVirtualStoreCard) {
                    true -> navigateToOTPFragment()
                    else -> navigateToLinkCardFragment()
                }
            }
            R.id.howItWorks -> {
                activity?.apply {
                    Intent(this, HowToUseTemporaryStoreCardActivity::class.java).let {
                        it.putExtra(HowToUseTemporaryStoreCardActivity.TRANSACTION_TYPE, Transition.SLIDE_UP)
                        startActivity(it)
                    }
                    overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
                }
            }

        }
    }

    private fun navigateToLinkCardFragment() {
        replaceFragment(
                fragment = LinkStoreCardFragment.newInstance(),
                tag = LinkStoreCardFragment::class.java.simpleName,
                containerViewId = R.id.flMyCard,
                allowStateLoss = true,
                enterAnimation = R.anim.slide_in_from_right,
                exitAnimation = R.anim.slide_to_left,
                popEnterAnimation = R.anim.slide_from_left,
                popExitAnimation = R.anim.slide_to_right
        )
    }

    fun navigateToOTPFragment() {
        replaceFragment(
                fragment = EnterOtpFragment.newInstance(),
                tag = EnterOtpFragment::class.java.simpleName,
                containerViewId = R.id.flMyCard,
                allowStateLoss = true,
                enterAnimation = R.anim.slide_in_from_right,
                exitAnimation = R.anim.slide_to_left,
                popEnterAnimation = R.anim.slide_from_left,
                popExitAnimation = R.anim.slide_to_right
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showTempStoreCardProgressBar(state: Int) {
        activity?.apply {
            getTempStoreCardProgressBar.indeterminateDrawable.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
            getTempStoreCardProgressBar.visibility = state
        }
    }

    fun showErrorDialog(errorMessage: String) {
        val dialog = ErrorDialogFragment.newInstance(errorMessage)
        (activity as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()?.let { fragmentTransaction -> dialog.show(fragmentTransaction, ErrorDialogFragment::class.java.simpleName) }
    }
}