package za.co.woolworths.financial.services.android.ui.fragments.card

import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.link_card_fragment.*


class LinkCardFragment : MyCardExtension() {


    companion object {
        fun newInstance() = LinkCardFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.link_card_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvReplacementCardInfo?.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        textEvent()

    }

    private fun textEvent() {
        etCardNumber?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                imNext?.alpha = if (etCardNumber?.length() != 0) 1.0f else 0.0f
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
    }
}