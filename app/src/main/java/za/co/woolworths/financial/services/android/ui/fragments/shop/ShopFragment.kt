package za.co.woolworths.financial.services.android.ui.fragments.shop


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_shop.*
import kotlinx.android.synthetic.main.shop_custom_tab.view.*
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.product.ProductSearchActivity
import za.co.woolworths.financial.services.android.ui.adapters.ShopPagerAdapter
import za.co.woolworths.financial.services.android.ui.fragments.barcode.BarcodeFragment
import za.co.woolworths.financial.services.android.util.PermissionResultCallback
import za.co.woolworths.financial.services.android.util.PermissionUtils
import java.util.*


/**
 * A simple [Fragment] subclass.
 *
 */
class ShopFragment : Fragment(), PermissionResultCallback {

    private var mTabTitle: MutableList<String>? = null
    private var permissionUtils: PermissionUtils? = null
    var permissions: ArrayList<String> = arrayListOf()
    var bottomNavigationActivity: BottomNavigationActivity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shop, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionUtils = PermissionUtils(activity, this)
        permissions.add(android.Manifest.permission.CAMERA)
        bottomNavigationActivity = activity as BottomNavigationActivity
        tvSearchProduct.setOnClickListener { navigateToProductSearch() }
        imBarcodeScanner.setOnClickListener { checkCameraPermission() }
        mTabTitle = mutableListOf(resources.getString(R.string.shop_department_title_department),
                resources.getString(R.string.shop_department_title_list),
                resources.getString(R.string.shop_department_title_order))
        viewpager_main.adapter = ShopPagerAdapter(fragmentManager, mTabTitle)
        tabs_main.setupWithViewPager(viewpager_main)
        setupTabIcons(0)
    }

    private fun checkCameraPermission() {
        permissionUtils?.check_permission(permissions, "Explain here why the app needs permissions", 1)
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

    private fun navigateToProductSearch() {
        val openProductSearch = Intent(activity, ProductSearchActivity::class.java)
        startActivity(openProductSearch)
        activity.overridePendingTransition(0, 0)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            //do when hidden
            (activity as BottomNavigationActivity).fadeOutToolbar(R.color.recent_search_bg)
            (activity as BottomNavigationActivity).showBackNavigationIcon(false)
            refreshViewPagerFragment()
        }
    }

    override fun PermissionGranted(request_code: Int) {
        navigateToBarcode()
    }

    override fun PartialPermissionGranted(request_code: Int, granted_permissions: ArrayList<String>?) {
    }

    override fun PermissionDenied(request_code: Int) {
    }

    override fun NeverAskAgain(request_code: Int) {
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionUtils?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun navigateToBarcode() {
        val barcodeFragment = BarcodeFragment()
        val bundle = Bundle()
        bundle.putString("SCAN_MODE", "ONE_D_MODE")
        barcodeFragment.arguments = bundle
        bottomNavigationActivity?.hideBottomNavigationMenu()
        bottomNavigationActivity?.pushFragmentSlideUp(barcodeFragment)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            SSOActivity.SSOActivityResult.SUCCESS.rawValue() -> {
                refreshViewPagerFragment()
            }
        }
    }

    private fun refreshViewPagerFragment() {
        when (viewpager_main.currentItem) {
            1 -> {
                val myListsFragment = viewpager_main.adapter?.instantiateItem(viewpager_main, viewpager_main.currentItem) as? MyListsFragment
                myListsFragment?.authenticateUser()
            }
            2 -> {
                // TODO:: Refresh my order sign in
                //                        val myListsFragment = viewpager_main.adapter?.instantiateItem(viewpager_main, viewpager_main.currentItem) as? MyOrdersFragment
                //                        myListsFragment?.authenticateUser()
            }
        }
    }
}

private fun Any?.authenticateUser() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}
