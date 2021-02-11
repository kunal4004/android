package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.size_guide

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_prodcut_size_guide.*
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class ProductSizeGuideFragment : Fragment(), ProductSizeGuideContract.ProductSizeGuideView {
    private var sizeGuideId: String? = null
    var presenter: ProductSizeGuideContract.ProductSizeGuidePresenter? = null

    companion object {
        fun newInstance(allergens: String?) = ProductSizeGuideFragment().withArgs {
            putString("SIZE_GUIDE_ID", allergens)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.apply {
            sizeGuideId = getString("SIZE_GUIDE_ID", null)
        }
        presenter = ProductSizeGuidePresenterImpl(this, ProductSizeGuideInteractorImpl())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_prodcut_size_guide, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getSizeGuideContent()
    }

    override fun getSizeGuideContent() {
        sizeGuideId?.let { presenter?.loadSizeGuideContent(it) }
    }

    override fun showProgressBar() {
        progressGetContent?.visibility = View.VISIBLE
    }

    override fun hideProgressBar() {
        progressGetContent?.visibility = View.GONE
    }

    override fun onSizeGuideContentSuccess(sizeGuideHtmlContent: String?) {
        loadSizeGuideView(sizeGuideHtmlContent)
    }

    override fun onSizeGuideContentFailed() {

    }

    override fun loadSizeGuideView(sizeGuideHtmlContent: String?) {
        sizeGuideContentWebView?.apply {
            settings?.javaScriptEnabled = true
            loadDataWithBaseURL(null, sizeGuideHtmlContent, "text/html", "UTF-8", null)
            visibility = View.VISIBLE
        }
        hideProgressBar()
    }
}