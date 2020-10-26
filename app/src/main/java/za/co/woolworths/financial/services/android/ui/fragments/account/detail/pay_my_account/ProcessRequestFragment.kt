package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R

class ProcessRequestFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
           return inflater.inflate(R.layout.processing_request_fragment, container, false)
    }

}