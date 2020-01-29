package za.co.woolworths.financial.services.android.ui.fragments.otp

import android.app.Activity
import android.content.Context
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_enter_otp.*
import za.co.woolworths.financial.services.android.ui.fragments.npc.OTPViewTextWatcher
import za.co.woolworths.financial.services.android.util.KeyboardUtil

open class EnterOTPFragmentExtension : Fragment() {
    fun setupInputListeners() {

        edtVerificationCode1?.addTextChangedListener(OTPViewTextWatcher(edtVerificationCode1, edtVerificationCode1, edtVerificationCode2) { validateVerificationCode() })
        edtVerificationCode2?.addTextChangedListener(OTPViewTextWatcher(edtVerificationCode1, edtVerificationCode2, edtVerificationCode3) { validateVerificationCode() })
        edtVerificationCode3?.addTextChangedListener(OTPViewTextWatcher(edtVerificationCode2, edtVerificationCode3, edtVerificationCode4) { validateVerificationCode() })
        edtVerificationCode4?.addTextChangedListener(OTPViewTextWatcher(edtVerificationCode3, edtVerificationCode4, edtVerificationCode5) { validateVerificationCode() })
        edtVerificationCode5?.addTextChangedListener(OTPViewTextWatcher(edtVerificationCode4, edtVerificationCode5, edtVerificationCode5) { validateVerificationCode() })

        edtVerificationCode1.setOnKeyListener(View.OnKeyListener
        { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                if (edtVerificationCode1?.text?.isEmpty() == true) {
                    edtVerificationCode1?.setSelection(edtVerificationCode1?.text?.length ?: 0)
                    edtVerificationCode1?.requestFocus(View.FOCUS_DOWN)
                }
                edtVerificationCode1?.text?.clear()
                validateVerificationCode()
                return@OnKeyListener true
            }
            false
        })

        edtVerificationCode2.setOnKeyListener(View.OnKeyListener
        { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {

                if (edtVerificationCode2?.text?.isEmpty() == true) {
                    edtVerificationCode1?.setSelection(edtVerificationCode1?.text?.length ?: 0)
                    edtVerificationCode1?.requestFocus(View.FOCUS_DOWN)
                }

                edtVerificationCode2?.text?.clear()
                validateVerificationCode()
                return@OnKeyListener true
            }
            false
        })

        edtVerificationCode3.setOnKeyListener(View.OnKeyListener
        { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {

                if (edtVerificationCode3?.text?.isEmpty() == true) {
                    edtVerificationCode2?.setSelection(edtVerificationCode2?.text?.length ?: 0)
                    edtVerificationCode2?.requestFocus(View.FOCUS_DOWN)
                }
                edtVerificationCode3?.text?.clear()
                validateVerificationCode()
                return@OnKeyListener true
            }
            false
        })

        edtVerificationCode4.setOnKeyListener(View.OnKeyListener
        { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                if (edtVerificationCode4?.text?.isEmpty() == true) {
                    edtVerificationCode3?.setSelection(edtVerificationCode3?.text?.length ?: 0)
                    edtVerificationCode3?.requestFocus(View.FOCUS_DOWN)
                }
                edtVerificationCode4?.text?.clear()
                validateVerificationCode()
                return@OnKeyListener true
            }
            false
        })

        edtVerificationCode5.setOnKeyListener(View.OnKeyListener
        { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                if (edtVerificationCode5?.text?.isEmpty() == true) {
                    edtVerificationCode4?.setSelection(edtVerificationCode4?.text?.length ?: 0)
                    edtVerificationCode4?.requestFocus(View.FOCUS_DOWN)
                }
                edtVerificationCode5?.text?.clear()
                validateVerificationCode()
                return@OnKeyListener true
            }
            false
        })

    }

    private fun validateVerificationCode() {
        when ((edtVerificationCode1.length() == 1)
                && (edtVerificationCode2.length() == 1)
                && (edtVerificationCode3.length() == 1)
                && (edtVerificationCode4.length() == 1)
                && (edtVerificationCode5.length() == 1)) {
            true -> {
                buttonNext?.isEnabled = true
                buttonNext?.alpha = 1.0f
                buttonNext?.isFocusable = false
            }
            false -> {
                buttonNext?.isEnabled = false
                buttonNext?.alpha = 0.5f
                buttonNext?.isFocusable = true
            }
        }
        otpErrorTextView?.visibility = View.GONE
    }

    fun getNumberFromEditText(numberEditText: EditText?) = numberEditText?.text?.toString()
            ?: ""

    fun showSoftKeyboard(activity: Activity, editTextView: EditText) {
        activity.apply {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.apply {
                showSoftInput(editTextView, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    open fun hideKeyboard() {
        activity?.apply { KeyboardUtil.hideSoftKeyboard(this) }
    }

}