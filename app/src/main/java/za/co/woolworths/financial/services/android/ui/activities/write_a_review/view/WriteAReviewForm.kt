package za.co.woolworths.financial.services.android.ui.activities.write_a_review.view

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.WriteAReviewBottomSheetDialogBinding
import com.awfs.coordination.databinding.WriteAReviewFormBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.write_a_review.request.PrepareWriteAReviewFormRequestEvent
import za.co.woolworths.financial.services.android.ui.activities.write_a_review.viewmodel.WriteAReviewFormViewModel
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.ImageManager
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager.Companion.logException


@AndroidEntryPoint
class WriteAReviewForm : Fragment(), View.OnClickListener {
    private var _binding: WriteAReviewFormBinding? = null
    private var _bottomSheetBinding: WriteAReviewBottomSheetDialogBinding? = null
    private val bottomSheetBinding get() = _bottomSheetBinding!!
    var isrecommended: Boolean? = null
    private val binding get() = _binding!!
    var imageView: ImageView? = null
    var recyclerView: RecyclerView? = null
    var ratingValue: String? = null
    var ratingQuality: String? = null
    private var image: String? = null
    private var productName: String? = null
    private var productId: String? = null
    private var mErrorHandlerView: ErrorHandlerView? = null
    private val writeAReviewFormViewModel: WriteAReviewFormViewModel by viewModels()


    private val boxes: List<TextView> by lazy {
        listOf(
            binding.boxOne,
            binding.boxTwo,
            binding.boxThree,
            binding.boxFour,
            binding.boxFive
        )
    }

    private val boxesQual: List<TextView> by lazy {
        listOf(
            binding.boxQual1,
            binding.boxQual2,
            binding.boxQual3,
            binding.boxQual4,
            binding.boxQual5
        )
    }

    companion object {
        fun newInstance() = WriteAReviewForm()

        const val PRODUCT_NAME = "PRODUCT_NAME"
        const val IMAGE_PATH = "IMGE_PATH"
        const val PRODUCT_ID = "PRODUCT_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            productName = getString(PRODUCT_NAME)
            image = getString(IMAGE_PATH)
            productId = getString(PRODUCT_ID)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = WriteAReviewFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.photInp

        val gridLayout = GridLayoutManager(requireContext(), 3)
        recyclerView?.layoutManager = gridLayout

        binding.backArrow.setOnClickListener(this@WriteAReviewForm)
        getUserNickName()
        configureDefaultUI()
        editable()
        reviewSubmitForm()
        reviewDescriptionHintPopUp()
        ratingValueSelectable()
        productQualitySelectable()

    }

    private fun productQualitySelectable() {
        for (quals in boxesQual) {
            quals.setOnClickListener {
                quals.text.toString().also { ratingValue = it }
                handleBoxQualCLick(boxesQual.indexOf(quals) + 1)
            }
        }
    }

    private fun ratingValueSelectable() {
        for (box in boxes) {
            box.setOnClickListener {
                box.text.toString().also { ratingQuality = it }
                handleBoxCLick(boxes.indexOf(box) + 1)
            }
        }
    }

    private fun reviewDescriptionHintPopUp() {
        binding.descp.setOnClickListener(this@WriteAReviewForm)
    }

    private fun reviewSubmitForm() {
        binding.submit.setOnClickListener(this@WriteAReviewForm)
    }

    private fun getUserNickName() {
        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            return
        }
        val userNickName = SessionUtilities.getInstance().jwt.name[0]
        binding.nameEdt.setText(userNickName)
    }

    private fun configureDefaultUI() {
        if (productName?.isNotEmpty() == true) {
            binding.productName.text = productName
        }
        if (image?.isNotEmpty() == true) {
            ImageManager.loadImage(binding.imageView, image ?: "")
        }
        toggleBUttoClickEvent()
    }

    private fun toggleBUttoClickEvent() {
        binding.yesButton.setOnClickListener(this@WriteAReviewForm)
        binding.noButton.setOnClickListener(this@WriteAReviewForm)
    }

    private fun backToPreviousScreen() {
        (activity as? BottomNavigationActivity)?.popFragment()
    }

    private fun handleBoxQualCLick(selectedBox: Int) {
        for (i in boxesQual.indices) {
            boxesQual[i].isSelected = i < selectedBox
        }
    }

    private fun handleBoxCLick(selectedBox: Int) {
        for (i in boxes.indices) {
            boxes[i].isSelected = i < selectedBox
        }
    }

    private fun showButtomDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.write_a_review_bottom_sheet_dialog)
        bottomSheetDialog.show()
        _bottomSheetBinding?.close?.setOnClickListener {
            if (bottomSheetDialog.isShowing)
                bottomSheetDialog.dismiss()
            else
                bottomSheetDialog.show()
        }
    }

    private fun validateSubmitForm(): Boolean {
        if (binding.ratingBar.rating == 0f) {
            binding.rating.setTextColor(resources.getColor(R.color.red))
            focusOnView(binding.rating)
            startShakeAnimation(binding.rating)
            startShakeAnimation(binding.ratingBar)
            return false
        } else if (binding.reviewTitleEdit.text.isNullOrEmpty() || (binding.reviewTitleEdit.text.length <= 1)) {
            binding.rvwTitle.setTextColor(resources.getColor(R.color.red))
            binding.reviewInput.background = resources.getDrawable(R.drawable.error_edit_box)
            startShakeAnimation(binding.rvwTitle)
            startShakeAnimation(binding.reviewInput)
            return false
        } else if (binding.descrEdt.text.isNullOrEmpty() || (binding.descrEdt.text.length <= 10)) {
            binding.rvwDes.setTextColor(resources.getColor(R.color.red))
            binding.descInput.background = resources.getDrawable(R.drawable.error_edit_box)
            startShakeAnimation(binding.rvwDes)
            startShakeAnimation(binding.descInput)
            return false
        } else if (binding.nameEdt.text.isNullOrEmpty()) {
            binding.displayName.setTextColor(resources.getColor(R.color.red))
            binding.nameInput.background = resources.getDrawable(R.drawable.error_edit_box)
            startShakeAnimation(binding.displayName)
            startShakeAnimation(binding.nameInput)
            return false
        } else return true
    }

    private fun startShakeAnimation(rating: View) {
        val shakeAnimation = AnimationUtils.loadAnimation(context, R.anim.edit_text_shake)
        rating.startAnimation(shakeAnimation)
    }

    private fun focusOnView(rating: TextView) {
        lifecycleScope.launch {
            binding.scrollView.scrollTo(0, rating.top)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun hideProgressBar() {
        binding.writeAReviewProgressBar.visibility = View.GONE
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun showProgressBar() {
        binding.writeAReviewProgressBar.visibility = View.VISIBLE
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun submitForm() {
        val rating = binding.ratingBar.rating.toInt()
        val reviewText = binding.descrEdt.text.toString()
        val title = binding.reviewTitleEdit.text.toString()
        val nickName = binding.nameEdt.text.toString()
        val ratingQualityValue = ratingQuality?.toInt()
        val ratingValueBox = ratingValue?.toInt()
        val prepareWriteAReviewFormRequestEvent = PrepareWriteAReviewFormRequestEvent(
            nickName,
            rating,
            ratingQualityValue,
            ratingValueBox,
            isrecommended,
            title,
            reviewText
        )
        writeAReviewFormViewModel.createWriteAReviewFormRequest(
            productId,
            prepareWriteAReviewFormRequestEvent
        )
        writeAReviewFormViewModel.writeAReviewFormResponseData.observe(viewLifecycleOwner) {
            val response = it.peekContent().data
            when (it.peekContent().status) {
                Status.LOADING -> {
                    showProgressBar()
                }
                Status.SUCCESS -> {
                    try {
                        if (response?.httpCode == AppConstant.HTTP_OK) {
                            (activity as? BottomNavigationActivity)?.openWriteAReviewSuccessScreenFragment(
                                response.toString()
                            )
                        } else {

                        }
                        hideProgressBar()
                    } catch (ex: Exception) {
                        logException(ex)
                    }
                }
                Status.ERROR -> {
                    requireActivity().runOnUiThread {
                        hideProgressBar()
                        mErrorHandlerView?.showToast()
                    }
                }


            }

        }
    }

    private fun editable() {
        binding.ratingBar?.setOnRatingBarChangeListener { p0, p1, p2 ->
            binding.rating?.setTextColor(resources.getColor(R.color.text_colors))
        }
        binding.reviewTitleEdit?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.reviewInput?.background =
                    resources.getDrawable(R.drawable.edit_box_write_a_review)

                if (binding.reviewTitleEdit.text.toString().trim().length < 151) {
                    binding.reviewInput?.background =
                        resources.getDrawable(R.drawable.edit_box_write_a_review)
                    val number = s?.length.toString()
                    binding.reviewTitleEditCounter.text = number
                } else {
                    binding.reviewTitleEditCounter.text = "0"
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        binding.descrEdt?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.descInput?.background =
                    resources.getDrawable(R.drawable.edit_box_write_a_review)
                if (binding.descrEdt.text.toString().length < 1501) {
                    val number = (1500 - binding.descrEdt.text.toString().trim().length).toString()
                    binding.descrEdtCounter.text = number
                } else {
                    binding.descrEdtCounter.text = "1500"
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.nameEdt?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.displayName?.focusable = View.NOT_FOCUSABLE
                binding.displayName?.setTextColor(resources.getColor(R.color.text_colors))
                binding.nameInput?.background =
                    resources.getDrawable(R.drawable.edit_box_write_a_review)

            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backArrow -> {
                backToPreviousScreen()
            }

            R.id.submit -> {
                if (validateSubmitForm()) {
                    submitForm()
                }
            }

            R.id.descp -> {
                showButtomDialog()
            }

            R.id.yesButton -> {
                binding.yesButton.isSelected = true
                binding.noButton.isSelected = false
                isrecommended = true
            }

            R.id.noButton -> {
                binding.noButton.isSelected = true
                binding.yesButton.isSelected = false
                isrecommended = false
            }

        }
    }

}