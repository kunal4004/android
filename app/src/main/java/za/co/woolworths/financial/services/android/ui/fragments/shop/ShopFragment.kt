package za.co.woolworths.financial.services.android.ui.fragments.shop


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_shop.*
import za.co.woolworths.financial.services.android.ui.adapters.ShopPagerAdapter


/**
 * A simple [Fragment] subclass.
 *
 */
class ShopFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shop, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewpager_main.adapter = ShopPagerAdapter(fragmentManager)
        tabs_main.setupWithViewPager(viewpager_main)
    }


}
