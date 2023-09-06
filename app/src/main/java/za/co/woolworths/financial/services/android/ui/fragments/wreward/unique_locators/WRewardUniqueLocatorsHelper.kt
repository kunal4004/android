package za.co.woolworths.financial.services.android.ui.fragments.wreward.unique_locators

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.tabs.TabLayout

class WRewardUniqueLocatorsHelper {
    companion object {
        fun setTabBarIDs(view: TabLayout.Tab, index: Int) {
            when (index) {
                0 -> view.contentDescription = WRewardsUniqueLocators.OVERVIEW_TITLE_TEXT.value
                1 -> view.contentDescription = WRewardsUniqueLocators.VOUCHERS_TITLE_TEXT.value
                2 -> view.contentDescription = WRewardsUniqueLocators.SAVINGS_TITLE_TEXT.value
            }
        }

        fun setLogOutFragLocators(
            pageIndex: Int,
            image: ImageView,
            title: TextView,
            desc: TextView
        ) {
            var imageID = ""
            var titleID = ""
            var descID = ""

            when (pageIndex) {
                0 -> {
                    imageID = WRewardsUniqueLocators.WREWARDS_CARD_IMAGE.value
                    titleID = WRewardsUniqueLocators.ALL_THE_MORE_REASON_TO_JOIN_TITLE_TEXT.value
                    descID = WRewardsUniqueLocators.ALL_THE_MORE_REASON_TO_JOIN_DESC.value
                }

                1 -> {
                    imageID = "${WRewardsUniqueLocators.HEADER_IMAGE.value}1"
                    titleID =
                        WRewardsUniqueLocators.SAVINGS_ON_WOOLIES_FAVOURITES_EVERYDAY_TITLE_TEXT.value
                    descID =
                        WRewardsUniqueLocators.SAVINGS_ON_WOOLIES_FAVOURITES_EVERYDAY_DESC.value
                }

                2 -> {
                    imageID = "${WRewardsUniqueLocators.HEADER_IMAGE.value}2"
                    titleID = WRewardsUniqueLocators.EXTRA_SAVING_PROMOS_EVENTS_TITLE_TEXT.value
                    descID = WRewardsUniqueLocators.EXTRA_SAVING_PROMOS_EVENTS_DESC.value
                }

                3 -> {
                    imageID = "${WRewardsUniqueLocators.HEADER_IMAGE.value}3"
                    titleID =
                        WRewardsUniqueLocators.EXCLUSIVE_VOUCHERS_JUST_FOR_YOU_TITLE_TEXT.value
                    descID =
                        WRewardsUniqueLocators.EXCLUSIVE_VOUCHERS_JUST_FOR_YOU_PROMOS_EVENTS_DESC.value
                }

                else -> {
                    // Handle the default case if needed
                }
            }

            image.contentDescription = imageID
            title.contentDescription = titleID
            desc.contentDescription = descID
        }
        fun setRewardsSignedOutMainIDs(joinButton: Button,signInButton: TextView,orLabel: TextView,registerButton: TextView) {
            joinButton.contentDescription = WRewardsUniqueLocators.JOIN_WREWARDS_BUTTON.value
            signInButton.contentDescription = WRewardsUniqueLocators.SIGN_IN_BUTTON.value
            orLabel.contentDescription = WRewardsUniqueLocators.OR_TEXT.value
            registerButton.contentDescription = WRewardsUniqueLocators.REGISTER_BUTTON.value
        }
        fun setIndicatorsLocators(tab: TabLayout.Tab?,index: Int) {
            tab?.contentDescription = WRewardsUniqueLocators.DOT_INDICATOR.value + index
        }
        fun setOverViewFragLocators(vararg views: View?) {
            val locators = arrayOf(
                WRewardsUniqueLocators.VIRTUAL_CARD_NUMBER_TEXT.value,
                WRewardsUniqueLocators.VIRTUAL_CARD_NUMBER_MORE_INFO_BUTTON.value,
                WRewardsUniqueLocators.WREWARDS_BENEFITS_TEXT.value,
                WRewardsUniqueLocators.WREWARDS_BENEFITS_MORE_INFO_BUTTON.value,
                WRewardsUniqueLocators.SAVINGS_TEXT.value,
                WRewardsUniqueLocators.SAVINGS_AMOUNT.value,
                WRewardsUniqueLocators.TO_GET_TO_VIP_TEXT.value,
                WRewardsUniqueLocators.TO_GET_TO_VIP_AMOUNT.value
            )
            for (index in views.indices) {
                views[index]?.apply {
                    contentDescription = locators[index]
                }
            }
        }

        fun setBenefitsFragLocators(vararg views: View?) {
            val locators = arrayOf(
                WRewardsUniqueLocators.THE_BEST_SAVINGS_AT_WOOLIES_EVERYDAY_TITLE_TEXT.value,
                WRewardsUniqueLocators.SAVING_ON_WOOLIES_FAVOURITES_TITLE_TEXT.value,
                WRewardsUniqueLocators.SAVING_ON_WOOLIES_FAVOURITES_DESC.value,
                WRewardsUniqueLocators.EXTRA_SAVING_PROMOS_EVENTS_TITLE_TEXT.value,
                WRewardsUniqueLocators.EXTRA_SAVING_PROMOS_EVENTS_DESC.value,
                WRewardsUniqueLocators.EXCLUSIVE_VOUCHERS_JUST_FOR_YOU_TITLE_TEXT.value,
                WRewardsUniqueLocators.EXCLUSIVE_VOUCHERS_JUST_FOR_YOU_PROMOS_EVENTS_DESC.value,
                WRewardsUniqueLocators.WREWARD_TERMS_AND_CONDITIONS_APPLY.value
            )
            for (index in views.indices) {
                views[index]?.apply {
                    contentDescription = locators[index]
                }
            }
        }

        fun setVipExclusiveFragLocators(vararg views: View?) {
            val locators = arrayOf(
                WRewardsUniqueLocators.ALL_THE_MORE_REASON_TO_JOIN_TITLE_TEXT.value,
                WRewardsUniqueLocators.W_VIP_IMAGE_ICON.value,
                WRewardsUniqueLocators.ALL_THE_MORE_REASON_TO_JOIN_DESC.value,
                WRewardsUniqueLocators.ALL_THE_MORE_REASON_TO_JOIN_IMAGE_ICON_1.value,
                WRewardsUniqueLocators.ALL_THE_MORE_REASON_TO_JOIN_DESC_1.value,
                WRewardsUniqueLocators.ALL_THE_MORE_REASON_TO_JOIN_IMAGE_ICON_2.value,
                WRewardsUniqueLocators.ALL_THE_MORE_REASON_TO_JOIN_DESC_2.value,
                WRewardsUniqueLocators.ALL_THE_MORE_REASON_TO_JOIN_IMAGE_ICON_3.value,
                WRewardsUniqueLocators.ALL_THE_MORE_REASON_TO_JOIN_DESC_3.value,
                WRewardsUniqueLocators.TO_GET_VIP_STATUS_SPEND_R30000_OR_MORE_ANUALLY_TEXT.value,
                WRewardsUniqueLocators.CALCULATION_OF_STATUS_LEVEL_TITLE_TEXT.value,
                WRewardsUniqueLocators.CALCULATION_OF_STATUS_LEVEL_DESC_1.value,
                WRewardsUniqueLocators.CALCULATION_OF_STATUS_LEVEL_DESC_2.value,
                WRewardsUniqueLocators.CALCULATION_OF_STATUS_LEVEL_DESC_3.value
            )
            for (index in views.indices) {
                views[index]?.apply {
                    contentDescription = locators[index]
                }
            }
        }

        fun setSavingsYearMonthsLocators(view: View, isYear: Boolean = false, position: Int = 0) {
            when (isYear) {
                true -> {
                    view.contentDescription = WRewardsUniqueLocators.YEAR_TO_DATE.value
                }

                false -> {
                    view.contentDescription = WRewardsUniqueLocators.MONTH.value + position
                }
            }
        }

        fun setSavingsFragLocators(vararg views: View?) {
            val locators = arrayOf(
                WRewardsUniqueLocators.WREWARDS_INSTANT_SAVINGS_TEXT.value,
                WRewardsUniqueLocators.WREWARDS_INSTANT_SAVINGS_AMOUNT.value,
                WRewardsUniqueLocators.SAVING_SINCE_TEXT.value,
                WRewardsUniqueLocators.SAVING_SINCE_AMOUNT.value,
                WRewardsUniqueLocators.SAVING_SINCE_INFO_ICON.value,
                WRewardsUniqueLocators.QUARTELY_VOUCHERS_EARNED_TEXT.value,
                WRewardsUniqueLocators.QUARTELY_VOUCHERS_EARNED_AMOUNT.value,
                WRewardsUniqueLocators.YEAR_TO_DATE_SPEND_TEXT.value,
                WRewardsUniqueLocators.YEAR_TO_DATE_SPEND_AMOUNT.value,
                WRewardsUniqueLocators.YEAR_TO_DATE_SPEND_INFO_ICON.value
            )
            for (index in views.indices) {
                views[index]?.apply {
                    contentDescription = locators[index]
                }
            }
        }
    }
}

enum class WRewardsUniqueLocators(val value: String) {
    //logout
    DOT_INDICATOR("dot_indicator_"),
    HEADER_IMAGE("HEADER_IMAGE_"),
    SAVINGS_ON_WOOLIES_FAVOURITES_EVERYDAY_TITLE_TEXT("savings_on_woolies_favourites_everyday_title_text"),
    SAVINGS_ON_WOOLIES_FAVOURITES_EVERYDAY_DESC("savings_on_woolies_favourites_everyday_desc"),
    JOIN_WREWARDS_BUTTON("join_wrewards_button"),
    SIGN_IN_BUTTON("sign_in_button"),
    OR_TEXT("or_text"),
    REGISTER_BUTTON("register_button"),

    WREWARDS_TOOLBAR_TEXT("wrewards_toolbar_text"),
    OVERVIEW_TITLE_TEXT("overview_title_text"),
    VOUCHERS_TITLE_TEXT("vouchers_title_text"),
    SAVINGS_TITLE_TEXT("savings_title_text"),
    WREWARDS_CARD_IMAGE("wrewards_card_image"),

    //OverViewFrag
    VIRTUAL_CARD_NUMBER_TEXT("virtual_card_number_text"),
    VIRTUAL_CARD_NUMBER_MORE_INFO_BUTTON("virtual_card_number_more_info_button"),
    WREWARDS_BENEFITS_TEXT("wrewards_benefits_text"),
    WREWARDS_BENEFITS_MORE_INFO_BUTTON("wrewards_benefits_more_info_button"),
    SAVINGS_TEXT("savings_text"),
    SAVINGS_AMOUNT("savings_amount"),
    TO_GET_TO_VIP_TEXT("to_get_to_vip_text"),
    TO_GET_TO_VIP_AMOUNT("to_get_to_vip_amount"),
    VIRTUAL_CARD_NUMBER_IMAGE_ICON("virtual_card_number_image_icon"),
    VIRTUAL_CARD_NUMBER_TITLE_TEXT("virtual_card_number_title_text"),
    VIRTUAL_CARD_NUMBER_DESC("virtual_card_number_desc"),
    GOT_IT_BUTTON("got_it_button"),
    WREWARDS_BENEFITS_IMAGE("wrewards_benefits_image"),
    CLOSE("close"),

    //BenefitsFrag
    THE_BEST_SAVINGS_AT_WOOLIES_EVERYDAY_TITLE_TEXT("the_best_savings_at_woolies_everyday_title_text"),
    SAVING_ON_WOOLIES_FAVOURITES_TITLE_TEXT("saving_on_woolies_favourites_title_text"),
    SAVING_ON_WOOLIES_FAVOURITES_DESC("saving_on_woolies_favourites_desc"),
    EXTRA_SAVING_PROMOS_EVENTS_TITLE_TEXT("extra_saving_promos_Events_title_text"),
    EXTRA_SAVING_PROMOS_EVENTS_DESC("extra_saving_promos_Events_desc"),
    EXCLUSIVE_VOUCHERS_JUST_FOR_YOU_TITLE_TEXT("exclusive_vouchers_just_for_you_title_text"),
    EXCLUSIVE_VOUCHERS_JUST_FOR_YOU_PROMOS_EVENTS_DESC("exclusive_vouchers_just_for_you_promos_Events_desc"),
    WREWARD_TERMS_AND_CONDITIONS_APPLY("wreward_terms_and_conditions_apply"),

    //VipExclusiveFrag
    ALL_THE_MORE_REASON_TO_JOIN_TITLE_TEXT("all_the_more_reason_to_join_title_text"),
    W_VIP_IMAGE_ICON("w_vip_image_icon"),
    ALL_THE_MORE_REASON_TO_JOIN_DESC("all_the_more_reason_to_join_desc"),
    ALL_THE_MORE_REASON_TO_JOIN_IMAGE_ICON_1("all_the_more_reason_to_join_image_icon_1"),
    ALL_THE_MORE_REASON_TO_JOIN_DESC_1("all_the_more_reason_to_join_desc_1"),
    ALL_THE_MORE_REASON_TO_JOIN_IMAGE_ICON_2("all_the_more_reason_to_join_image_icon_2"),
    ALL_THE_MORE_REASON_TO_JOIN_DESC_2("all_the_more_reason_to_join_desc_2"),
    ALL_THE_MORE_REASON_TO_JOIN_IMAGE_ICON_3("all_the_more_reason_to_join_image_icon_3"),
    ALL_THE_MORE_REASON_TO_JOIN_DESC_3("all_the_more_reason_to_join_desc_3"),
    TO_GET_VIP_STATUS_SPEND_R30000_OR_MORE_ANUALLY_TEXT("to_get_vip_status_spend_R30000_or_more_anually_text"),
    CALCULATION_OF_STATUS_LEVEL_TITLE_TEXT("calculation_of_status_level_title_text"),
    CALCULATION_OF_STATUS_LEVEL_DESC_1("calculation_of_status_level_desc_1"),
    CALCULATION_OF_STATUS_LEVEL_DESC_2("calculation_of_status_level_desc_2"),
    CALCULATION_OF_STATUS_LEVEL_DESC_3("calculation_of_status_level_desc_3"),

    //SavingsFrag
    YEAR_TO_DATE("year_to_date"),
    MONTH("month_"),
    WREWARDS_INSTANT_SAVINGS_TEXT("wrewards_instant_savings_text"),
    WREWARDS_INSTANT_SAVINGS_AMOUNT("wrewards_instant_savings_amount"),
    SAVING_SINCE_TEXT("saving_since_text"),
    SAVING_SINCE_AMOUNT("saving_since_amount"),
    SAVING_SINCE_INFO_ICON("saving_since_info_icon"),
    QUARTELY_VOUCHERS_EARNED_TEXT("quartely_vouchers_earned_text"),
    QUARTELY_VOUCHERS_EARNED_AMOUNT("quartely_vouchers_earned_amount"),
    YEAR_TO_DATE_SPEND_TEXT("year_to_date_spend_text"),
    YEAR_TO_DATE_SPEND_AMOUNT("year_to_date_spend_amount"),
    YEAR_TO_DATE_SPEND_INFO_ICON("year_to_date_spend_info_icon")

}