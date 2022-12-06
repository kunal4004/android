package za.co.woolworths.financial.services.android.ui.fragments.account.petinsurance

import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.awfs.coordination.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.woolworths.financial.services.android.models.AppConfigSingleton.accountOptions
import za.co.woolworths.financial.services.android.models.dto.account.*
import za.co.woolworths.financial.services.android.models.network.OneAppService.getAppGUIDResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService.getFeatureEnablementResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService.getPetInsuranceResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.UpdateMyAccount
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.Constants.PET
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.Constants.PET_INSURANCE
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.isPetInsuranceEnabled
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.petInsuranceRedirect
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.showPetInsurancePendingDialog
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

class PetInsuranceHandler constructor(
    private var activity: FragmentActivity?,
    private var mUpdateMyAccount: UpdateMyAccount,
    private var ivPetInsuranceProgress: ImageView,
    private var tvPetInsuranceApply: TextView,
    private var tvPetInsuranceCovered: TextView,
    private var tvPetInsuranceHelped: TextView,
    private var applyPetInsuranceCardView: RelativeLayout
) {
    var insuranceProducts: InsuranceProducts? = null

    fun featureEnablementRequest() {
        if (SessionUtilities.getInstance().isUserAuthenticated && isPetInsuranceEnabled()) {
            getFeatureEnablementResponse().enqueue(object : Callback<FeatureEnablementModel?> {
                override fun onResponse(
                    call: Call<FeatureEnablementModel?>,
                    response: Response<FeatureEnablementModel?>
                ) {
                    if (activity != null) {
                        val featureEnablementModel = response.body()
                        featureEnablementModel?.featureEnabled?.let {
                            for (feature in it) {
                                when(feature.featureName){
                                    PET_INSURANCE->{
                                        if (feature.enabled) {
                                            petInsuranceRequest()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<FeatureEnablementModel?>, t: Throwable) {
                    Utils.hideView(applyPetInsuranceCardView)
                    petInsuranceCheck()
                }
            })
        }
    }

    fun petInsuranceRequest() {
        petInsuranceShowLoading(true)
        getPetInsuranceResponse().enqueue(object : Callback<PetInsuranceModel?> {
            override fun onResponse(
                call: Call<PetInsuranceModel?>,
                response: Response<PetInsuranceModel?>
            ) {
                if (activity != null) {
                    val petInsuranceModel = response.body()
                    if (petInsuranceModel != null) {
                        for (insuranceProduct in petInsuranceModel.insuranceProducts) {
                            if (insuranceProduct.type == PET) {
                                insuranceProducts = insuranceProduct
                            }
                        }
                        petInsuranceCheck(insuranceProducts)
                    }
                }
            }

            override fun onFailure(call: Call<PetInsuranceModel?>, t: Throwable) {
                petInsuranceCheck()
            }
        })
    }

    fun appGUIDRequest(appGUIDRequestType: AppGUIDRequestType?) {
        petInsuranceShowLoading(true)
        getAppGUIDResponse(appGUIDRequestType!!).enqueue(object : Callback<AppGUIDModel?> {
            override fun onResponse(call: Call<AppGUIDModel?>, response: Response<AppGUIDModel?>) {
                val appGUIDModel = response.body()
                if (appGUIDModel != null) {
                    if (activity != null && appGUIDModel.httpCode == 200) {
                        when(appGUIDModel.appGuid.isNullOrEmpty()){
                            true->{petInsuranceCheck()}
                            false->{
                                val (_, renderMode, petInsuranceUrl, exitUrl) = accountOptions!!.insuranceProducts
                                petInsuranceRedirect(
                                    activity, petInsuranceUrl + appGUIDModel.appGuid,
                                    renderMode == AvailableFundFragment.WEBVIEW, exitUrl
                                )
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<AppGUIDModel?>, t: Throwable) {
                petInsuranceCheck()
            }
        })
    }

    private fun petInsuranceShowLoading(animateProgress: Boolean) {
        applyPetInsuranceCardView.visibility = View.VISIBLE
        ivPetInsuranceProgress.visibility = View.VISIBLE
        if (animateProgress) {
            val rotateAnimation: RotateAnimation = mUpdateMyAccount.rotateViewAnimation()
            rotateAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    applyPetInsuranceCardView.isEnabled = false
                }

                override fun onAnimationEnd(animation: Animation) {
                    applyPetInsuranceCardView.isEnabled = true
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            ivPetInsuranceProgress.startAnimation(rotateAnimation)
        }
        tvPetInsuranceApply.visibility = View.GONE
        tvPetInsuranceCovered.visibility = View.GONE
        tvPetInsuranceHelped.visibility = View.GONE
    }

    private fun petInsuranceCheck(insuranceProduct: InsuranceProducts? = null) {
        ivPetInsuranceProgress.visibility = View.GONE
        ivPetInsuranceProgress.clearAnimation()
        if (insuranceProduct == null) {
            petInsuranceShowLoading(false)
            return
        }
        when (CoveredStatus.valueOf(insuranceProduct.status)) {
            CoveredStatus.COVERED -> {
                tvPetInsuranceApply.visibility = View.GONE
                tvPetInsuranceCovered.visibility = View.VISIBLE
                tvPetInsuranceHelped.visibility = View.VISIBLE
                tvPetInsuranceCovered.text = activity!!.getString(R.string.pet_insurance_covered)
            }
            CoveredStatus.NOT_COVERED -> {
                tvPetInsuranceApply.visibility = View.VISIBLE
                tvPetInsuranceCovered.visibility = View.GONE
                tvPetInsuranceHelped.visibility = View.GONE
            }
            CoveredStatus.PENDING -> {
                tvPetInsuranceApply.visibility = View.GONE
                tvPetInsuranceCovered.visibility = View.VISIBLE
                tvPetInsuranceHelped.visibility = View.VISIBLE
                tvPetInsuranceCovered.text = activity!!.getString(R.string.pet_insurance_pending)
            }
        }
    }
    fun navigateToPetInsurance() {
        insuranceProducts?.apply {
            if (CoveredStatus.valueOf(status) == CoveredStatus.PENDING) {
                activity?.let { showPetInsurancePendingDialog(it.supportFragmentManager) }
                return
            }
        }
        appGUIDRequest(AppGUIDRequestType.PET_INSURANCE)
    }
}