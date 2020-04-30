package za.co.woolworths.financial.services.android.ui.fragments.faq

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.faq_detail.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.fragments.faq.web.WebFragment
import za.co.woolworths.financial.services.android.util.Utils

@Suppress("DEPRECATION")
class FAQDetailFragment : Fragment() {

    private var mQuestion: String? = null
    private var mAnswer: String? = null
    private var mBottomNavigator: BottomNavigator? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationActivity)
            mBottomNavigator = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mQuestion = getString("question", "")
            mAnswer = getString("answer", "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.faq_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        populateTextView()
    }

    private fun setupToolbar() {
        (activity as? BottomNavigationActivity)?.apply {
            setToolbarBackgroundDrawable(R.drawable.appbar_background)
            showBackNavigationIcon(true)
            setTitle(getString(R.string.drawer_faq))
            showToolbar()
        }
        (activity as? MyAccountActivity)?.setToolbarTitle(activity?.resources?.getString(R.string.drawer_faq))
    }

    override fun onResume() {
        super.onResume()
        activity?.let { Utils.setScreenName(it, FirebaseManagerAnalyticsProperties.ScreenNames.FAQ_DETAIL) }
    }


    private fun populateTextView() {
        title?.setText(mQuestion)
        description?.text = Html.fromHtml(mAnswer)
        description?.setOnClickListener {
            val spans = description.urls
            if (spans.isNotEmpty()) {
                val url = spans[0].url
                if (URLUtil.isValidUrl(url)) {
                    val bundle = Bundle()
                    bundle.putString("web_url", url)
                    val webFragment = WebFragment()
                    webFragment.arguments = bundle
                    mBottomNavigator?.pushFragment(webFragment)
                    (activity as? MyAccountActivity)?.replaceFragment(webFragment)
                }
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            setupToolbar()
        }
    }
}