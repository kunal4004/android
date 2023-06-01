package za.co.woolworths.financial.services.android.enhancedSubstitution.view

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.R
import com.awfs.coordination.databinding.SubstitutionErrorScreenBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.cart.view.SubstitutionChoice
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.AddSubstitutionRequest
import za.co.woolworths.financial.services.android.enhancedSubstitution.view.SearchSubstitutionFragment.Companion.SELECTED_SUBSTITUTED_PRODUCT
import za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel.ProductSubstitutionViewModel
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

@AndroidEntryPoint
class SubstitutionProcessingScreen : BaseFragmentBinding<SubstitutionErrorScreenBinding>(
    SubstitutionErrorScreenBinding::inflate
), View.OnClickListener {

    private var commerceItemId = ""
    private var skuId = ""
    private val productSubstitutionViewModel: ProductSubstitutionViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.apply {
            commerceItemId = getString(ManageSubstitutionFragment.COMMERCE_ITEM_ID, "")
            skuId = getString(ManageSubstitutionFragment.SKU_ID, "")
        }
        initErrorView()
    }

    fun newInstance(
        commerceItemId: String?,
        skuId: String? = "",
    ) = SubstitutionProcessingScreen().withArgs {
        putString(ManageSubstitutionFragment.COMMERCE_ITEM_ID, commerceItemId)
        putString(ManageSubstitutionFragment.SKU_ID, skuId)
    }

    private fun initErrorView() {
        binding.substitutionErrorLayout.apply {
            root.visibility = VISIBLE
            errorDescription.visibility = GONE
            errorLogo.setImageResource(R.drawable.ic_error_icon)
            errorTitle.text =
                getString(R.string.add_substitution_error_msg)
            actionButton.text = getString(R.string.retry)
            cancelButton.visibility = VISIBLE
            cancelButton.text = context?.getString(R.string.back)
            actionButton.setOnClickListener(this@SubstitutionProcessingScreen)
            cancelButton.setOnClickListener(this@SubstitutionProcessingScreen)
        }
    }

    private fun addSubstitutionApiCall() {
        val addSubstitutionRequest = AddSubstitutionRequest(
            substitutionSelection = SubstitutionChoice.USER_CHOICE.name,
            substitutionId = skuId,
            commerceItemId = commerceItemId
        )
        productSubstitutionViewModel.addSubstitutionForProduct(addSubstitutionRequest)
        productSubstitutionViewModel.addSubstitutionResponse.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.substitutionProcressLayout.root.visibility = VISIBLE
                        binding.substitutionProcressLayout.processRequestLayout.apply {
                            processRequestTitleTextView.text =
                                context?.getString(R.string.processing_your_request)
                            processRequestDescriptionTextView.text =
                                context?.getString(R.string.processing_your_request_desc)
                        }
                        startSpinning()
                    }

                    Status.SUCCESS -> {
                        binding.substitutionProcressLayout.root.visibility = GONE
                        stopSpinning()/* if we get form exception need to show error popup*/
                        resource.data?.data?.getOrNull(0)?.formExceptions?.getOrNull(0)?.let {
                            if (it.message?.isNotEmpty() == true) {
                               binding.substitutionErrorLayout.root.visibility = VISIBLE
                            }
                            return@observe
                        }
                        binding.substitutionSuccessLayout.root.visibility = VISIBLE
                        binding.substitutionSuccessLayout.apply {
                            txtOrderPaymentConfirmed.text =
                                context?.getString(R.string.add_substitution_success_msg)
                            imgPaymentSuccess.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                    resources, R.drawable.success_tick, null
                                )
                            )
                            btnGotIt.visibility = VISIBLE
                            btnGotIt.setOnClickListener {
                                // navigate to pdp and call getSubs. api
                                setFragmentResult(
                                    SELECTED_SUBSTITUTED_PRODUCT,
                                    bundleOf(SearchSubstitutionFragment.SUBSTITUTION_ITEM_ADDED to true)
                                )
                                (activity as? BottomNavigationActivity)?.popFragment()
                            }
                        }
                    }

                    Status.ERROR -> {
                            binding.substitutionProcressLayout.root.visibility = GONE
                            stopSpinning()
                            binding.substitutionErrorLayout.root.visibility = VISIBLE
                    }
                }
            }
        })
    }

    fun stopSpinning() {
        binding.substitutionProcressLayout.includeCircleProgressLayout.circularProgressIndicator.apply {
            stopSpinning()
            setValueAnimated(100f)
        }
    }

    fun startSpinning() {
        binding.substitutionProcressLayout.includeCircleProgressLayout.circularProgressIndicator.spin()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.actionButton -> {
                addSubstitutionApiCall()
            }

            R.id.cancelButton -> {
                (activity as? BottomNavigationActivity)?.popFragment()
            }
        }
    }
}