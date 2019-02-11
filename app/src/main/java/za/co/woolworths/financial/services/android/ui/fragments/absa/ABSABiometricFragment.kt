package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R

class ABSABiometricFragment : Fragment() {

    companion object {
        fun newInstance() = ABSABiometricFragment()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.absa_biometric_fragment, container, false)
    }
}