package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.list.AddToListFragment

class AddToShoppingListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_to_shopping_list_activity)

        if (savedInstanceState == null) {
            val addToListRequestBundle = intent?.getStringExtra("addToListRequest")
            addFragment(
                    fragment = AddToListFragment.newInstance(addToListRequestBundle),
                    tag = AddToListFragment::class.java.simpleName,
                    containerViewId = R.id.flShoppingListContainer
            )
        }
    }
}