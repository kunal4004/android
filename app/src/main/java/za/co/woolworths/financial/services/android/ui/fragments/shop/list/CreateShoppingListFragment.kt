package za.co.woolworths.financial.services.android.ui.fragments.shop.list

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.create_new_list.*
import za.co.woolworths.financial.services.android.contracts.AsyncAPIResponse
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.CreateList
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.CreateShoppingList
import za.co.woolworths.financial.services.android.models.rest.shoppinglist.PostAddToShoppingList
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.HttpAsyncTask
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.ToastUtils
import java.util.HashMap

class CreateShoppingListFragment : ShoppingListExtensionFragment(), View.OnClickListener, ToastUtils.ToastInterface {

    private var mShoppingListGroup: HashMap<String, Boolean>? = null
    private var mAddToListRequest: MutableList<AddToListRequest>? = null
    private var isPostingShoppingItem: Boolean? = null
    private var mPostShoppingList: HttpAsyncTask<String, String, ShoppingListItemsResponse>? = null
    private var mCreateShoppingList: HttpAsyncTask<String, String, ShoppingListsResponse>? = null
    private var mShouldDisplayCreateListOnly: Boolean = false

    companion object {
        private const val SHOPPING_LIST_SELECTED_LIST_ID = "SHOPPING_LIST_SELECTED_LIST_ID"
        private const val SHOPPING_LIST_SELECTED_GROUP = "SHOPPING_LIST_SELECTED_GROUP"
        private const val DISPLAY_CREATE_LIST_ONLY = "DISPLAY_CREATE_LIST_ONLY"

        fun newInstance(listOfIds: HashMap<String, Boolean>?, selectedListGroup: String?) = CreateShoppingListFragment().apply {
            arguments = Bundle(2).apply {
                putSerializable(SHOPPING_LIST_SELECTED_LIST_ID, listOfIds)
                putString(SHOPPING_LIST_SELECTED_GROUP, selectedListGroup)
            }
        }

        fun newInstance(listOfIds: HashMap<String, Boolean>?, selectedListGroup: String?, shouldDisplayCreateList: Boolean) = CreateShoppingListFragment().apply {
            arguments = Bundle(3).apply {
                putSerializable(SHOPPING_LIST_SELECTED_LIST_ID, listOfIds)
                putString(SHOPPING_LIST_SELECTED_GROUP, selectedListGroup)
                putBoolean(DISPLAY_CREATE_LIST_ONLY, shouldDisplayCreateList)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.create_new_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getBundleArguments()
        toolbarIconVisibility()
        clickListener()
        textChangeListener()
    }

    private fun getBundleArguments() {
        mAddToListRequest = mutableListOf()
        arguments.apply {
            if (this.containsKey(SHOPPING_LIST_SELECTED_LIST_ID)) {
                mShoppingListGroup = this.getSerializable(SHOPPING_LIST_SELECTED_LIST_ID) as HashMap<String, Boolean>?
            }

            if (this.containsKey(SHOPPING_LIST_SELECTED_GROUP)) {
                mAddToListRequest = convertStringToObject(this.getString(SHOPPING_LIST_SELECTED_GROUP))
            }

            if (this.containsKey(DISPLAY_CREATE_LIST_ONLY))
                mShouldDisplayCreateListOnly = this.getBoolean(DISPLAY_CREATE_LIST_ONLY, false)

        }
    }

    private fun clickListener() {
        imBack.setOnClickListener(this)
        imCloseIcon.setOnClickListener(this)
        btnCancel.setOnClickListener(this)
    }

    private fun textChangeListener() {
        etNewList?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                // if (!mShouldDisplayCreateListOnly)
                btnCancel.text = if (etNewList.text.toString().trim { it <= ' ' }.isNotEmpty()) getString(R.string.ok) else getString(R.string.cancel)
            }

            override fun afterTextChanged(editable: Editable) {
            }
        })
    }

    override fun onResume() {
        super.onResume()
        showKeyboard(etNewList)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelRequest(mPostShoppingList)
        cancelRequest(mCreateShoppingList)
        hideKeyboard()

    }

    private fun toolbarIconVisibility() {
        val entryCount: Int? = getFragmentBackStackEntryCount()
        imBack.visibility = if (entryCount == 0) GONE else VISIBLE
        imCloseIcon.visibility = if (entryCount == 0) VISIBLE else GONE
//        imBack.visibility = if (mShouldDisplayCreateListOnly) GONE else VISIBLE
//        imCloseIcon.visibility = if (mShouldDisplayCreateListOnly) GONE else VISIBLE
        changeLayoutHeightToMatchParent()
    }

    private fun getFragmentBackStackEntryCount() = activity?.supportFragmentManager?.backStackEntryCount

    override fun onClick(view: View?) {
        activity?.apply {
            when (view?.id) {
                R.id.imBack -> {
                    hideKeyboard()
                    onBackPressed()
                }

                R.id.imCloseIcon -> {
                    onBackPressed()
                }

                R.id.btnCancel -> {
                    val listName = etNewList.text.toString()
                    if (listName.isNotEmpty()) {
                        showKeyboard(etNewList)
                        if (NetworkManager.getInstance().isConnectedToNetwork(this)) {
                            createShoppingListRequest()
                        } else {
                            ErrorHandlerView(this).showToast()
                        }
                    } else {
                        onBackPressed()
                    }
                }
            }
        }
    }

    private fun createShoppingListRequest() {
        val listName = etNewList?.text?.toString()
        val createListRequest = buildFirstRequest(listName)
        shoppingListPostProgress(true)
        mCreateShoppingList = CreateShoppingList(createListRequest, object : AsyncAPIResponse.ResponseDelegate<ShoppingListsResponse> {
            override fun onSuccess(response: ShoppingListsResponse) {
                response.apply {
                    when (httpCode) {
                        200 -> {
                            if (mShoppingListGroup?.size!! > 0) {
                                mShoppingListGroup?.forEach {
                                    val listId = it.key
                                    // Post item not sent to server only, false mean item not send yet
                                    if (!it.value) {
                                        // Update listId value from dto AddToListRequest
                                        mAddToListRequest?.forEach { item -> item.listId = listId }
                                    }
                                    postToShoppingList(mAddToListRequest, it.key)
                                }
                            } else {
                                shoppingListPostProgress(false)
                                displayToastMessage()
                            }
                        }
                        440 -> {
                            shoppingListPostProgress(false)
                        }
                        else -> {
                            shoppingListPostProgress(false)
                        }
                    }
                }
            }

            override fun onFailure(errorMessage: String) {
                shoppingListPostProgress(false)
                displayNoConnectionToast()
            }
        }).execute() as HttpAsyncTask<String, String, ShoppingListsResponse>
    }


    private fun postToShoppingList(addToListRequest: MutableList<AddToListRequest>?, listId: String?) {
        isPostingShoppingItem = true
        mPostShoppingList = PostAddToShoppingList(listId, addToListRequest, object : AsyncAPIResponse.ResponseDelegate<ShoppingListItemsResponse> {
            override fun onSuccess(response: ShoppingListItemsResponse) {
                response.apply {
                    when (httpCode) {
                        200 -> {
                            mShoppingListGroup?.apply {
                                // Will replace the value of an existing key and will create it if doesn't exist
                                listId?.let { put(it, true) }

                                //Check all values are true, implying that all request was sent
                                if (false !in values) {
                                    shoppingListPostProgress(false)
                                    displayToastMessage()
                                }
                            }
                        }
                        440 -> {
                            shoppingListPostProgress(false)

                        }
                        else -> {
                            shoppingListPostProgress(false)
                        }
                    }
                }
                isPostingShoppingItem = false
            }

            override fun onFailure(errorMessage: String) {
                shoppingListPostProgress(false)
                displayNoConnectionToast()
            }

        }).execute() as HttpAsyncTask<String, String, ShoppingListItemsResponse>
    }

    private fun displayNoConnectionToast() {
        activity?.let { ErrorHandlerView(it).showToast() }
    }

    private fun shoppingListPostProgress(state: Boolean) {
        btnCancel.isEnabled = !state
        etNewList.isEnabled = !state
        pbCreateList.visibility = if (state) VISIBLE else GONE
    }

    private fun displayToastMessage() {
        val toast = ToastUtils(this)
    }

    private fun buildFirstRequest(listName: String?): CreateList {
        return CreateList(listName, mAddToListRequest)
    }

    override fun onToastButtonClicked(state: String?) {
        Toast.makeText(activity, "TODO(not implement Toast)", Toast.LENGTH_SHORT).show()
    }

    private fun changeLayoutHeightToMatchParent() {
//        val params = clCreateNewList.layoutParams
//        params.height = if (mShouldDisplayCreateListOnly) MATCH_PARENT else resources.getDimension(R.dimen.hundred_and_fifty_dp).toInt()
//        params.width = MATCH_PARENT
//        clCreateNewList.layoutParams = params
//        clCreateNewList.requestLayout()//It is necesary to refresh the screen
    }
}