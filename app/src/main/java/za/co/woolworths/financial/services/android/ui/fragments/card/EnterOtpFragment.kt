package za.co.woolworths.financial.services.android.ui.fragments.card

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.enter_otp_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.card.LinkNewCardActivity
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment


class EnterOtpFragment : MyCardExtension() {

    companion object {
        fun newInstance() = EnterOtpFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        (activity as? LinkNewCardActivity)?.showBackIcon()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.enter_otp_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInputListeners()
        clickEvent()
        getString(R.string.enter_otp_desc)?.apply { tvEnterOtpDesc.htmlText(this) }
        imNextProcessLinkCard?.isEnabled = false
    }

    private fun clickEvent() {
        imNextProcessLinkCard?.setOnClickListener { linkCardRequest() }
        tvDidNotReceivedOTP?.setOnClickListener { (activity as? AppCompatActivity)?.let { navigateToResendOTPFragment(it) } }
    }

    private fun setupInputListeners() {
        edtVericationCode1?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length == 1) {
                    edtVerificationCode2?.requestFocus(View.FOCUS_DOWN)
                }
            }

            override fun afterTextChanged(s: Editable) {
                validateVerificationCode()
            }
        })
        edtVerificationCode2?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (count == 0) {
                    edtVericationCode1?.requestFocus(View.FOCUS_DOWN)
                } else {
                    if (s.length == 1) {
                        edtVerificationCode3?.requestFocus(View.FOCUS_DOWN)
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {
                validateVerificationCode()
            }
        })
        edtVerificationCode3?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (count == 0) {
                    edtVerificationCode2?.requestFocus(View.FOCUS_DOWN)
                } else {
                    if (s.length == 1) {
                        edtVerificationCode4?.requestFocus(View.FOCUS_DOWN)
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {
                validateVerificationCode()
            }
        })
        edtVerificationCode4?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (count == 0) {
                    edtVerificationCode3?.requestFocus(View.FOCUS_DOWN)
                } else {
                    if (s.length == 1) {
                        edtVerificationCode5?.requestFocus(View.FOCUS_DOWN)
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {
                validateVerificationCode()
            }
        })
        edtVerificationCode5?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (count == 0) {
                    edtVerificationCode4?.requestFocus(View.FOCUS_DOWN)
                }
            }

            override fun afterTextChanged(s: Editable) {
                edtVerificationCode5?.apply { isCursorVisible = s.isEmpty() }
                validateVerificationCode()
            }
        })
    }

    private fun validateVerificationCode() {
        when ((edtVericationCode1.length() == 1)
                && (edtVerificationCode2.length() == 1)
                && (edtVerificationCode3.length() == 1)
                && (edtVerificationCode4.length() == 1)
                && (edtVerificationCode5.length() == 1)) {
            true -> {
                imNextProcessLinkCard?.isEnabled = true
                imNextProcessLinkCard?.alpha = 1.0f
                imNextProcessLinkCard?.isFocusable  = false
            }
            false -> {
                imNextProcessLinkCard?.isEnabled = false
                imNextProcessLinkCard?.alpha = 0.5f
                imNextProcessLinkCard?.isFocusable  = true
            }
        }
    }

    private fun linkCardRequest() {
        replaceFragment(
                fragment = ProcessBlockCardFragment.newInstance(true),
                tag = ProcessBlockCardFragment::class.java.simpleName,
                containerViewId = R.id.flMyCard,
                allowStateLoss = true,
                enterAnimation = R.anim.slide_in_from_right,
                exitAnimation = R.anim.slide_to_left,
                popEnterAnimation = R.anim.slide_from_left,
                popExitAnimation = R.anim.slide_to_right
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                (activity as? LinkNewCardActivity)?.hideBackIcon()
                activity?.onBackPressed()
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            edtVericationCode1?.apply {
                requestFocus()
                showSoftKeyboard(it, this)
            }
        }
    }
}