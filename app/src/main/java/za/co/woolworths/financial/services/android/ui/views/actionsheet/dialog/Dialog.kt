package za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog

import android.app.AlertDialog
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment

open class Dialog : AppCompatDialogFragment(), View.OnClickListener {
    private val mDialogParams: DialogParams
    protected fun show(activity: AppCompatActivity?, tag: String?) {
        if (!TextUtils.isEmpty(tag)) {
            activity?.supportFragmentManager?.let { show(it, tag) }
        } else {
            show(activity)
        }
    }

    protected fun show(activity: AppCompatActivity?) {
        activity?.supportFragmentManager?.let { show(it, activity.javaClass.simpleName) }
    }

    protected fun isNonEmpty(content: String?): Boolean {
        return !TextUtils.isEmpty(content)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.setCanceledOnTouchOutside(setCancelable())
        val window = dialog?.window
        val animationsRes = setAnimations()
        if (animationsRes != 0 && animationsRes != -1) {
            window?.setWindowAnimations(animationsRes)
        }
        isCancelable = setCancelable()
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val gravity = setGravity()
        if (gravity != -1 && gravity != 0) {
            window?.setGravity(gravity)
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = params
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        dismiss()
        super.onConfigurationChanged(newConfig)
    }

    /**
     * @return
     * @see Gravity
     */
    protected fun setGravity(): Int {
        return Gravity.CENTER
    }

    protected open fun setCancelable(): Boolean {
        return mDialogParams.isCancelable
    }

    @StyleRes
    protected fun setAnimations(): Int {
        return mDialogParams.animations
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): android.app.Dialog {
        val builder = AlertDialog.Builder(context)
        val dialogLayout = LayoutInflater.from(context).inflate(setLayoutRes(), null)
        builder.setView(dialogLayout)
        onViewCreated(dialogLayout, null)
        return builder.create()
    }

    private fun setLayoutRes(): Int {
        return mDialogParams.contentView
    }

    override fun onClick(v: View) {}
    inner class DialogParams {
        var activity: AppCompatActivity? = null
        var style = 0
        var animations = 0
        var contentView = 0
        var tag: String? = null
        var isCancelable = false
    }

    class Builder(activity: AppCompatActivity) {
        private val dialogParams: DialogParams
        private val dialog: Dialog = Dialog()
        fun setStyle(style: Int): Builder {
            dialogParams.style = style
            return this
        }

        fun setAnimations(value: Int): Builder {
            dialogParams.animations = value
            return this
        }

        fun setContentView(@LayoutRes layoutId: Int): Builder {
            dialogParams.contentView = layoutId
            return this
        }

        fun setCancelable(value: Boolean): Builder {
            dialogParams.isCancelable = value
            return this
        }

        fun build(): Dialog {
            require(dialogParams.contentView != -1) { "Please set setContentView" }
            dialog.show(dialogParams.activity, dialogParams.tag)
            return dialog
        }

        init {
            dialogParams = dialog.mDialogParams
            dialogParams.activity = activity
        }
    }

    companion object {
        fun newBuilder(activity: AppCompatActivity): Builder {
            return Builder(activity)
        }
    }

    init {
        mDialogParams = DialogParams()
    }
}