package za.co.woolworths.financial.services.android.ui.fragments.card

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.replace_card_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.util.Utils

class GetReplacementCardFragment : MyCardExtension() {

    companion object {
        fun newInstance() = GetReplacementCardFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.replace_card_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { Utils.updateStatusBarBackground(it) }
        updateToolbarBg()
        tvAlreadyHaveCard?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
    }

    private fun updateToolbarBg() {
        (activity as? MyCardDetailActivity)?.apply {
            hideToolbarTitle()
            changeToolbarBackground(R.color.white)
        }
    }
}