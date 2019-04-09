package za.co.woolworths.financial.services.android.ui.fragments.card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R


class LinkCardFragment : MyCardExtension() {


    companion object {
        fun newInstance() = LinkCardFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.link_card_fragment, container, false)
    }
}