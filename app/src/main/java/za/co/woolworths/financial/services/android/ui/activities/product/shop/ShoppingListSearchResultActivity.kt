package za.co.woolworths.financial.services.android.ui.activities.product.shop

import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.shopping_list_detail_activity.*
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.search.SearchResultFragment
import za.co.woolworths.financial.services.android.util.Utils

class ShoppingListSearchResultActivity : AppCompatActivity() {
    private var searchTerm = ""
    private var listID = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.updateStatusBarBackground(this)
        setContentView(R.layout.shopping_list_detail_activity)
        intent?.extras?.apply {
            searchTerm = getString("searchTerm", "")
            listID = getString("listID", "")
        }
        retrieveBundleArgument()
        setUpToolbar(searchTerm)
        initFragment()
    }

    private fun retrieveBundleArgument() {
        btnBack?.setOnClickListener { onBackPressed() }
    }

    private fun setUpToolbar(listName: String) {
        shoppingListTitleTextView?.text = listName
        mToolbar?.let { toolbar -> setSupportActionBar(toolbar) }
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    private fun initFragment() {
        val searchResultFragment = SearchResultFragment()
        val bundle = Bundle()
        bundle.putString("searchTerm", searchTerm)
        bundle.putString("listID", listID)
        searchResultFragment.arguments = bundle
        supportFragmentManager
                .beginTransaction()
                .add(R.id.flShoppingListDetailFragment, searchResultFragment,
                        SearchResultFragment::class.java.simpleName)
                .disallowAddToBackStack()
                .commitAllowingStateLoss()
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BottomNavigationActivity.PDP_REQUEST_CODE && resultCode == AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE) {
            setResult(AddToShoppingListActivity.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE, data)
            finish()
            overridePendingTransition(0, 0)
            return
        }
        val shoppingListDetailFragment = supportFragmentManager.findFragmentById(R.id.flShoppingListDetailFragment)
        shoppingListDetailFragment?.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val SHOPPING_LIST_SEARCH_RESULT_REQUEST_CODE = 2012
    }
}