package za.co.woolworths.financial.services.android.ui.fragments.wreward

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R

class VIPExclusiveFragment : Fragment() {

    companion object {
        fun newInstance() = VIPExclusiveFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.reward_vip_exclusive_fragment, container, false)
    }
}