package za.co.woolworths.financial.services.android.ui.fragments.product.sub_category

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.BR
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ExpandableSubCategoryFragmentBinding
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.models.dto.RootCategory
import za.co.woolworths.financial.services.android.models.dto.SubCategory
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.base.BaseFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment.Companion.newInstance
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.ImageManager.Companion.setPictureCenterInside
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.expand.*
import kotlin.collections.ArrayList

class SubCategoryFragment :
    BaseFragment<ExpandableSubCategoryFragmentBinding?, SubCategoryViewModel?>(),
    SubCategoryNavigator, View.OnClickListener {

    companion object {
        const val ERROR_DIALOG_REQUEST = 1456
        const val KEY_ARGS_ROOT_CATEGORY = "rootCategory"
        const val KEY_ARGS_VERSION = "version"
        const val KEY_ARGS_IS_LOCATION_ENABLED = "isLocationEnabled"
        const val KEY_ARGS_LOCATION = "location"
    }

    private var mSubCategories: ArrayList<SubCategory>? = null
    private var rvCategoryDrill: RecyclerView? = null
    private var mAdapter: SubCategoryAdapter? = null
    private var lastExpandedPosition = -1
    private var mRootCategory: RootCategory? = null
    private var mErrorHandlerView: ErrorHandlerView? = null
    private var mSelectedHeaderPosition = 0
    private var mParentViewHolder: ParentSubCategoryViewHolder? = null
    private var mSubCategoryListModel: MutableList<SubCategoryModel>? = null
    private var version: String? = null
    private var isLocationEnabled: Boolean = false
    private var location: Location? = null

    override fun getLayoutId(): Int {
        return R.layout.expandable_sub_category_fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel?.navigator = this
        val bundle = this.arguments
        mSubCategories = ArrayList()
        if (bundle != null) {
            val rootCategory = bundle.getString(KEY_ARGS_ROOT_CATEGORY)
            version = bundle.getString(KEY_ARGS_VERSION, "")
            isLocationEnabled = bundle.getBoolean(KEY_ARGS_IS_LOCATION_ENABLED, false)
            if (bundle.containsKey(KEY_ARGS_LOCATION)) {
                location = bundle.getParcelable(KEY_ARGS_LOCATION)
            }
            if (rootCategory != null) mRootCategory =
                Gson().fromJson(rootCategory, RootCategory::class.java)
            mRootCategory = Gson().fromJson(rootCategory, RootCategory::class.java)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvCategoryDrill = viewDataBinding?.rcvDrillCategory
        mErrorHandlerView = ErrorHandlerView(activity, viewDataBinding?.rlNoConnection)

        // RecyclerView has some built in animations to it, using the DefaultItemAnimator.
        // Specifically when you call notifyItemChanged() it does a fade animation for the changing
        // of the data in the ViewHolder. If you would like to disable this you can use the following:
        val animator = rvCategoryDrill?.itemAnimator
        if (animator is DefaultItemAnimator) {
            animator.supportsChangeAnimations = true
        }
        setHeader(mRootCategory)
        rvCategoryDrill?.layoutManager = LinearLayoutManager(activity)
        fetchSubCategories(mRootCategory?.categoryId, false, version)
        viewDataBinding?.btnRetry?.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(
            activity,
            FirebaseManagerAnalyticsProperties.ScreenNames.SHOP_SUB_CATEGORIES
        )
    }

    override fun getViewModel(): SubCategoryViewModel {
        return ViewModelProviders.of(this).get(
            SubCategoryViewModel::class.java
        )
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun bindSubCategoryResult(subCategoryList: List<SubCategory>, latestVersionParam: String) {
        version = latestVersionParam
        if (viewModel.childItem()) { // child item
            val subCategoryChildList: MutableList<SubCategoryChild> = ArrayList()
            for (subCat in subCategoryList) {
                val subCategoryChild = SubCategoryChild()
                subCategoryChild.subCategory = subCat
                subCategoryChildList.add(subCategoryChild)
            }
            val subCategoryModel = mSubCategoryListModel?.get(mSelectedHeaderPosition)
            subCategoryModel?.setSubCategoryChildList(subCategoryChildList)
            if (mAdapter != null) {
                mAdapter?.updateList(
                    mSubCategoryListModel,
                    mParentViewHolder,
                    mSelectedHeaderPosition
                )
            }
            return
        }
        setCategoryAdapter(subCategoryList) // header item
    }

    override fun unhandledResponseHandler(response: Response) {
        if (viewModel.childItem()) {
            mAdapter?.hideChildItemProgressBar()
            subcategoryOtherHttpResponse(response)
        } else {
            val activity = activity ?: return
            activity.runOnUiThread {
                hideView(viewDataBinding?.pbSubCategory)
                hideView(viewDataBinding?.rcvDrillCategory)
                showView(viewDataBinding?.rootDrillDownCategory)
                subcategoryOtherHttpResponse(response)
            }
        }
    }

    private fun subcategoryOtherHttpResponse(response: Response) {
        if (!TextUtils.isEmpty(response.desc)) {
            Utils.displayValidationMessageForResult(
                activity,
                CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc, ERROR_DIALOG_REQUEST
            )
        }
    }

    override fun onFailureResponse(e: String) {
        connectionFailureUI(e)
        viewDataBinding?.pbSubCategory?.visibility = View.GONE
        viewDataBinding?.rcvDrillCategory?.visibility = View.GONE
    }

    private fun connectionFailureUI(e: String) {
        viewDataBinding?.rootDrillDownCategory?.visibility = View.VISIBLE
        viewDataBinding?.rcvDrillCategory?.visibility = View.GONE
        mErrorHandlerView?.networkFailureHandler(e)
    }

    override fun onLoad() {
        if (!viewModel.childItem()) {
            showProgressBar(true)
        }
    }

    override fun onLoadComplete() {
        showProgressBar(false)
    }

    override fun onChildItemClicked(subCategory: SubCategory) {
        //Navigate to product grid
        pushFragment(
            newInstance(
                ProductsRequestParams.SearchType.NAVIGATE,
                subCategory.categoryName,
                subCategory.dimValId
            )
        )
    }

    override fun noConnectionDetected() {
        if (mErrorHandlerView != null) {
            mErrorHandlerView?.showToast()
            Utils.toggleStatusBarColor(activity, R.color.red)
        }
    }

    override fun retrieveChildItem(
        holder: ParentSubCategoryViewHolder,
        subCategory: SubCategory,
        selectedHeaderPosition: Int
    ) {
        mSelectedHeaderPosition = selectedHeaderPosition
        mParentViewHolder = holder
        fetchSubCategories(subCategory.categoryId, true, version)
    }

    override fun onCloseIconPressed() {
        popFragmentSlideDown()
    }

    private fun setHeader(mRootCategory: RootCategory?) {
        if (mRootCategory != null) {
            setPictureCenterInside(
                viewDataBinding?.imProductCategory, mRootCategory.imgUrl
            )
            viewDataBinding?.tvCategoryName?.setText(mRootCategory.categoryName)
            viewDataBinding?.imClose?.setOnClickListener(this)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.imClose -> popFragmentSlideDown()
            R.id.btnRetry -> fetchSubCategories(mRootCategory?.categoryId, false, version)
            else -> {
            }
        }
    }

    private fun fetchSubCategories(categoryId: String?, childItem: Boolean, version: String?) {
        if (categoryId == null || version == null) return
        if (isNetworkConnected) {
            mErrorHandlerView?.hideErrorHandler()
            //ChildItem params determine whether to perform header or child operation
            viewModel.setChildItem(childItem)
            viewModel.fetchSubCategory(categoryId, version, isLocationEnabled, location)
        } else {
            if (!viewModel.childItem()) {
                connectionFailureUI("e")
            }
        }
    }

    private fun setCategoryAdapter(subCategories: List<SubCategory>) {
        /***
         * Check for NullPointerException on Activity to prevent
         * Attempt to invoke virtual method 'java.lang.Object android.content.Context.getSystemService
         */
        /***
         * Check for NullPointerException on Activity to prevent
         * Attempt to invoke virtual method 'java.lang.Object android.content.Context.getSystemService
         */
        val activity = activity ?: return
        mSubCategories = ArrayList(subCategories)
        val subHeaderCategory = SubCategory()
        subHeaderCategory.setCategoryId(mRootCategory?.categoryId)
        subHeaderCategory.setCategoryName(mRootCategory?.categoryName)
        subHeaderCategory.setHasChildren(mRootCategory?.hasChildren)
        subHeaderCategory.setImgUrl(mRootCategory?.imgUrl)
        subHeaderCategory.setHasChildren(false)
        mSubCategories?.add(0, subHeaderCategory)
        val expandableSubCategory = viewDataBinding?.rcvDrillCategory
        mSubCategoryListModel = ArrayList()
        mSubCategories?.let {
            for (subCategory in it) {
                mSubCategoryListModel?.add(SubCategoryModel(subCategory, null))
            }
        }
        mAdapter = SubCategoryAdapter(activity, this, mSubCategoryListModel)
        mAdapter?.setExpandCollapseListener(object :
            ExpandableRecyclerAdapter.ExpandCollapseListener {
            override fun onListItemExpanded(position: Int) {
                if (lastExpandedPosition != -1
                    && position != lastExpandedPosition
                ) {
                    mAdapter?.collapseParent(lastExpandedPosition)
                }
                lastExpandedPosition = position
                val llm = rvCategoryDrill?.layoutManager as LinearLayoutManager?
                llm?.scrollToPositionWithOffset(position, 0)
            }

            override fun onListItemCollapsed(position: Int) {}
        })
        rvCategoryDrill?.adapter = mAdapter
    }

    private fun showProgressBar(visible: Boolean) {
        viewDataBinding?.pbSubCategory?.visibility =
            if (visible) View.VISIBLE else View.GONE
        viewDataBinding?.rootDrillDownCategory?.visibility =
            if (visible) View.VISIBLE else View.GONE
        viewDataBinding?.rcvDrillCategory?.visibility =
            if (visible) View.GONE else View.VISIBLE
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            hideToolbar()
            (activity as? BottomNavigationActivity)?.showBottomNavigationMenu()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //TODO: Comment what's actually happening here.
        if (requestCode == ERROR_DIALOG_REQUEST && resultCode == Activity.RESULT_CANCELED) {
            val activity: Activity? = activity
            if (activity is BottomNavigationActivity) {
                activity.onBackPressed()
                activity.reloadDepartmentFragment()
            }
        }
    }
}