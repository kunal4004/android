package za.co.woolworths.financial.services.android.ui.fragments.account.main.util

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewbinding.ViewBinding
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.showErrorDialog
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils

abstract class BindingBaseActivity<B : ViewBinding>(val bindingFactory: (LayoutInflater) -> B) : AppCompatActivity(),View.OnClickListener {

    protected lateinit var binding: B
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindingFactory(layoutInflater)
        setContentView(binding.root)
        Utils.updateStatusBarBackground(this, R.color.bg_e6e6e6)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_item, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_search)?.icon?.setTint(resources.getColor(R.color.white))
        return super.onPrepareOptionsMenu(menu)
    }
    fun changeMenuItemColor(menu: Menu?,tintColor:Int){
        menu?.findItem(R.id.action_search)?.icon?.setTint(tintColor)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
    protected fun setUpActionBar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
        }
        KotlinUtils.setTransparentStatusBar(this)
    }
    protected fun setClickListeners(vararg views: View){
        for (v in views){
            v.setOnClickListener(this)
        }
    }
    protected fun errorDialog(){
        runOnUiThread{
            val serverErrorResponse = ServerErrorResponse()
            serverErrorResponse.desc = getString(R.string.general_error_desc) ?: ""
            showErrorDialog(this, serverErrorResponse)
        }
    }
}