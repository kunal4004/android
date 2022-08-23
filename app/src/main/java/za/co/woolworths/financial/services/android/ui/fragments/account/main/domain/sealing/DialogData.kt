package za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing

import android.os.Parcelable
import android.view.View.VISIBLE
import androidx.annotation.StringRes
import com.awfs.coordination.R
import kotlinx.android.parcel.Parcelize

sealed class DialogData : Parcelable {
    abstract val title: Int
    abstract val desc: Int
    abstract val firstButtonTitle: Int
    abstract val secondButtonTitle: Int
    abstract var formattedValue : String?
    abstract val secondButtonVisibility: Int

    @Parcelize
    data class EliteDialog(
        @StringRes override val title: Int = R.string.account_in_recovery_label,
        @StringRes override val desc: Int = R.string.remove_block_on_collection_dialog_desc,
        @StringRes override var firstButtonTitle: Int = R.string.make_payment_now_button_label,
        @StringRes override val secondButtonTitle: Int = R.string.cannot_afford_payment_button_label,
        override var secondButtonVisibility: Int = VISIBLE,
        override var formattedValue: String? = ""
    ) : DialogData()

    @Parcelize
    data class VipDialog(
        @StringRes override val title: Int = R.string.account_in_recovery_label,
        @StringRes override val desc: Int = R.string.view_treatment_plan_description,
        @StringRes override val firstButtonTitle: Int = R.string.make_payment_now_button_label,
        @StringRes override val secondButtonTitle: Int = R.string.cannot_afford_payment_button_label,
        override val secondButtonVisibility: Int = VISIBLE,
        override var formattedValue: String? = ""
    ) : DialogData()

    @Parcelize
    data class ViewPlanDialog(
        @StringRes override val title: Int = R.string.account_in_recovery_label,
        @StringRes override val desc: Int = R.string.view_treatment_plan_description,
        @StringRes override var firstButtonTitle: Int = R.string.view_payment_plan_button_label,
        @StringRes override val secondButtonTitle: Int = R.string.make_payment_now_button_label,
        override var secondButtonVisibility: Int = VISIBLE,
        override var formattedValue: String?="",
        ) : DialogData()

    @Parcelize
    data class AccountInArrDialog(
        @StringRes override val title: Int = R.string.payment_overdue_label,
        @StringRes override val desc: Int = R.string.payment_overdue_error_desc,
        @StringRes override val firstButtonTitle: Int = R.string.pay_now_button_label,
        @StringRes override val secondButtonTitle: Int = R.string.chat_to_us_label,
        override val secondButtonVisibility: Int = VISIBLE,
        override var formattedValue: String?=""
    ) : DialogData()

    @Parcelize
    data class ChargedOff(
        @StringRes override val title: Int = R.string.remove_block_on_collection_dialog_title,
        @StringRes override val desc: Int = R.string.remove_block_on_collection_dialog_desc,
        @StringRes override var firstButtonTitle: Int = R.string.make_payment_now_button_label,
        @StringRes override val secondButtonTitle: Int = R.string.chat_to_us_label,
        override var secondButtonVisibility: Int = VISIBLE,
        override var formattedValue: String?=""
    ) : DialogData()
}

enum class DialogType(val type: String) {
    ELITE_PLAN("Elite Plan"),
    VIP_PLAN("VIP Plan")
}
