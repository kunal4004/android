package za.co.woolworths.financial.services.android.ui.fragments.voc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_my_preferences.*
import kotlinx.android.synthetic.main.fragment_survey_voc.*
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.models.dto.linkdevice.UserDevice
import za.co.woolworths.financial.services.android.models.dto.linkdevice.ViewAllLinkedDeviceResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.MyPreferencesInterface
import za.co.woolworths.financial.services.android.ui.activities.voc.VoiceOfCustomerInterface
import za.co.woolworths.financial.services.android.ui.adapters.SurveyQuestionAdapter
import za.co.woolworths.financial.services.android.ui.adapters.ViewAllLinkedDevicesAdapter
import za.co.woolworths.financial.services.android.ui.fragments.account.MyAccountsFragment.RESULT_CODE_DEVICE_LINKED
import za.co.woolworths.financial.services.android.ui.fragments.mypreferences.ViewAllLinkedDevicesFragment
import za.co.woolworths.financial.services.android.ui.fragments.mypreferences.ViewAllLinkedDevicesFragment.Companion.DEVICE_LIST
import za.co.woolworths.financial.services.android.util.AuthenticateUtils
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.presentEditDeliveryLocationActivity
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.setDeliveryAddressView
import za.co.woolworths.financial.services.android.util.Utils


class SurveyVocFragment : Fragment() {

    companion object {
        const val QUESTION_LIST = "questionList"
    }

    private var surveyQuestionAdapter: SurveyQuestionAdapter? = null
    private var questionList: ArrayList<Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getSerializable(QUESTION_LIST)?.let { list ->
            if (list is ArrayList<*> && list?.get(0) is Int) { // TODO: instance type check to be updated
                questionList = list as ArrayList<Int>
            }
        }

        // TODO: remove dummy code
        questionList = ArrayList()
        questionList?.add(1)
        questionList?.add(2)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_survey_voc, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        if (surveyQuestionAdapter == null) {
            initRecyclerView()
        }
    }

    private fun setupToolbar() {
        activity?.apply {
            when (this) {
                is VoiceOfCustomerInterface -> {
                    context?.let {
                        // TODO: remove and replace with Not Now button to the right
                        setToolbarTitle("Survey")
                        setToolbarTitleGravity(Gravity.CENTER_HORIZONTAL)
                    }
                }
            }
        }
    }

    private fun initRecyclerView() {
        if (questionList.isNullOrEmpty()) return
        context?.let {
            rvSurveyQuestions.layoutManager = LinearLayoutManager(it, RecyclerView.VERTICAL, false)
            surveyQuestionAdapter = SurveyQuestionAdapter(it, questionList!!)
        }
        rvSurveyQuestions.adapter = surveyQuestionAdapter
    }
}