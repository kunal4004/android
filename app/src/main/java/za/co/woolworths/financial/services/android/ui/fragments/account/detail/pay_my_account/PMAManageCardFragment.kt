package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.pma_manage_card_fragment.*
import kotlinx.coroutines.GlobalScope
import za.co.woolworths.financial.services.android.models.dto.GetPaymentMethod
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity.Companion.PAYMENT_DETAIL_CARD_UPDATE
import za.co.woolworths.financial.services.android.ui.adapters.PMACardsAdapter
import za.co.woolworths.financial.services.android.ui.extension.*
import za.co.woolworths.financial.services.android.ui.views.card_swipe.RecyclerViewSwipeDecorator
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.wenum.LifecycleType

class PMAManageCardFragment : PMAFragment(), View.OnClickListener {

    private var mDeletedPaymentMethodPosition: Int = 0
    private var temporarySelectedPosition: Int = 0
    private var manageCardAdapter: PMACardsAdapter? = null
    private var mPaymentMethodList: MutableList<GetPaymentMethod>? = null
    private var navController: NavController? = null
    private var deletedPaymentMethod: GetPaymentMethod? = null
    private var root: View? = null
    private val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()
    private var mLifecycleType: LifecycleType = LifecycleType.INIT

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (root == null)
            root = inflater.inflate(R.layout.pma_manage_card_fragment, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        configureToolbar(true, R.string.credit_debit_cards_label)
        useThisCardButton?.apply {
            setOnClickListener(this@PMAManageCardFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }
        swipeToRefreshList()
        addCardTextView?.apply {
            setOnClickListener(this@PMAManageCardFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        payMyAccountViewModel.getNavigationResult().observe(viewLifecycleOwner) { result ->
            when (result) {
                PayMyAccountViewModel.OnBackNavigation.REMOVE -> {
                    // deleteRow(position)
                    GlobalScope.doAfterDelay(AppConstant.DELAY_300_MS) {
                        removeCardProduct(temporarySelectedPosition)
                    }
                }
                PayMyAccountViewModel.OnBackNavigation.ADD -> {
                    GlobalScope.doAfterDelay(AppConstant.DELAY_300_MS) {
                        val accounts = payMyAccountViewModel.getAccount()
                        val addCard = PMAManageCardFragmentDirections.actionManageCardFragmentToAddNewPayUCardFragment(accounts)
                        navController?.navigate(addCard)
                    }
                }

                PayMyAccountViewModel.OnBackNavigation.MAX_CARD_LIMIT -> {
                    GlobalScope.doAfterDelay(AppConstant.DELAY_350_MS) {
                        navController?.navigate(R.id.action_manageCardFragment_to_PMATenCardLimitDialogFragment)
                    }
                }

                else -> return@observe
            }
        }
    }

    private fun swipeToRefreshList() {
        swipeToRefreshList?.setOnRefreshListener {
            queryGetPaymentMethod(mLifecycleType)
        }
    }

    private fun configureRecyclerview() {
        // ensure first item is checked

        mPaymentMethodList = payMyAccountViewModel.getPaymentMethodList()

        pmaManageCardRecyclerView?.apply {
            layoutManager = activity?.let { LinearLayoutManager(it, LinearLayoutManager.VERTICAL, false) }

            manageCardAdapter = PMACardsAdapter(mPaymentMethodList) { paymentMethod, position ->
                temporarySelectedPosition = position

                val isCardSelected = manageCardAdapter?.getList()?.any { it.isCardChecked }

                useThisCardButton?.isEnabled = isCardSelected ?: false

                when (paymentMethod.cardExpired) {
                    true -> {
                        val cardExpiredFragmentDirections = PMAManageCardFragmentDirections.actionManageCardFragmentToPMACardExpiredFragment(paymentMethod)
                        navController?.navigate(cardExpiredFragmentDirections)
                    }
                }
            }

            adapter = manageCardAdapter
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.useThisCardButton -> {
                val cardDetail = payMyAccountViewModel.getCardDetail()
                cardDetail?.selectedCardPosition = temporarySelectedPosition

                cardDetail?.paymentMethodList = manageCardAdapter?.getList()
                payMyAccountViewModel.setPMACardInfo(cardDetail)
                activity?.apply {
                    setResult(RESULT_OK, Intent().putExtra(PAYMENT_DETAIL_CARD_UPDATE, Gson().toJson(cardDetail)))
                    onBackPressed()
                }
            }

            R.id.addCardTextView -> {
                if (payMyAccountViewModel.isPaymentMethodListSizeLimitedToTenItem()) {
                    navController?.navigate(R.id.action_manageCardFragment_to_PMATenCardLimitDialogFragment)
                    return
                }
                val accounts = payMyAccountViewModel.getAccount()
                val manageCard = PMAManageCardFragmentDirections.actionManageCardFragmentToAddNewPayUCardFragment(accounts)
                navController?.navigate(manageCard)
            }
        }
    }

    private fun removeCardProduct(position: Int) {
        deletedPaymentMethod = mPaymentMethodList?.get(position)
        mDeletedPaymentMethodPosition = position
        mPaymentMethodList?.removeAt(position)
        manageCardAdapter?.notifyItemRemoved(position)

        val cardPosition = payMyAccountViewModel.getCardDetail()?.selectedCardPosition
        if (cardPosition != null && cardPosition >= 1)
            payMyAccountViewModel.getCardDetail()?.selectedCardPosition = if (deletedPaymentMethod?.isCardChecked == true) 0 else cardPosition.minus(1)

        if (NetworkManager.getInstance().isConnectedToNetwork(context)) {
            deleteProgressVisibility(true)
            payMyAccountViewModel.queryServiceDeletePaymentMethod(deletedPaymentMethod, position, {
                deleteProgressVisibility(false)
            }, {
                // Add deleted card to list
                deleteProgressVisibility(false)
                deletedPaymentMethod?.let { manageCardAdapter?.notifyInsert(it, position) }
                mPaymentMethodList = manageCardAdapter?.getList()

                // Display delete card popup
                // To prevent IllegalArgumentException cannot be found from the current destination Destination
                if (navController?.currentDestination?.id == R.id.manageCardFragment)
                    navController?.navigate(R.id.action_manageCardFragment_to_PMADeleteCardAPIErrorFragment)
            })

            // Disable use this card button when no item is selected
            useThisCardButton?.isEnabled = !payMyAccountViewModel.isPaymentMethodListChecked()

            // Set and display add new card as start destination in graph
            if (mPaymentMethodList?.isEmpty() == true) {

                val card = payMyAccountViewModel.getCardDetail()
                card?.payuMethodType = PayMyAccountViewModel.PAYUMethodType.CREATE_USER
                card?.paymentMethodList = mutableListOf()
                val graph = navController?.graph
                graph?.startDestination = R.id.addNewPayUCardFragment
                graph?.let { navController?.setGraph(it) }
            }
        } else {
            ErrorHandlerView(context).showToast()
        }
    }

    private fun deleteProgressVisibility(isVisible: Boolean) {
        GlobalScope.doAfterDelay(100) {
            deleteProgressBar?.visibility = if (isVisible) VISIBLE else GONE
            useThisCardButton?.isEnabled = !isVisible
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        queryGetPaymentMethod(mLifecycleType)
    }

    private fun queryGetPaymentMethod(type: LifecycleType) {
        onGetPaymentMethodProgress(type, true)
        payMyAccountViewModel.queryServicePayUPaymentMethod({ paymentMethodsList ->
            if (!isAdded) return@queryServicePayUPaymentMethod
            when (mLifecycleType) {
                LifecycleType.INIT -> configureRecyclerview()
                LifecycleType.RESUME -> {
                    manageCardAdapter?.notifyUpdate(paymentMethodsList, temporarySelectedPosition)
                    mPaymentMethodList = manageCardAdapter?.getList()
                }
            }

            mPaymentMethodList = manageCardAdapter?.getList()

            val itemTouchHelper = ItemTouchHelper(paymentMethodItemSwipeLeft)
            itemTouchHelper.attachToRecyclerView(pmaManageCardRecyclerView)

            onGetPaymentMethodProgress(type, false)

            mLifecycleType = LifecycleType.RESUME
        }, {
            onGetPaymentMethodProgress(type, false)
        }, {
            onGetPaymentMethodProgress(type, false)
        }, {
            onGetPaymentMethodProgress(type, false)
        })
    }

    private fun onGetPaymentMethodProgress(type: LifecycleType, isRefreshing: Boolean) {
        if (!isAdded) return
        when (type) {
            LifecycleType.INIT -> {
                paymentMethodsListProgressBar?.visibility = if (isRefreshing) VISIBLE else GONE
                pmaManageCardRecyclerView?.visibility = if (isRefreshing) GONE else VISIBLE
            }
            LifecycleType.RESUME -> {
                swipeToRefreshList?.isRefreshing = isRefreshing
            }
        }
        useThisCardButton?.isEnabled = !isRefreshing
    }

    private val paymentMethodItemSwipeLeft: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            //Disable swipe if card is expired or deleteCardIsRunning
            val position = viewHolder.adapterPosition
            val swipedProduct = mPaymentMethodList?.get(position)
            if (swipedProduct?.cardExpired == true || !payMyAccountViewModel.isDeleteCardListEmpty()) return 0
            return super.getSwipeDirs(recyclerView, viewHolder)
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition

            when (direction) {
                ItemTouchHelper.LEFT -> {
                    removeCardProduct(position)
                }
            }
        }

        override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.7f

        override fun onChildDraw(canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

            RecyclerViewSwipeDecorator.Builder(canvas, pmaManageCardRecyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                .addSwipeLeftLabel(bindString(R.string.delete))
                .setSwipeLeftLabelTextSize(TypedValue.COMPLEX_UNIT_SP, 12.0f)
                .addBackgroundColor(bindColor(R.color.delete_red_bg))
                .setSwipeLeftLabelTypeface(getMyriadProSemiBoldFont() ?: Typeface.DEFAULT)
                .setSwipeLeftTextColor(Color.WHITE)
                .create()
                .decorate()

            super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }
}