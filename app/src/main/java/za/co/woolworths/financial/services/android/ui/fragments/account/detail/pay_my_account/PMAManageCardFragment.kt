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
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.pma_manage_card_fragment.*
import kotlinx.coroutines.GlobalScope
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.GetPaymentMethod
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity.Companion.PAYMENT_DETAIL_CARD_UPDATE_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl
import za.co.woolworths.financial.services.android.ui.adapters.PMACardsAdapter
import za.co.woolworths.financial.services.android.ui.extension.*
import za.co.woolworths.financial.services.android.ui.fragments.account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.views.card_swipe.RecyclerViewSwipeDecorator
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class PMAManageCardFragment : Fragment(), View.OnClickListener {

    private var selectedProductPosition: Int = 0
    private var accountInfo: String? = null
    private var paymentMethod: String? = null
    private var mAccountDetails: Pair<ApplyNowState, Account>? = null
    private var manageCardAdapter: PMACardsAdapter? = null
    private var mPaymentMethodList: MutableList<GetPaymentMethod>? = null
    private var navController: NavController? = null
    private var deletedPaymentMethod: GetPaymentMethod? = null
    private var root: View? = null
    private val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    val args: PMAProcessRequestFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            accountInfo = getString(PayMyAccountPresenterImpl.GET_ACCOUNT_INFO, "")
            paymentMethod = getString(PayMyAccountPresenterImpl.GET_PAYMENT_METHOD, "")
            mAccountDetails = Gson().fromJson<Pair<ApplyNowState, Account>>(accountInfo, object : TypeToken<Pair<ApplyNowState, Account>>() {}.type)
            mPaymentMethodList = Gson().fromJson<MutableList<GetPaymentMethod>>(paymentMethod, object : TypeToken<MutableList<GetPaymentMethod>>() {}.type)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (root == null)
            root = inflater.inflate(R.layout.pma_manage_card_fragment, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        configureToolbar()
        configureRecyclerview()
        useThisCardButton?.apply {
            setOnClickListener(this@PMAManageCardFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        addCardTextView?.apply {
            setOnClickListener(this@PMAManageCardFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        payMyAccountViewModel.getNavigationResult().observe(viewLifecycleOwner) { result ->
            when (result) {
                PayMyAccountViewModel.OnBackNavigation.REMOVE -> {
                    // deleteRow(position)
                    GlobalScope.doAfterDelay(300) {
                        removeCardProduct(selectedProductPosition)
                    }
                }
                PayMyAccountViewModel.OnBackNavigation.ADD -> {
                    GlobalScope.doAfterDelay(300) {
                        val accounts = mAccountDetails?.second
                        val addCard = PMAManageCardFragmentDirections.actionManageCardFragmentToAddNewPayUCardFragment(accounts)
                        navController?.navigate(addCard)
                    }
                }

                PayMyAccountViewModel.OnBackNavigation.MAX_CARD_LIMIT -> {
                    GlobalScope.doAfterDelay(350) {
                        navController?.navigate(R.id.action_manageCardFragment_to_PMATenCardLimitDialogFragment)
                    }
                }

                else -> return@observe
            }
        }
    }

    private fun configureRecyclerview() {
        // ensure first item is checked
        val isPaymentChecked: List<GetPaymentMethod>? = mPaymentMethodList?.filter { s -> s.isCardChecked }
        if (isPaymentChecked?.isEmpty() == true) {
            if (mPaymentMethodList?.size ?: 0 > 0)
                mPaymentMethodList?.get(0)?.isCardChecked = true
        } else {
            mPaymentMethodList = payMyAccountViewModel.getPaymentMethodList()
        }

        pmaManageCardRecyclerView?.apply {
            layoutManager = activity?.let { LinearLayoutManager(it, LinearLayoutManager.VERTICAL, false) }

            manageCardAdapter = PMACardsAdapter(mPaymentMethodList) { paymentMethod, position ->
                selectedProductPosition = position

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

            val itemTouchHelper = ItemTouchHelper(paymentMethodItemSwipeLeft)
            itemTouchHelper.attachToRecyclerView(this)

        }
    }


    private fun configureToolbar() {
        (activity as? PayMyAccountActivity)?.apply {
            configureToolbar(bindString(R.string.credit_debit_cards_label))
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            displayToolbarDivider(true)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.useThisCardButton -> {
                val cardDetail = payMyAccountViewModel.getCardDetail()
                cardDetail?.selectedCardPosition = selectedProductPosition

                cardDetail?.paymentMethodList = manageCardAdapter?.getList()
                payMyAccountViewModel.setPMACardInfo(cardDetail)
                activity?.apply {
                    setResult(RESULT_OK, Intent().putExtra(PAYMENT_DETAIL_CARD_UPDATE_RESULT_CODE, Gson().toJson(cardDetail)))
                    onBackPressed()
                }
            }

            R.id.addCardTextView -> {
                if (payMyAccountViewModel.isPaymentMethodListSizeLimitedToTenItem()) {
                    navController?.navigate(R.id.action_manageCardFragment_to_PMATenCardLimitDialogFragment)
                    return
                }
                val accounts = mAccountDetails?.second
                val manageCard = PMAManageCardFragmentDirections.actionManageCardFragmentToAddNewPayUCardFragment(accounts)
                navController?.navigate(manageCard)
            }
        }
    }

    private val paymentMethodItemSwipeLeft: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            //Disable swipe if card is expired
            val position = viewHolder.adapterPosition
            val swipedProduct = mPaymentMethodList?.get(position)
            if (swipedProduct?.cardExpired == true) return 0
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

    private fun removeCardProduct(position: Int) {
        deletedPaymentMethod = mPaymentMethodList?.get(position)
        mPaymentMethodList?.removeAt(position)
        manageCardAdapter?.notifyItemRemoved(position)
        manageCardAdapter?.notifyItemRangeChanged(position, manageCardAdapter?.itemCount ?: 0)

        val cardPosition = payMyAccountViewModel.getCardDetail()?.selectedCardPosition
        if (cardPosition != null && cardPosition >= 1)
            payMyAccountViewModel.getCardDetail()?.selectedCardPosition = if (deletedPaymentMethod?.isCardChecked == true) 0 else cardPosition.minus(1)

        if (NetworkManager.getInstance().isConnectedToNetwork(context)) {
            request(deletedPaymentMethod?.token?.let { OneAppService.queryServicePayURemovePaymentMethod(it) }, object : IGenericAPILoaderView<Any> {
                override fun onSuccess(response: Any?) {}
            })
            // Disable use this card button when no item is selected
            useThisCardButton?.isEnabled = !payMyAccountViewModel.isPaymentMethodListChecked()

            // set and display add new card as start destination in graph
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}