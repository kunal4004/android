package za.co.woolworths.financial.services.android.ui.activities

import android.content.res.TypedArray
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_tips_and_trics_view_pager.*
import za.co.woolworths.financial.services.android.ui.adapters.TipsAndTricksViewPagerAdapter
import za.co.woolworths.financial.services.android.util.Utils
import kotlin.properties.Delegates

class TipsAndTricksViewPagerActivity : AppCompatActivity(), View.OnClickListener, ViewPager.OnPageChangeListener {

    private var tricksViewPagerAdapter: TipsAndTricksViewPagerAdapter? = null
    private var titles: Array<String>? = null
    private var descriptions: Array<String>? = null
    private var actionButtonTexts: Array<String>? = null
    private var icons: TypedArray by Delegates.notNull()
    private var mCurrentItem: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tips_and_trics_view_pager)
        Utils.updateStatusBarBackground(this);
        initViews()
    }

    private fun initViews() {
        next.setOnClickListener(this)
        previous.setOnClickListener(this)
        viewPager.addOnPageChangeListener(this)
        closePage.setOnClickListener(this)
        titles = resources.getStringArray(R.array.tips_tricks_titles)
        descriptions = resources.getStringArray(R.array.tips_tricks_descriptions)
        icons = resources.obtainTypedArray(R.array.tips_tricks_icons)
        actionButtonTexts = resources.getStringArray(R.array.tips_tricks_buttons)
        bindDataToViews()
    }

    private fun bindDataToViews() {
        mCurrentItem = intent.getIntExtra("position", 0)
        tricksViewPagerAdapter = TipsAndTricksViewPagerAdapter(this)
        viewPager.adapter = tricksViewPagerAdapter
        viewPager.currentItem = mCurrentItem
        onPageSelected(mCurrentItem)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.next -> {
                var current: Int = viewPager.currentItem + 1
                if (current < titles!!.size) viewPager.setCurrentItem(current) else onBackPressed()
            }
            R.id.previous -> {
                var current: Int = viewPager.currentItem
                viewPager.setCurrentItem(current - 1)
            }
            R.id.closePage -> {
                onBackPressed()
            }
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        previous.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
        next.setText(if ((position + 1) == titles?.size) resources.getString(R.string.done) else resources.getString(R.string.next))
        featureTitle.text = titles?.get(position)
        featureDescription.text = descriptions?.get(position)
        featureActionButton.text = actionButtonTexts?.get(position)
        counter.text = (position + 1).toString() + " OF " + titles?.size.toString()
        featureIcon.setBackgroundResource(icons.getResourceId(position, -1))
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
    }
}
