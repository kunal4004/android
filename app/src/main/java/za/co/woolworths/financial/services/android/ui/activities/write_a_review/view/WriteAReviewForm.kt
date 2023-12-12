package za.co.woolworths.financial.services.android.ui.activities.write_a_review.view

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.WriteAReviewFormBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.write_a_review.request.PrepareWriteAReviewFormRequestEvent
import za.co.woolworths.financial.services.android.ui.activities.write_a_review.viewmodel.WriteAReviewFormViewModel
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager.Companion.logException


@AndroidEntryPoint
class WriteAReviewForm : Fragment(), View.OnClickListener {
    private var _binding: WriteAReviewFormBinding? = null
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
        @SuppressLint("ObjectAnimatorBinding")
        val OBJECT_ANIMATOR = ObjectAnimator.ofInt(null, "scrollY", 0).setDuration(500)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            productName = getString(PRODUCT_NAME)
            image = getString(IMAGE_PATH)
            productId = getString(PRODUCT_ID)
        }
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

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
        binding.termsAndCondition.setOnClickListener(this@WriteAReviewForm)
        CustomRatingBar.clicked = false
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
        val bottomSheetBinding = BottomSheetDialog(requireContext())
        bottomSheetBinding.apply {
            val view = layoutInflater.inflate(R.layout.write_a_review_bottom_sheet_dialog, null)
            val close = view.findViewById<Button>(R.id.close)
            close?.setOnClickListener { dismiss() }
            setContentView(view)
            setCancelable(true)
            show()
        }
    }

    private fun validateSubmitForm() {
        var rating: Double? = null
        var ratingQualityValue: Int? = null
        var ratingValueBox: Int? = null
        var reviewText: String? = null
        var title: String? = null
        var nickName: String? = null
        var mid: Int
        if (binding.ratingBar.rating == 0f) {
            resources.displayMetrics.let {
                 mid = it.heightPixels / 2 - binding.errorMsgOfRatingbar.height
            }
            animateScrollView(binding,mid)
            CustomRatingBar.clicked = true
            binding.ratingBar.drawBoundingBox()
            binding.errorMsgOfRatingbar.visibility = View.VISIBLE
        } else {
            CustomRatingBar.clicked = false
            binding.ratingBar.drawBoundingBox()
            rating = binding.ratingBar.rating.toDouble()
        }
        if (!binding.yesButton.isSelected && !binding.noButton.isSelected) {
            resources.displayMetrics.let {
                 mid = it.heightPixels / 2 - binding.errorMsgOfToggleBtn.height
            }
            animateScrollView(binding,mid)
            binding.yesButton.background =
                ResourcesCompat.getDrawable(resources, R.drawable.error_edit_box, null)
            binding.noButton.background =
                ResourcesCompat.getDrawable(resources, R.drawable.error_edit_box, null)
            startShakeAnimation(binding.rccd)
            binding.errorMsgOfToggleBtn.visibility = View.VISIBLE

        }
        if (binding.reviewTitleEdit.text.isNullOrEmpty() || (binding.reviewTitleEdit.text.length <= 1)) {
            resources.displayMetrics.let {
                 mid = it.heightPixels / 2 - binding.errorMsgOfReviewTitle.height
            }
            animateScrollView(binding,mid)
            binding.reviewInput.background =
                ResourcesCompat.getDrawable(resources, R.drawable.error_edit_box, null)
            startShakeAnimation(binding.reviewInput)
            binding.errorMsgOfReviewTitle.visibility = View.VISIBLE

        } else {
            title = binding.reviewTitleEdit.text.toString()

        }
        if (binding.descrEdt.text.isNullOrEmpty() || (binding.descrEdt.text.length <= 10)) {
            binding.descInput.background =
                ResourcesCompat.getDrawable(resources, R.drawable.error_edit_box, null)
            startShakeAnimation(binding.descInput)
            binding.errorMsgOfReviewDesc.visibility = View.VISIBLE

        } else {
            reviewText = binding.descrEdt.text.toString()
        }
        if (binding.nameEdt.text.isNullOrEmpty()) {
            binding.nameInput.background =
                ResourcesCompat.getDrawable(resources, R.drawable.error_edit_box, null)
            startShakeAnimation(binding.nameInput)
            binding.errorMsgOfDisplayName.visibility = View.VISIBLE

        } else {
            nickName = binding.nameEdt.text.toString()
        }
        ratingQualityValue = ratingQuality?.toInt()
        ratingValueBox = ratingValue?.toInt()
        if ((rating != null) && (title != null) && (reviewText != null) && (nickName != null) && (isrecommended == true || isrecommended == false)) {
            submitForm(
                rating?.toInt(),
                title,
                reviewText,
                nickName,
                isrecommended,
                ratingQualityValue,
                ratingValueBox
            )
        }


    }

    private fun startShakeAnimation(rating: View) {
        val shakeAnimation = AnimationUtils.loadAnimation(context, R.anim.edit_text_shake)
        rating.startAnimation(shakeAnimation)
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

    private fun submitForm(
        rating: Int?,
        title: String?,
        reviewText: String?,
        nickName: String?,
        isrecommended: Boolean?,
        ratingQualityValue: Int?,
        ratingValueBox: Int?
    ) {

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
            CustomRatingBar.clicked = true
            binding.ratingBar.drawBoundingBox()
            binding.errorMsgOfRatingbar.visibility = View.GONE
            submitButtonBackgroundChange()
        }
        binding.reviewTitleEdit?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.reviewInput?.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.edit_box_write_a_review, null)

                if (binding.reviewTitleEdit.text.toString().trim().length < 151) {
                    binding.reviewInput?.background = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.edit_box_write_a_review,
                        null
                    )
                    val number = s?.length.toString()
                    binding.reviewTitleEditCounter.text = number
                    submitButtonBackgroundChange()
                } else {
                    binding.reviewTitleEditCounter.text = "0"
                }
                binding.errorMsgOfReviewTitle.visibility = View.GONE
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        binding.descrEdt?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.descInput?.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.edit_box_write_a_review, null)

                if (binding.descrEdt.text.toString().length < 1501) {
                    val number = (1500 - binding.descrEdt.text.toString().trim().length).toString()
                    binding.descrEdtCounter.text = number
                    submitButtonBackgroundChange()
                } else {
                    binding.descrEdtCounter.text = "1500"
                }
                binding.errorMsgOfReviewDesc.visibility = View.GONE
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
                    ResourcesCompat.getDrawable(resources, R.drawable.edit_box_write_a_review, null)
                binding.errorMsgOfDisplayName.visibility = View.GONE
                submitButtonBackgroundChange()
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
                validateSubmitForm()

            }

            R.id.descp -> {
                showButtomDialog()
            }

            R.id.yesButton -> {
                binding.yesButton.isSelected = true
                binding.noButton.isSelected = false
                isrecommended = true
                binding.errorMsgOfToggleBtn.visibility = View.GONE
                binding.yesButton.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.customrating, null)
                binding.noButton.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.customrating, null)
                submitButtonBackgroundChange()
            }

            R.id.noButton -> {
                binding.noButton.isSelected = true
                binding.yesButton.isSelected = false
                isrecommended = false
                binding.errorMsgOfToggleBtn.visibility = View.GONE
                binding.yesButton.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.customrating, null)
                binding.noButton.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.customrating, null)
                submitButtonBackgroundChange()
            }

            R.id.terms_and_condition -> {
                (activity as? BottomNavigationActivity)?.openWriteAReviewTnCScreenFragment(AppConstant.actionItemTnC)
            }

        }
    }
    private fun animateScrollView(binding: WriteAReviewFormBinding, mid: Int) {
        OBJECT_ANIMATOR.target = binding.scrollView
        OBJECT_ANIMATOR.setIntValues(mid)
        OBJECT_ANIMATOR.duration = 500
        OBJECT_ANIMATOR.start()
    }

    private fun submitButtonBackgroundChange() {
        if (binding.ratingBar.rating.toDouble() != null && binding.descrEdt.text.toString() != null && binding.nameEdt.text.toString() != null && (isrecommended == true || isrecommended == false)) {
            binding.submit.background = ResourcesCompat.getDrawable(resources, R.color.black,null)
        } else {
            binding.submit.background = ResourcesCompat.getDrawable(resources, R.color.gray, null)
        }
    }

}