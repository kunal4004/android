package za.co.woolworths.financial.services.android.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.appcompat.app.AppCompatActivity
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.add_to_shopping_list_activity.*
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow.ANIM_DOWN_DURATION
import za.co.woolworths.financial.services.android.ui.activities.OrderDetailsActivity.Companion.ORDER_ID
import za.co.woolworths.financial.services.android.contracts.IDialogListener
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.list.AddToShoppingListFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.list.CreateShoppingListFragment
import za.co.woolworths.financial.services.android.util.Utils

class AddToShoppingListActivity : AppCompatActivity(), IDialogListener {

    private var mPopEnterAnimation: Animation? = null
    private var exitAnimationHasStarted: Boolean = false
    private var mShoppingList: MutableList<ShoppingList>? = null

    companion object {
        const val ADD_TO_SHOPPING_LIST_REQUEST_CODE = 1209
        const val ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE = 1210
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_to_shopping_list_activity)
        if (savedInstanceState == null) {
            Utils.updateStatusBarBackground(this)
            val addToListRequestBundle: String? = intent?.getStringExtra("addToListRequest")
            val shouldDisplayCreateList: Boolean? = intent?.getBooleanExtra("shouldDisplayCreateList", false)
            val orderId = intent?.getStringExtra(ORDER_ID)

            if (shouldDisplayCreateList!!) {
                addFragment(
                        fragment = CreateShoppingListFragment.newInstance(HashMap(), addToListRequestBundle, shouldDisplayCreateList, orderId),
                        tag = AddToShoppingListFragment::class.java.simpleName,
                        allowStateLoss = false,
                        containerViewId = R.id.flShoppingListContainer)

            } else {
                addFragment(
                        fragment = AddToShoppingListFragment.newInstance(addToListRequestBundle, orderId),
                        tag = AddToShoppingListFragment::class.java.simpleName,
                        allowStateLoss = false,
                        containerViewId = R.id.flShoppingListContainer
                )
            }
        }

        setAnimation()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fm = supportFragmentManager?.findFragmentById(R.id.flShoppingListContainer)
        fm?.onActivityResult(requestCode, resultCode, data)

    }

    override fun onDialogDismissed() {
        val fm = supportFragmentManager?.findFragmentById(R.id.flShoppingListContainer)
        when (fm) {
            is AddToShoppingListFragment -> (fm as? AddToShoppingListFragment)?.closeFragment()
        }
    }

    override fun onBackPressed() {
        val fm = supportFragmentManager
        if (fm.backStackEntryCount > 0) {
            fm.popBackStack(null, POP_BACK_STACK_INCLUSIVE)
        } else {
            exitActivityAnimation()
        }
    }

    private fun setAnimation() {
        mPopEnterAnimation = AnimationUtils.loadAnimation(this, R.anim.popup_enter)
        flShoppingListContainer.startAnimation(mPopEnterAnimation)
    }

    fun exitActivityAnimation() {
        if (!exitAnimationHasStarted) {
            exitAnimationHasStarted = true
            val animation = TranslateAnimation(0f, 0f, 0f, flShoppingListContainer.height.toFloat())
            animation.fillAfter = true
            animation.duration = ANIM_DOWN_DURATION.toLong()
            animation.setAnimationListener(object : Animation.AnimationListener {

                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationRepeat(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {

                    finish()
                    overridePendingTransition(0, 0)
                }
            })
            flShoppingListContainer.startAnimation(animation)
        }
    }

    fun setLatestShoppingList(shoppingList: MutableList<ShoppingList>) {
        mShoppingList = shoppingList
    }

    fun getLatestShoppingList() = mShoppingList

}