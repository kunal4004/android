package za.co.woolworths.financial.services.android.ui.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_dash_details.*
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils


class DashDetailsActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var dashAdapter: DashDetailsAdapter
    private var wooliesAppLink: String? = null

    companion object {
        const val WOOLIES_APP_PACKAGE_NAME = "com.awfs.coordination"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_details)
        Utils.updateStatusBarBackground(this, R.color.bg_e6e6e6)

        intent.getBundleExtra("bundle")?.apply {
            wooliesAppLink = getString(AppConstant.KEY_DASH_WOOLIES_DOWNLOAD_LINK)
        }

        actionBar()
        setupRecyclerView()
        dash_details_download_woolies_app_btn.setOnClickListener(this)
    }

    private fun setupRecyclerView() {
        recycler_view_dash_details.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        dashAdapter = DashDetailsAdapter(this)
        recycler_view_dash_details.adapter = dashAdapter
        recycler_view_dash_details.isNestedScrollingEnabled = false
    }

    private fun actionBar() {
        setSupportActionBar(toolbarDash)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_item, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    override fun onClick(p0: View?) {
        try {
            var intent: Intent? = this.packageManager.getLaunchIntentForPackage(WOOLIES_APP_PACKAGE_NAME)
            if (intent == null) {
                intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(wooliesAppLink)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this.startActivity(intent)

        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$WOOLIES_APP_PACKAGE_NAME")))
        } catch (e: PackageManager.NameNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$WOOLIES_APP_PACKAGE_NAME")))
        }
    }
}