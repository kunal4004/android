package za.co.woolworths.financial.services.android.util.expand

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.databinding.OrderAgainCategoryViewBinding
import com.awfs.coordination.databinding.SubCategoryChildViewBinding
import com.awfs.coordination.databinding.SubCategoryHeaderViewBinding
import com.awfs.coordination.databinding.SubCategoryParentViewBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.fragments.product.sub_category.SubCategoryNavigator
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.Utils

class SubCategoryAdapter(
    context: Context,
    subCategoryNavigator: SubCategoryNavigator,
    parentItemList: List<ParentListItem?>?
) : ExpandableRecyclerAdapter<HeaderViewHolder, OrderAgainViewHolder, ParentSubCategoryViewHolder,
        SubCategoryViewHolder>(
    parentItemList
) {
    private val mContext: Context
    private val mInflator: LayoutInflater
    private val mSubCategoryNavigator: SubCategoryNavigator
    private val ROW_NOTIFY_ITEM_CHANGED_DELAY = 50

    init {
        mInflator = LayoutInflater.from(context)
        mSubCategoryNavigator = subCategoryNavigator
        mContext = context
    }

    override fun onCreateParentViewHolder(
        parentViewGroup: ViewGroup,
        viewType: Int
    ): ParentSubCategoryViewHolder {
        return ParentSubCategoryViewHolder(
            SubCategoryParentViewBinding.inflate(
                LayoutInflater.from(parentViewGroup.context),
                parentViewGroup,
                false
            )
        )
    }

    override fun onCreateChildViewHolder(
        childViewGroup: ViewGroup,
        viewType: Int
    ): SubCategoryViewHolder {
        return SubCategoryViewHolder(
            SubCategoryChildViewBinding.inflate(LayoutInflater.from(childViewGroup.context), childViewGroup, false)
        )
    }

    override fun onCreateHeaderViewHolder(headerViewGroup: ViewGroup): HeaderViewHolder {
        return HeaderViewHolder(
            SubCategoryHeaderViewBinding.inflate(LayoutInflater.from(headerViewGroup.context), headerViewGroup, false)
        )
    }

    override fun onCreateOrderAgainViewHolder(parentViewGroup: ViewGroup): OrderAgainViewHolder {
        return OrderAgainViewHolder(
            OrderAgainCategoryViewBinding.inflate(LayoutInflater.from(parentViewGroup.context), parentViewGroup, false)
        )
    }

    override fun onBindOrderAgainViewHolder(
        orderAgainViewHolder: OrderAgainViewHolder,
        position: Int,
        item: ParentListItem?
    ) {
        with(orderAgainViewHolder) {
            val subCategoryModel = item as SubCategoryModel
            bind(subCategoryModel)
            itemView.setOnClickListener {
                mSubCategoryNavigator.onOrderAgainClicked()
            }
        }
    }

    override fun onBindParentViewHolder(
        parentSubCategoryViewHolder: ParentSubCategoryViewHolder,
        parentPosition: Int,
        parentListItem: ParentListItem
    ) {
        val subCategoryModel = parentListItem as SubCategoryModel
        parentSubCategoryViewHolder.bind(subCategoryModel)
        parentSubCategoryViewHolder.itemView.setOnClickListener(View.OnClickListener {
            val subCategory = subCategoryModel.subCategory
            if (parentSubCategoryViewHolder.isRowExpanded) {
                parentSubCategoryViewHolder.retrieveChildVisibility(false)
                parentSubCategoryViewHolder.collapseView()
                return@OnClickListener
            }
            val arguments: MutableMap<String, String> = HashMap()
            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.PROMOTION_NAME] =
                subCategoryModel.name
            if (subCategoryModel.name == "Promotions") Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.VIEW_PROMOTION,
                arguments,
                mContext as Activity
            )
            if (subCategoryModel.childItemList != null) {
                parentSubCategoryViewHolder.retrieveChildVisibility(false)
                parentSubCategoryViewHolder.expandView()
                return@OnClickListener
            }

//				int sizeOfList = getParentItemList().size();
//				// retrieve one api call at a time
//				for (int index = 0; index < sizeOfList; index++) {
//					if (((SubCategoryModel) getParentItemList().get(index)).getSubCategory().singleProductItemIsLoading) {
//						return;
//					}
//				}
            if (mSubCategoryNavigator == null) return@OnClickListener
            if (!NetworkManager.getInstance().isConnectedToNetwork(mContext)) {
                mSubCategoryNavigator.noConnectionDetected()
                return@OnClickListener
            }
            if (subCategory.hasChildren) {
                subCategory.singleProductItemIsLoading = true
                var headerIndexLoop = 0
                var headerPosition = 0
                for (pl in parentItemList) {
                    if ((parentItemList[headerIndexLoop] as SubCategoryModel).subCategory.categoryId
                            .equals(subCategory.categoryId, ignoreCase = true)
                    ) {
                        headerPosition = headerIndexLoop
                    }
                    headerIndexLoop++
                }
                mSubCategoryNavigator.retrieveChildItem(
                    parentSubCategoryViewHolder,
                    subCategoryModel.subCategory,
                    headerPosition
                )
                notifyItemChanged(headerPosition)
            } else {
                //Open ProductListingFragment when hasChildren = false;
                mSubCategoryNavigator.onChildItemClicked(subCategory)
            }
        })
    }

    private fun toggleExpandCollapseView(parentSubCategoryViewHolder: ParentSubCategoryViewHolder) {
        if (parentSubCategoryViewHolder.isRowExpanded) {
            parentSubCategoryViewHolder.collapseView()
        } else {
            parentSubCategoryViewHolder.expandView()
        }
    }

    override fun onBindChildViewHolder(
        subCategoryViewHolder: SubCategoryViewHolder,
        position: Int,
        childListItem: Any
    ) {
        val subCategoryChild = childListItem as SubCategoryChild
        subCategoryViewHolder.bind(mSubCategoryNavigator, subCategoryChild)
    }

    override fun onBindHeaderViewHolder(
        headerViewHolder: HeaderViewHolder,
        position: Int,
        headerListItem: ParentListItem
    ) {
        headerViewHolder.bind(headerListItem as SubCategoryModel)
        headerViewHolder.itemBinding.imClose.setOnClickListener { mSubCategoryNavigator!!.onCloseIconPressed() }
    }

    fun updateList(
        mSubCategoryListModel: List<SubCategoryModel>,
        mParentViewHolder: ParentSubCategoryViewHolder,
        selectedPosition: Int
    ) {
        /**
         * The handler prevent notifyItemChanged and notifyDataInserted from expanded method
         * to send update at the same time.
         */
        val runnable = Runnable { toggleExpandCollapseView(mParentViewHolder) }
        val handler = Handler()
        handler.postDelayed(runnable, ROW_NOTIFY_ITEM_CHANGED_DELAY.toLong())
        mSubCategoryListModel[selectedPosition].subCategory.setSingleProductItemIsLoading(false)
        parentItemList = mSubCategoryListModel
        notifyItemChanged(selectedPosition)
    }

    /***
     * @Method hideChildItemProgressBar hide the progressbar and display arrow indicator on error response
     */
    fun hideChildItemProgressBar() {
        val sizeOfList = parentItemList.size
        for (index in 0 until sizeOfList) {
            (parentItemList[index] as SubCategoryModel).subCategory.singleProductItemIsLoading =
                false
        }
        notifyDataSetChanged()
    }
}