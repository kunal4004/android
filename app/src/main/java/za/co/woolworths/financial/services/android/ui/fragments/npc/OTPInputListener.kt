package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.text.Editable
import android.view.KeyEvent
import android.view.View
import android.view.View.FOCUS_DOWN
import kotlinx.android.synthetic.main.enter_otp_fragment.*
import za.co.woolworths.financial.services.android.ui.views.TextWatcherExtended


open class OTPInputListener : MyCardExtension() {

    fun setupInputListeners() {
        edtVerificationCode1?.addTextChangedListener(object : TextWatcherExtended() {
            override fun afterTextChanged(s: Editable, backSpace: Boolean) {
                if (backSpace) {
                    edtVerificationCode1?.text?.clear()
                    edtVerificationCode1?.requestFocus(FOCUS_DOWN)
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                when (s.count()) {
                    0 -> edtVerificationCode1?.requestFocus(FOCUS_DOWN)
                    1 -> edtVerificationCode2?.requestFocus(FOCUS_DOWN)
                }
            }
        })

        edtVerificationCode2?.addTextChangedListener(object : TextWatcherExtended() {

            override fun afterTextChanged(s: Editable, backSpace: Boolean) {
                if (backSpace) {
                    edtVerificationCode2?.text?.clear()
                    edtVerificationCode1?.requestFocus(FOCUS_DOWN)
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                when (s.count()) {
                    0 -> edtVerificationCode1?.requestFocus(FOCUS_DOWN)
                    1 -> edtVerificationCode3?.requestFocus(FOCUS_DOWN)
                }
            }

        })

        edtVerificationCode3?.addTextChangedListener(object : TextWatcherExtended() {

            override fun afterTextChanged(s: Editable, backSpace: Boolean) {
                if (backSpace) {
                    edtVerificationCode3?.text?.clear()
                    edtVerificationCode2?.requestFocus(FOCUS_DOWN)
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                when (s.count()) {
                    0 -> edtVerificationCode2?.requestFocus(FOCUS_DOWN)
                    1 -> edtVerificationCode4?.requestFocus(FOCUS_DOWN)
                }
            }
        })

        edtVerificationCode4?.addTextChangedListener(object : TextWatcherExtended() {

            override fun afterTextChanged(s: Editable, backSpace: Boolean) {
                if (backSpace) {
                    edtVerificationCode4?.text?.clear()
                    edtVerificationCode3?.requestFocus(FOCUS_DOWN)
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                when (s.count()) {
                    0 -> edtVerificationCode3?.requestFocus(FOCUS_DOWN)
                    1 -> edtVerificationCode5?.requestFocus(FOCUS_DOWN)
                }
            }
        })

        edtVerificationCode5?.addTextChangedListener(object : TextWatcherExtended() {

            override fun afterTextChanged(s: Editable, backSpace: Boolean) {
                if (backSpace) {
                    edtVerificationCode5?.text?.clear()
                    edtVerificationCode4?.requestFocus(FOCUS_DOWN)
                }

                validateVerificationCode()
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                when (s.count()) {
                    0 -> edtVerificationCode4?.requestFocus(FOCUS_DOWN)
                    1 -> edtVerificationCode5?.requestFocus(FOCUS_DOWN)
                }
                edtVerificationCode5?.isCursorVisible = false

            }
        })

        edtVerificationCode1.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                if (edtVerificationCode1?.text?.isEmpty() == true) {
                    edtVerificationCode1?.setSelection(edtVerificationCode1?.text?.length ?: 0)
                    edtVerificationCode1?.requestFocus(FOCUS_DOWN)
                }
                edtVerificationCode1?.text?.clear()
                return@OnKeyListener true
            }
            false
        })

        edtVerificationCode2.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {

                if (edtVerificationCode2?.text?.isEmpty() == true) {
                    edtVerificationCode1?.setSelection(edtVerificationCode1?.text?.length ?: 0)
                    edtVerificationCode1?.requestFocus(FOCUS_DOWN)
                }

                edtVerificationCode2?.text?.clear()
                return@OnKeyListener true
            }
            false
        })

        edtVerificationCode3.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {

                if (edtVerificationCode3?.text?.isEmpty() == true) {
                    edtVerificationCode2?.setSelection(edtVerificationCode2?.text?.length ?: 0)
                    edtVerificationCode2?.requestFocus(FOCUS_DOWN)
                }
                edtVerificationCode3?.text?.clear()
                return@OnKeyListener true
            }
            false
        })

        edtVerificationCode4.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                if (edtVerificationCode4?.text?.isEmpty() == true) {
                    edtVerificationCode3?.setSelection(edtVerificationCode3?.text?.length ?: 0)
                    edtVerificationCode3?.requestFocus(FOCUS_DOWN)
                }
                edtVerificationCode4?.text?.clear()
                return@OnKeyListener true
            }
            false
        })

        edtVerificationCode5.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                if (edtVerificationCode5?.text?.isEmpty() == true) {
                    edtVerificationCode4?.setSelection(edtVerificationCode4?.text?.length ?: 0)
                    edtVerificationCode4?.requestFocus(FOCUS_DOWN)
                }
                edtVerificationCode5?.text?.clear()

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