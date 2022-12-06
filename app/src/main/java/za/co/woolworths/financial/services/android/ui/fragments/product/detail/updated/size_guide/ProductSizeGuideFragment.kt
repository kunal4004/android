package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.size_guide

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentProdcutSizeGuideBinding
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ErrorDialogFragment
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class ProductSizeGuideFragment : BaseFragmentBinding<FragmentProdcutSizeGuideBinding>(FragmentProdcutSizeGuideBinding::inflate), ProductSizeGuideContract.ProductSizeGuideView, ErrorDialogFragment.IOnErrorDialogDismiss {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getSizeGuideContent()
    }

    override fun getSizeGuideContent() {
        sizeGuideId?.let { presenter?.loadSizeGuideContent(it) }
    }

    override fun showProgressBar() {
        binding.progressGetContent?.visibility = View.VISIBLE
    }

    override fun hideProgressBar() {
        binding.progressGetContent?.visibility = View.GONE
    }

    override fun onSizeGuideContentSuccess(sizeGuideHtmlContent: String?) {
        loadSizeGuideView(sizeGuideHtmlContent)
    }

    override fun onSizeGuideContentFailed(errorMessage: String?) {
        hideProgressBar()
        activity?.apply {
            this@ProductSizeGuideFragment.childFragmentManager?.let { fragmentManager -> Utils.showGeneralErrorDialog(fragmentManager, errorMessage) }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun loadSizeGuideView(sizeGuideHtmlContent: String?) {
        binding.sizeGuideContentWebView?.apply {
            settings?.javaScriptEnabled = true
            loadDataWithBaseURL(null, sizeGuideHtmlContent!!, "text/html", "UTF-8", null)
            visibility = View.VISIBLE
        }
        hideProgressBar()
    }

    override fun onErrorDialogDismiss() {
        super.onErrorDialogDismiss()
        activity?.finish()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.close_menu_item, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.closeIcon -> {
                activity?.finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}