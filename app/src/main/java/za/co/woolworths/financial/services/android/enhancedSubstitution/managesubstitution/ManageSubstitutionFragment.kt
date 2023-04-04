package za.co.woolworths.financial.services.android.enhancedSubstitution.managesubstitution

import android.graphics.Typeface
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.view.View.OnClickListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ManageSubstitutionDetailsLayoutBinding
import za.co.woolworths.financial.services.android.enhancedSubstitution.ProductSubstitutionListListener
import za.co.woolworths.financial.services.android.enhancedSubstitution.adapter.ManageProductSubstitutionAdapter
import za.co.woolworths.financial.services.android.enhancedSubstitution.adapter.SubstitutionRecylerViewItem
import za.co.woolworths.financial.services.android.enhancedSubstitution.model.AddSubstitutionRequest
import za.co.woolworths.financial.services.android.enhancedSubstitution.repository.ProductSubstitutionRepository
import za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel.ProductSubstitutionViewModelFactory
import za.co.woolworths.financial.services.android.enhancedSubstitution.apihelper.SubstitutionApiHelper
import za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel.ProductSubstitutionViewModel
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class ManageSubstitutionFragment : BaseFragmentBinding<ManageSubstitutionDetailsLayoutBinding>(
    ManageSubstitutionDetailsLayoutBinding::inflate
), OnClickListener, ProductSubstitutionListListener {

    private var manageProductSubstitutionAdapter: ManageProductSubstitutionAdapter? = null
    private var selectionChoice = ""
    private lateinit var productSubstitutionViewModel: ProductSubstitutionViewModel
    private var commarceItemId = ""


    companion object {
        private val SELECTION_CHOICE = "SELECTION_CHOICE"
        val COMMARCE_ITEM_ID = "COMMARCE_ITEM_ID"

        fun newInstance(
            substitutionSelectionChoice: String?,
                commarceItemId: String?,
        ) = ManageSubstitutionFragment().withArgs {
            putString(SELECTION_CHOICE, substitutionSelectionChoice)
            putString(COMMARCE_ITEM_ID, commarceItemId)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.apply {
            selectionChoice = getString(SELECTION_CHOICE, "")
         commarceItemId = getString(COMMARCE_ITEM_ID,  "")
        }
        binding.btnConfirm?.setOnClickListener(this)
        binding.dontWantText?.setOnClickListener(this)
        binding.imgBack?.setOnClickListener(this)
        setUpViewModel()

        manageProductSubstitutionAdapter = ManageProductSubstitutionAdapter(
            getHeaderForSubstituteList(), getSubstituteProductList(), this
        )

        binding.recyclerView?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = manageProductSubstitutionAdapter
        }
    }

    private fun getHeaderForSubstituteList(): SubstitutionRecylerViewItem.SubstitutionOptionHeader {
        return SubstitutionRecylerViewItem.SubstitutionOptionHeader(
            searchHint = resources.getString(R.string.search_alternative_product)
        )
    }

    private fun setUpViewModel() {
        productSubstitutionViewModel = ViewModelProvider(
                this,
                ProductSubstitutionViewModelFactory(ProductSubstitutionRepository(SubstitutionApiHelper()))
        ).get(ProductSubstitutionViewModel::class.java)
    }


    private fun getSubstituteProductList(): MutableList<SubstitutionRecylerViewItem.SubstitutionProducts> {
        var list = mutableListOf<SubstitutionRecylerViewItem.SubstitutionProducts>()
        /*prepare list from kibo api and set to recyler view */
        list.add(SubstitutionRecylerViewItem.SubstitutionProducts(
                1,"Banana","","you have 5","R21"))
        list.add(SubstitutionRecylerViewItem.SubstitutionProducts(
                1,"Banana","","you have 5","R21"))
        list.add(SubstitutionRecylerViewItem.SubstitutionProducts(
                1,"Banana","","you have 5","R21"))
        list.add(SubstitutionRecylerViewItem.SubstitutionProducts(
                1,"Banana","","you have 5","R21"))
        list.add(SubstitutionRecylerViewItem.SubstitutionProducts(
                1,"Banana","","you have 5","R21"))
        list.add(SubstitutionRecylerViewItem.SubstitutionProducts(
                1,"Banana","","you have 5","R21"))
        return list
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnConfirm -> confirmSubstitutionProduct()
            R.id.dontWantText -> confirmDontWantSubstitutionForProduct()
            R.id.imgBack -> (activity as? BottomNavigationActivity)?.popFragment()
        }
    }

    private fun confirmSubstitutionProduct() {
        callAddSubstitutionApi()
    }

    private fun callAddSubstitutionApi() {
        if (commarceItemId?.isEmpty() == true) {
            /*navigate to pdp with selected product  object and call add to cart api in order to add substitute there*/
        } else {
            /*add subsitute api here since we have commarceId because product is already added in cart */
        }

    }

    private fun confirmDontWantSubstitutionForProduct() {
        (activity as? BottomNavigationActivity)?.popFragment()
    }

    /*
     * To show Error message once the API is fail.
     */
    private fun showSubstitutionErrorMessage() {
        binding.errorMessageLayout.visibility = View.VISIBLE
        binding.errorMessage.makeLinks(
            Pair(getString(R.string.tap_to_retry), OnClickListener {
                // ToDo make retry call for Kibo API.
            })
        )
    }

    /*
     *  This function is to create a underline and to create a particular link text in a text view.
     */
    private fun TextView.makeLinks(vararg links: Pair<String, OnClickListener>) {
        val spannableString = SpannableString(this.text)
        var startIndexOfLink = -1
        for (link in links) {
            val clickableSpan = object : ClickableSpan() {
                override fun updateDrawState(textPaint: TextPaint) {
                    textPaint.color = ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                    textPaint.isUnderlineText = true
                    textPaint.isFakeBoldText = true
                    textPaint.typeface = Typeface.DEFAULT_BOLD
                }

                override fun onClick(view: View) {
                    Selection.setSelection((view as TextView).text as Spannable, 0)
                    view.invalidate()
                    link.second.onClick(view)
                }
            }
            startIndexOfLink = this.text.toString().indexOf(link.first, startIndexOfLink + 1)
            spannableString.setSpan(
                clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        this.movementMethod =
            LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not be clickable.
        this.setText(spannableString, TextView.BufferType.SPANNABLE)
    }

    override fun openSubstitutionSearchScreen() {
        (activity as? BottomNavigationActivity)?.pushFragmentSlideUp(
            SearchSubstitutionFragment.newInstance(commarceItemId)
        )
    }

    override fun clickOnLetMyShooperChooseOption() {
        binding.btnConfirm?.background =
            resources.getDrawable(R.drawable.black_color_drawable, null)
    }

    override fun clickOnMySubstitutioneOption() {
        binding.btnConfirm?.isEnabled = false
        binding.btnConfirm?.background =
            resources.getDrawable(R.drawable.grey_bg_drawable, null)
    }

    override fun clickOnSubstituteProduct() {
        binding.btnConfirm.isEnabled = true
        binding.btnConfirm.background = resources.getDrawable(R.drawable.black_color_drawable, null)
    }
}
