package za.co.woolworths.financial.services.android.ui.fragments.card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.my_card_fragment.*
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.Utils

class MyCardDetailFragment : MyCardExtension() {

    companion object {
        fun newInstance() = MyCardDetailFragment().withArgs {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity?.let { Utils.updateStatusBarBackground(it, R.color.grey_bg) }
        return inflater.inflate(R.layout.my_card_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateView()
        onClick()
    }

    private fun populateView() {
        tvCardNumberValue?.text = maskedCardNumberWithSpaces("30394938244039012")
        tvCardHolderValue?.text = "Thuli Sandile"
    }

    private fun onClick() {
        blockCardView.setOnClickListener { navigateToBlockMyCardActivity(activity) }
    }
}