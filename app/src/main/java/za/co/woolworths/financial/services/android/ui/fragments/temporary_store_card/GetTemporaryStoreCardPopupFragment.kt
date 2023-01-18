package za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.awfs.coordination.databinding.GetTempStoreCardPopupFragmentBinding
import com.google.gson.Gson
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
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class GetTemporaryStoreCardPopupFragment : BaseFragmentBinding<GetTempStoreCardPopupFragmentBinding>(GetTempStoreCardPopupFragmentBinding::inflate), View.OnClickListener {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.getTempStoreCardButton.setOnClickListener(this)
        (activity as? MyCardActivityExtension)?.apply {
            Utils.updateStatusBarBackground(this)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.icon_info)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.getTempStoreCardButton -> {
                activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MY_ACCOUNTS_VTC_GET, this) }
                when (mStoreCardsResponse?.oneTimePinRequired?.linkVirtualStoreCard) {
                    true -> navigateToOTPFragment()
                    else -> navigateToLinkCardFragment()
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.close_menu_item, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.closeIcon -> {
                activity?.onBackPressed()
                super.onOptionsItemSelected(item)
            }
            android.R.id.home ->{
                activity?.apply {

                    Intent(this, HowToUseTemporaryStoreCardActivity::class.java).let {
                        it.putExtra(HowToUseTemporaryStoreCardActivity.TRANSACTION_TYPE, Transition.SLIDE_UP)
                        startActivity(it)
                    }
                    overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
                }
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun showErrorDialog(errorMessage: String) {
        val dialog = ErrorDialogFragment.newInstance(errorMessage)
        (activity as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()?.let { fragmentTransaction -> dialog.show(fragmentTransaction, ErrorDialogFragment::class.java.simpleName) }
    }
}