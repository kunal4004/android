package za.co.woolworths.financial.services.android.ui.activities.temporary_store_card

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.awfs.coordination.databinding.TempCardHowToUseLayoutBinding
import za.co.woolworths.financial.services.android.models.dto.npc.Transition
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.VirtualCardStaffMemberMessage
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.spannable.WSpannableStringBuilder
import za.co.woolworths.financial.services.android.util.wenum.LinkType


class HowToUseTemporaryStoreCardActivity : AppCompatActivity() {

    companion object {
        var TRANSACTION_TYPE = "TRANSACTION_TYPE"
        var STAFF_DISCOUNT_INFO = "STAFF_DISCOUNT_INFO"
    }

    private lateinit var binding: TempCardHowToUseLayoutBinding
    private var type: Transition = Transition.SLIDE_LEFT
    private var virtualCardStaffMemberMessage: VirtualCardStaffMemberMessage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TempCardHowToUseLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Utils.updateStatusBarBackground(this)
        type = intent?.extras?.getSerializable(TRANSACTION_TYPE) as Transition
        if(intent?.hasExtra(STAFF_DISCOUNT_INFO) == true){
            virtualCardStaffMemberMessage = intent?.extras?.getSerializable(STAFF_DISCOUNT_INFO) as VirtualCardStaffMemberMessage?
        }
        actionBar()

        with(binding) {
            val howToUseSpannableStringBuilder =
                WSpannableStringBuilder(getString(R.string.temp_card_how_to_use6))
            howToUseSpannableStringBuilder.makeStringInteractable(
                "appfeedback@woolworths.co.za",
                LinkType.EMAIL
            )
            howToUseSpannableStringBuilder.makeStringUnderlined("appfeedback@woolworths.co.za")
            setUnderlineText(howToUseSpannableStringBuilder.build(), howToUse6)

            val howToUse8SpannableContent =
                WSpannableStringBuilder(getString(R.string.temp_card_how_to_use12))
            howToUse8SpannableContent.makeStringInteractable("queries@wfs.co.za", LinkType.EMAIL)
            howToUse8SpannableContent.makeStringUnderlined("queries@wfs.co.za")
            howToUse8SpannableContent.makeStringInteractable("0861 50 20 20", LinkType.PHONE)
            howToUse8SpannableContent.makeStringUnderlined("0861 50 20 20")
            setUnderlineText(howToUse8SpannableContent.build(), howToUse12)

            virtualCardStaffMemberMessage.let {
                if (!virtualCardStaffMemberMessage?.paragraphs.isNullOrEmpty() &&
                    virtualCardStaffMemberMessage?.paragraphs?.size == 3
                ) {
                    staffMessage1CheckBox.visibility = View.VISIBLE
                    staffMessage2CheckBox.visibility = View.VISIBLE
                    staffMessage3CheckBox.visibility = View.VISIBLE

                    staffMessage1CheckBox.text = virtualCardStaffMemberMessage?.paragraphs?.get(0)
                    staffMessage2CheckBox.text = virtualCardStaffMemberMessage?.paragraphs?.get(1)
                    staffMessage3CheckBox.text = virtualCardStaffMemberMessage?.paragraphs?.get(2)
                }
            }

            setUniqueIds()
        }
    }

    private fun setUnderlineText(howToUseSpannableContent: Spannable, textView: TextView?) {
        textView?.text = howToUseSpannableContent
        textView?.movementMethod = LinkMovementMethod.getInstance()
        textView?.highlightColor = Color.TRANSPARENT
    }

    private fun actionBar() {
        setSupportActionBar(binding.tbHowToUse)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            if (type == Transition.SLIDE_LEFT) {
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.back24)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (type != Transition.SLIDE_LEFT)
            menuInflater?.inflate(R.menu.search_item, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        this.finish()
        if (type == Transition.SLIDE_LEFT)
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
        else
            overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search, android.R.id.home -> onBackPressed()
        }
        return false
    }

    private fun setUniqueIds() {
        binding.howItWorksTitleTextView?.contentDescription = bindString(R.string.how_to_pay_toolbar_title)
    }
}

