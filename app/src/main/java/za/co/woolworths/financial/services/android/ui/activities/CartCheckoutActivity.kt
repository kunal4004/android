package za.co.woolworths.financial.services.android.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_cart_checkout.*
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CheckOutFragment.REQUEST_CHECKOUT_ON_DESTROY
import za.co.woolworths.financial.services.android.util.Utils

class CartCheckoutActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart_checkout)
        Utils.updateStatusBarBackground(this)
        val checkOutFragment = CheckOutFragment()
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
            .replace(R.id.content_frame, checkOutFragment).commit()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnClose -> {
                setResult(REQUEST_CHECKOUT_ON_DESTROY)
                finish()
            }
            else -> {
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_down_anim, R.anim.stay)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> onBackPressed()
            android.R.id.home -> onBackPressed()
        }
        return false
    }

    fun showTitleWithCrossButton(titleText: String) {
        cart_appbar?.visibility = View.VISIBLE
        cart_toolbar?.visibility = View.VISIBLE
        setSupportActionBar(cart_toolbar)
        btnClose?.visibility = View.VISIBLE
        btnClose.setOnClickListener(this)
        toolbarText.text = titleText
        supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(false)
        }
    }
}