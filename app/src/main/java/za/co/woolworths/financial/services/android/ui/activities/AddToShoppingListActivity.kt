package za.co.woolworths.financial.services.android.ui.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.list.AddToShoppingListFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.list.CreateShoppingListFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.SingleButtonDialogFragment
import za.co.woolworths.financial.services.android.util.Utils

class AddToShoppingListActivity : AppCompatActivity(), SingleButtonDialogFragment.DialogListener {

    companion object {
        const val ADD_TO_SHOPPING_LIST_REQUEST_CODE = 1209
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_to_shopping_list_activity)
        if (savedInstanceState == null) {
            Utils.updateStatusBarBackground(this)
            val addToListRequestBundle: String? = intent?.getStringExtra("addToListRequest")
            val shouldDisplayCreateList: Boolean? = intent?.getBooleanExtra("shouldDisplayCreateList", false)

            if (shouldDisplayCreateList!!) {
                addFragment(
                        fragment = CreateShoppingListFragment.newInstance(HashMap(), addToListRequestBundle, shouldDisplayCreateList),
                        tag = AddToShoppingListFragment::class.java.simpleName,
                        containerViewId = R.id.flShoppingListContainer)
            } else {
                addFragment(
                        fragment = AddToShoppingListFragment.newInstance(addToListRequestBundle),
                        tag = AddToShoppingListFragment::class.java.simpleName,
                        containerViewId = R.id.flShoppingListContainer
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var fm = supportFragmentManager?.findFragmentById(R.id.flShoppingListContainer)
        fm?.onActivityResult(requestCode, resultCode, data)

    }

    override fun onDismissListener() {
        var fm = supportFragmentManager?.findFragmentById(R.id.flShoppingListContainer)
        when (fm) {
            is AddToShoppingListFragment -> (fm as? AddToShoppingListFragment)?.closeFragment()

            is CreateShoppingListFragment -> {

            }
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