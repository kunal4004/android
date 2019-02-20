package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.list.AddToShoppingListFragment

class AddToShoppingListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_to_shopping_list_activity)
        if (savedInstanceState == null) {
            val addToListRequestBundle = intent?.getStringExtra("addToListRequest")
            addFragment(
                    fragment = AddToShoppingListFragment.newInstance(addToListRequestBundle),
                    tag = AddToShoppingListFragment::class.java.simpleName,
                    containerViewId = R.id.flShoppingListContainer
            )
        }
    }

    override fun onBackPressed() {
        val fm = fragmentManager
        if (fm.backStackEntryCount > 0) {
            fm.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}