package za.co.woolworths.financial.services.android.ui.fragments.faq

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.View
import android.webkit.URLUtil
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FaqDetailBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.fragments.faq.web.WebFragment
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

@Suppress("DEPRECATION")
class FAQDetailFragment : BaseFragmentBinding<FaqDetailBinding>(FaqDetailBinding::inflate) {

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        binding.populateTextView()
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


    private fun FaqDetailBinding.populateTextView() {
        title.text = mQuestion
        description.text = Html.fromHtml(mAnswer)
        description.setOnClickListener {
            val spans = description.urls
            if (spans.isNotEmpty()) {
                var spanUrl = spans[0].url
                if (!URLUtil.isValidUrl(spanUrl)) {
                    spanUrl = "$woolworthsDomainUrl$spanUrl"
                }
                if (URLUtil.isValidUrl(spanUrl)) {
                    val bundle = Bundle()
                    bundle.putString("web_url", spanUrl)
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

    companion object {
        const val woolworthsDomainUrl = "https://www.woolworths.co.za"
    }
}