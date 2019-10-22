package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.view.KeyEvent
import android.view.View
import android.view.View.FOCUS_DOWN
import kotlinx.android.synthetic.main.enter_otp_fragment.*

open class OTPInputListener : MyCardExtension() {

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
                    edtVerificationCode1?.requestFocus(FOCUS_DOWN)
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
                    edtVerificationCode1?.requestFocus(FOCUS_DOWN)
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
                    edtVerificationCode2?.requestFocus(FOCUS_DOWN)
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
                    edtVerificationCode3?.requestFocus(FOCUS_DOWN)
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
                    edtVerificationCode4?.requestFocus(FOCUS_DOWN)
                }
                edtVerificationCode5?.text?.clear()
                validateVerificationCode()
                return@OnKeyListener true
            }
            false
        })

        // Disable touch event on EditText
        edtVerificationCode1?.setOnTouchListener { _, _ -> true }
        edtVerificationCode2?.setOnTouchListener { _, _ -> true }
        edtVerificationCode3?.setOnTouchListener { _, _ -> true }
        edtVerificationCode4?.setOnTouchListener { _, _ -> true }
        edtVerificationCode5?.setOnTouchListener { _, _ -> true }
    }

    private fun validateVerificationCode() {
        when ((edtVerificationCode1.length() == 1)
                && (edtVerificationCode2.length() == 1)
                && (edtVerificationCode3.length() == 1)
                && (edtVerificationCode4.length() == 1)
                && (edtVerificationCode5.length() == 1)) {
            true -> {
                imNextProcessLinkCard?.isEnabled = true
                imNextProcessLinkCard?.alpha = 1.0f
                imNextProcessLinkCard?.isFocusable = false
            }
            false -> {
                imNextProcessLinkCard?.isEnabled = false
                imNextProcessLinkCard?.alpha = 0.5f
                imNextProcessLinkCard?.isFocusable = true
            }
        }
    }
}