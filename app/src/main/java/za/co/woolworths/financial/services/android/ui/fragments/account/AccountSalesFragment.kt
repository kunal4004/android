package za.co.woolworths.financial.services.android.ui.fragments.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.google.gson.Gson
import com.thoughtbot.expandablerecyclerview.listeners.GroupExpandCollapseListener
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import kotlinx.android.synthetic.main.account_sales_card_benefit_layout.*
import kotlinx.android.synthetic.main.account_sales_card_collection_layout.*
import kotlinx.android.synthetic.main.account_sales_detail_fragment.*
import kotlinx.android.synthetic.main.account_sales_more_benefit_layout.*
import kotlinx.android.synthetic.main.account_sales_qualify_criteria_layout.*
import za.co.woolworths.financial.services.android.models.dto.account.*
import za.co.woolworths.financial.services.android.ui.adapters.AccountSalesCardBenefitAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class AccountSalesFragment : Fragment() {

    private var mAccountSalesCardBenefitAdapter: AccountSalesCardBenefitAdapter? = null
    private var expandedGroup: ExpandableGroup<*>? = null

    companion object {
        const val ACCOUNT_SALES_CREDIT_CARD = "ACCOUNT_SALES_CREDIT_CARD"
        fun newInstance() = AccountSalesFragment().withArgs {
        }

        fun newInstance(hashMap: AccountSales?) = AccountSalesFragment().withArgs {
            putString(ACCOUNT_SALES_CREDIT_CARD, Gson().toJson(hashMap))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.account_sales_detail_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val storeCardItem = arguments?.getString(ACCOUNT_SALES_CREDIT_CARD, "")
        val accountSales: AccountSales? = Gson().fromJson(storeCardItem, AccountSales::class.java)

        accountSales?.apply {
            displayCardBenefits(cardBenefit)
            displayMoreBenefits(getMoreBenefit(moreBenefit))
            displayQualifyCriteria(cardQualifyCriteria)
            displayCartCollection(cardCollection)
        }
    }

    private fun getMoreBenefit(moreBenefit: MutableList<MoreBenefit>?): MutableList<MoreBenefits> {
        val moreBenefits: MutableList<MoreBenefits> = mutableListOf()
        moreBenefit?.forEach { benefit ->
            moreBenefits.add(MoreBenefits(benefit.name
                    ?: "", benefit.description, benefit.drawableId))
        }
        return moreBenefits
    }

    private fun displayCardBenefits(cardBenefits: MutableList<CardBenefit>?) {
        activity?.let { activity ->
            cardBenefits?.forEach { cardBenefit ->
                val view = View.inflate(activity, R.layout.account_sales_card_benefits_item, null)
                val titleTextView: TextView? = view?.findViewById(R.id.salesBenefitTitleTextView)
                val descriptionTextView: TextView? =
                        view?.findViewById(R.id.salesBenefitDescriptionTextView)
                val salesItemImageView: ImageView? =
                        view?.findViewById(R.id.salesItemImageView)
                titleTextView?.text = cardBenefit.title
                descriptionTextView?.text = cardBenefit.description
                salesItemImageView?.setImageResource(cardBenefit.drawableId)
                cardBenefitLinearLayout?.addView(view)
            }
        }
    }

    private fun displayMoreBenefits(moreBenefits: MutableList<MoreBenefits>) {
        mAccountSalesCardBenefitAdapter = AccountSalesCardBenefitAdapter(moreBenefits)

        moreBenefitRecyclerView?.apply {
            (itemAnimator as? DefaultItemAnimator)?.supportsChangeAnimations = false
            isNestedScrollingEnabled = false
            layoutManager = activity?.let { activity -> LinearLayoutManager(activity) }
            adapter = mAccountSalesCardBenefitAdapter
        }

        mAccountSalesCardBenefitAdapter?.setOnGroupExpandCollapseListener(object : GroupExpandCollapseListener {
            override fun onGroupExpanded(group: ExpandableGroup<*>?) {
                if (expandedGroup != null && expandedGroup?.equals(group) != true && mAccountSalesCardBenefitAdapter?.isGroupExpanded(expandedGroup) == true) {
                    mAccountSalesCardBenefitAdapter?.toggleGroup(expandedGroup)
                }
                expandedGroup = group
            }

            override fun onGroupCollapsed(group: ExpandableGroup<*>?) {}
        })
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