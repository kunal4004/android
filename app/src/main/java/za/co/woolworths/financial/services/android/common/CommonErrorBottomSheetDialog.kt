package za.co.woolworths.financial.services.android.common

import android.content.Context

interface CommonErrorBottomSheetDialog {


    fun showCommonErrorBottomDialog(
        onClickListener: ClickOnDialogButton,
        context: Context,
        title: String,
        desc: String,
        buttonText: String,
        isDismissButtonNeeded: Boolean,
        isCanceledOnTouchOutside : Boolean
    )
}