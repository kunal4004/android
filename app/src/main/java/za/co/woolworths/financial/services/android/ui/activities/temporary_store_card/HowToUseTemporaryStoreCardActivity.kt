package za.co.woolworths.financial.services.android.ui.activities.temporary_store_card

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.temp_card_how_to_use_layout.*
import za.co.woolworths.financial.services.android.models.dto.npc.Transition
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.spannable.WSpannableStringBuilder
import za.co.woolworths.financial.services.android.util.wenum.LinkType


class HowToUseTemporaryStoreCardActivity : AppCompatActivity() {

    companion object {
        var TRANSACTION_TYPE = "TRANSACTION_TYPE"
    }

    var type: Transition = Transition.SLIDE_LEFT
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.temp_card_how_to_use_layout)
        Utils.updateStatusBarBackground(this)
        type = intent?.extras?.getSerializable(TRANSACTION_TYPE) as Transition
        actionBar()

        val howToUseSpannableStringBuilder = WSpannableStringBuilder(getString(R.string.temp_card_how_to_use5))
        howToUseSpannableStringBuilder.makeStringInteractable("appfeedback@woolworths.co.za", LinkType.EMAIL)
        howToUseSpannableStringBuilder.makeStringUnderlined("appfeedback@woolworths.co.za")
        setUnderlineText(howToUseSpannableStringBuilder.build(), howToUse5)

        val howToUse8SpannableContent = WSpannableStringBuilder(getString(R.string.temp_store_card_contact_customer_service_desc))
        howToUse8SpannableContent.makeStringInteractable("queries@wfs.co.za", LinkType.EMAIL)
        howToUse8SpannableContent.makeStringUnderlined("queries@wfs.co.za")
        howToUse8SpannableContent.makeStringInteractable("0861 50 20 20", LinkType.PHONE)
        howToUse8SpannableContent.makeStringUnderlined("0861 50 20 20")
        setUnderlineText(howToUse8SpannableContent.build(), howToUse10)

        setUniqueIds()

    }

    private fun setUnderlineText(howToUseSpannableContent: Spannable, textView: TextView?) {
        textView?.text = howToUseSpannableContent
        textView?.movementMethod = LinkMovementMethod.getInstance()
        textView?.highlightColor = Color.TRANSPARENT
    }

    private fun actionBar() {
        setSupportActionBar(tbHowToUse)
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
            howItWorksTitleTextView?.contentDescription = bindString(R.string.how_to_pay_toolbar_title)
    }
}

