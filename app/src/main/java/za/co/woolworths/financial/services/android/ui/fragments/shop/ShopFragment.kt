package za.co.woolworths.financial.services.android.ui.fragments.shop


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_shop.*
import kotlinx.android.synthetic.main.shop_custom_tab.view.*
import za.co.woolworths.financial.services.android.ui.adapters.ShopPagerAdapter


/**
 * A simple [Fragment] subclass.
 *
 */
class ShopFragment : Fragment() {

    private var mTabTitle: MutableList<String>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shop, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mTabTitle = mutableListOf(resources.getString(R.string.shop_department_title_department),
                resources.getString(R.string.shop_department_title_list),
                resources.getString(R.string.shop_department_title_order))
        viewpager_main.adapter = ShopPagerAdapter(fragmentManager, mTabTitle)
        tabs_main.setupWithViewPager(viewpager_main)
        setupTabIcons(0)
    }

    private fun setupTabIcons(selectedTab: Int) {
        for (i in mTabTitle?.indices!!) {
            tabs_main.getTabAt(i)!!.customView = prepareTabView(i, mTabTitle)
        }
        tabs_main.getTabAt(selectedTab)!!.customView!!.isSelected = true
    }

    private fun prepareTabView(pos: Int, tabTitle: MutableList<String>?): View {
        val view = activity.layoutInflater.inflate(R.layout.shop_custom_tab, null)
        view.tvTitle.text = tabTitle!![pos]
        return view
    }


}
