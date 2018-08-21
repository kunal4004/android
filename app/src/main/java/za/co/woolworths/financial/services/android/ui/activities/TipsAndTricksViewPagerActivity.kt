package za.co.woolworths.financial.services.android.ui.activities

import android.content.res.TypedArray
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.ImageView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.adapters.TipsAndTricksViewPagerAdapter
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.util.Utils
import kotlin.properties.Delegates

class TipsAndTricksViewPagerActivity : AppCompatActivity(), View.OnClickListener, ViewPager.OnPageChangeListener {

    lateinit var viewPager: ViewPager
    var tricksViewPagerAdapter: TipsAndTricksViewPagerAdapter? = null
    lateinit var tvNext: WTextView
    lateinit var tvPrevious: WTextView
    lateinit var tvCounter: WTextView
    lateinit var tvTitle: WTextView
    lateinit var tvDescription: WTextView
    lateinit var btnAction: WTextView
    lateinit var imgIcon: ImageView
    lateinit var imgCloseIcon: ImageView
    var titles: Array<String>? = null
    var descriptions: Array<String>? = null
    var actionButtonTexts: Array<String>? = null
    var icons: TypedArray by Delegates.notNull()
    var mCurrentItem: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tips_and_trics_view_pager)
        Utils.updateStatusBarBackground(this);
        initViews()
    }

    fun initViews() {
        viewPager = findViewById(R.id.tipsAndTricks)
        tvNext = findViewById(R.id.next)
        tvPrevious = findViewById(R.id.previous)
        tvCounter = findViewById(R.id.counter)
        tvTitle = findViewById(R.id.title)
        tvDescription = findViewById(R.id.description)
        imgIcon = findViewById(R.id.icon)
        imgCloseIcon = findViewById(R.id.close)
        btnAction = findViewById(R.id.actionButton)
        tvNext.setOnClickListener(this)
        tvPrevious.setOnClickListener(this)
        viewPager.addOnPageChangeListener(this)
        imgCloseIcon.setOnClickListener(this)
        titles = resources.getStringArray(R.array.tips_tricks_titles)
        descriptions = resources.getStringArray(R.array.tips_tricks_descriptions)
        icons = resources.obtainTypedArray(R.array.tips_tricks_icons)
        actionButtonTexts = resources.getStringArray(R.array.tips_tricks_buttons)
        bindDataToViews()
    }

    fun bindDataToViews() {
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
            R.id.close -> {
                onBackPressed()
            }
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        tvPrevious.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
        tvNext.setText(if ((position + 1) == titles?.size) resources.getString(R.string.done) else resources.getString(R.string.next))
        tvTitle.setText(titles?.get(position))
        tvDescription.setText(descriptions?.get(position))
        btnAction.setText(actionButtonTexts?.get(position))
        tvCounter.setText((position + 1).toString() + " OF " + titles?.size.toString())
        imgIcon.setBackgroundResource(icons.getResourceId(position, -1))
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
    }
}
