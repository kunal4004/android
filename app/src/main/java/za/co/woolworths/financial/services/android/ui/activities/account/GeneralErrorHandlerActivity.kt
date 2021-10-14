package za.co.woolworths.financial.services.android.ui.activities.account

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.general_error_handler_activity.*
import za.co.woolworths.financial.services.android.util.Utils
import android.content.Intent

class GeneralErrorHandlerActivity : AppCompatActivity() {

    private var navigationHost: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.general_error_handler_activity)
        Utils.updateStatusBarBackground(this)
        actionBar()

        initErrorHandlerNavGraph()
    }

    private fun initErrorHandlerNavGraph() {
        val errorLayout = supportFragmentManager.findFragmentById(R.id.errorHandlerContainerFrameLayout)  as? NavHostFragment
        navigationHost = errorLayout?.navController
        val navGraph = navigationHost?.navInflater?.inflate(R.navigation.error_handler_nav_graph)
        navGraph?.startDestination = R.id.errorHandlerFragment
        navGraph?.let { navigationHost?.setGraph(it) }
    }

    private fun actionBar() {
        setSupportActionBar(tbErrorHandler)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.error_handler_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.itmIconClose -> {
                onBackPressed()
                return true
            }
        }
        return false
    }

    override fun onBackPressed() {
        val intent = Intent()
        setResult(-1, intent)
        finish()
        overridePendingTransition(0, 0)
    }
}