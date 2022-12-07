package za.co.woolworths.financial.services.android.ui.fragments.account.apply_now

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountSalesDetailFragmentBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dto.account.*
import za.co.woolworths.financial.services.android.ui.activities.account.apply_now.AccountSalesActivity
import za.co.woolworths.financial.services.android.ui.activities.account.apply_now.AccountSalesPresenterImpl.Companion.ACCOUNT_SALES_CREDIT_CARD
import za.co.woolworths.financial.services.android.ui.adapters.MoreBenefitAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs


class AccountSalesFragment : Fragment(R.layout.account_sales_detail_fragment) {

    companion object {
        fun newInstance(hashMap: AccountSales?) = AccountSalesFragment().withArgs {
            putString(ACCOUNT_SALES_CREDIT_CARD, Gson().toJson(hashMap))
        }
    }

    private lateinit var binding: AccountSalesDetailFragmentBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = AccountSalesDetailFragmentBinding.bind(view)

        val storeCardItem = arguments?.getString(ACCOUNT_SALES_CREDIT_CARD, "")
        val accountSales: AccountSales? = Gson().fromJson(storeCardItem, AccountSales::class.java)

        var isCreditCardProduct: Boolean? = null

        (activity as? AccountSalesActivity)?.apply {
            // setNestedScrollingEnabled will unlock the ability of NestedScrollView to scroll inside ViewPager2
            isCreditCardProduct = mAccountSalesModelImpl?.isCreditCardProduct()

            binding.applyNowCardDetailScrollView?.apply {
                setScrollingEnabled(
                        when (mBottomSheetBehaviorState) {
                            BottomSheetBehavior.STATE_COLLAPSED -> {
                                smoothScrollToTop()
                                true
                            }
                            else -> false
                        })

                ViewCompat.setNestedScrollingEnabled(this, isCreditCardProduct == true)
            }
        }

        accountSales?.apply {
            displayCardBenefits(cardBenefit)
            displayMoreBenefits(moreBenefit)
            displayQualifyCriteria(cardQualifyCriteria)
            displayCardCollection(cardCollection, isCreditCardProduct)
        }
    }

    private fun displayCardBenefits(cardBenefits: MutableList<CardBenefit>?) {
        activity?.let { activity ->
            cardBenefits?.forEach { cardBenefit ->
                // Personal Loan title is Benefits instead of Card Benefits in other sections
                if (cardBenefit.cardBenefitTitle?.isNotEmpty() == true) {
                    binding.includeAccountSalesCardBenefit.salesBenefitTitleTextView?.text = cardBenefit.cardBenefitTitle
                }
                val view = View.inflate(activity, R.layout.account_sales_card_benefits_item, null)
                val titleTextView: TextView? = view?.findViewById(R.id.salesBenefitTitleTextView)
                val descriptionTextView: TextView? =
                        view?.findViewById(R.id.salesBenefitDescriptionTextView)
                val salesItemImageView: ImageView? = view?.findViewById(R.id.salesItemImageView)
                titleTextView?.text = cardBenefit.title
                descriptionTextView?.text = cardBenefit.description
                salesItemImageView?.setImageResource(cardBenefit.drawableId)
                binding.includeAccountSalesCardBenefit.cardBenefitLinearLayout?.addView(view)
            }
        }
    }

    private fun displayMoreBenefits(moreBenefits: MutableList<MoreBenefit>) {
        val accountSalesMoreBenefitAdapter = MoreBenefitAdapter(moreBenefits)
        binding.includeAccountSalesMoreBenefit.moreBenefitRecyclerView?.apply {
            isNestedScrollingEnabled = false
            clipToPadding = true
            layoutManager = activity?.let { activity -> LinearLayoutManager(activity) }
            adapter = accountSalesMoreBenefitAdapter
        }
    }

    private fun displayQualifyCriteria(qualifyCriteria: MutableList<CardQualifyCriteria>) {
        activity?.let { activity ->
            qualifyCriteria.forEach { items ->
                val view = View.inflate(activity, R.layout.account_sales_bullet_item, null)
                val titleTextView: TextView? = view?.findViewById(R.id.bulletTitleTextView)
                titleTextView?.text = items.title
                binding.includeAccountSalesQualifyCriteria.qualifyCriteriaLinearLayout?.addView(view)
            }
        }
    }

    private fun displayCardCollection(cardCollection: MutableList<CardCollection>, isCreditCardProduct: Boolean?) {
        if (cardCollection.isEmpty()) {
            binding.cardCollectionConstraintLayout?.root?.visibility = GONE
            binding.includeAccountSalesQualifyCriteria.qualifyCriteriaSpaceView?.setBackgroundColor(Color.WHITE)
            return
        }
        activity?.let { activity ->
            val cardCollectionDescriptionTextView: TextView? = view?.findViewById(R.id.cardCollectionDescriptionTextView)

            if (isCreditCardProduct == true){ //there's a bug here. isCreditCardProduct is true but this is actually not a Credit Card
                cardCollectionDescriptionTextView?.text = getString(R.string.card_collection_desc,"Store Card")
            } else{
                cardCollectionDescriptionTextView?.text = getString(R.string.card_collection_desc,"Credit Card")
            }

            cardCollection.forEach { items ->
                val view = View.inflate(activity, R.layout.account_sales_bullet_item, null)
                val titleTextView: TextView? = view?.findViewById(R.id.bulletTitleTextView)
                titleTextView?.text = items.title
                binding.cardCollectionConstraintLayout.cardCollectionItemLinearLayout?.addView(view)
            }
        }
    }

    fun smoothScrollToTop() {
        binding.applyNowCardDetailScrollView?.smoothScrollTo(0, 0)
    }

    fun setScrollingEnabled(isEnabled: Boolean) {
        binding.applyNowCardDetailScrollView?.setScrollingEnabled(isEnabled)
    }
}