package za.co.woolworths.financial.services.android.ui.fragments.account.apply_now

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.account_sales_card_benefit_layout.*
import kotlinx.android.synthetic.main.account_sales_card_collection_layout.*
import kotlinx.android.synthetic.main.account_sales_detail_fragment.*
import kotlinx.android.synthetic.main.account_sales_more_benefit_layout.*
import kotlinx.android.synthetic.main.account_sales_qualify_criteria_layout.*
import za.co.woolworths.financial.services.android.models.dto.account.*
import za.co.woolworths.financial.services.android.ui.activities.account.apply_now.AccountSalesPresenterImpl.Companion.ACCOUNT_SALES_CREDIT_CARD
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.adapters.MoreBenefitAdapter
import za.co.woolworths.financial.services.android.util.expand.ExpandableAdapter
import za.co.woolworths.financial.services.android.util.expand.ExpandableRecyclerAdapter

class AccountSalesFragment : Fragment() {

    private var lastExpandedPosition: Int = 0

    companion object {
        fun newInstance(hashMap: AccountSales?) = AccountSalesFragment().withArgs {
            putString(ACCOUNT_SALES_CREDIT_CARD, Gson().toJson(hashMap))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.account_sales_detail_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val storeCardItem = arguments?.getString(ACCOUNT_SALES_CREDIT_CARD, "")
        val accountSales: AccountSales? = Gson().fromJson(storeCardItem, AccountSales::class.java)

        accountSales?.apply {
            displayCardBenefits(cardBenefit)
            displayMoreBenefits(moreBenefit)
            displayQualifyCriteria(cardQualifyCriteria)
            displayCartCollection(cardCollection)
        }
        ViewCompat.setNestedScrollingEnabled(nestedScrollViewLayout, true)

    }

    private fun displayCardBenefits(cardBenefits: MutableList<CardBenefit>?) {
        activity?.let { activity ->
            cardBenefits?.forEach { cardBenefit ->
                // Personal Loan title is Benefits instead of Card Benefits in other sections
                if (cardBenefit.cardBenefitTitle?.isNotEmpty() == true) {
                    salesBenefitTitleTextView?.text = cardBenefit.cardBenefitTitle
                }
                val view = View.inflate(activity, R.layout.account_sales_card_benefits_item, null)
                val titleTextView: TextView? = view?.findViewById(R.id.salesBenefitTitleTextView)
                val descriptionTextView: TextView? =
                        view?.findViewById(R.id.salesBenefitDescriptionTextView)
                val salesItemImageView: ImageView? = view?.findViewById(R.id.salesItemImageView)
                titleTextView?.text = cardBenefit.title
                descriptionTextView?.text = cardBenefit.description
                salesItemImageView?.setImageResource(cardBenefit.drawableId)
                cardBenefitLinearLayout?.addView(view)
            }
        }
    }

    private fun displayMoreBenefits(moreBenefits: MutableList<MoreBenefit>) {
        val accountSalesMoreBenefitAdapter = MoreBenefitAdapter(moreBenefits)
        moreBenefitRecyclerView?.apply {
            isNestedScrollingEnabled = false
            clipToPadding = true
            layoutManager = activity?.let { activity -> LinearLayoutManager(activity) }
            adapter = accountSalesMoreBenefitAdapter
            accountSalesMoreBenefitAdapter.setExpandCollapseListener(object : ExpandableRecyclerAdapter.ExpandCollapseListener, ExpandableAdapter.ExpandCollapseListener {
                override fun onListItemExpanded(position: Int) {
                    if (lastExpandedPosition != -1
                            && position != lastExpandedPosition) {
                        accountSalesMoreBenefitAdapter.collapseParent(lastExpandedPosition)
                    }
                    lastExpandedPosition = position
                    val linearLayoutManager = layoutManager as? LinearLayoutManager
                    linearLayoutManager?.scrollToPositionWithOffset(position, 0)
                }

                override fun onListItemCollapsed(position: Int) {}
            })
        }
    }

    private fun displayQualifyCriteria(qualifyCriteria: MutableList<CardQualifyCriteria>) {
        activity?.let { activity ->
            qualifyCriteria.forEach { items ->
                val view = View.inflate(activity, R.layout.account_sales_bullet_item, null)
                val titleTextView: TextView? = view?.findViewById(R.id.bulletTitleTextView)
                titleTextView?.text = items.title
                qualifyCriteriaLinearLayout?.addView(view)
            }
        }
    }

    private fun displayCartCollection(cartCollection: MutableList<CardCollection>) {
        if (cartCollection.isEmpty()) {
            cardCollectionConstraintLayout?.visibility = GONE
        }
        activity?.let { activity ->
            cartCollection.forEach { items ->
                val view = View.inflate(activity, R.layout.account_sales_bullet_item, null)
                val titleTextView: TextView? = view?.findViewById(R.id.bulletTitleTextView)
                titleTextView?.text = items.title
                cardCollectionItemLinearLayout?.addView(view)
            }
        }
    }
}