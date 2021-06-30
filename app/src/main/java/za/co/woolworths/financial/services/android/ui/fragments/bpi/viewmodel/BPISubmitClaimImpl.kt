package za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.bpi.SubmitClaimReason
import za.co.woolworths.financial.services.android.ui.fragments.bpi.contract.BPISubmitClaimInterface

class BPISubmitClaimImpl : BPISubmitClaimInterface {
    override fun submitClaimList(): MutableList<SubmitClaimReason> {
        return mutableListOf(
            SubmitClaimReason(
                title = R.string.death_cover_title,
                requiredForm = listOf(
                    R.string.death_cover_required_form_1,
                    R.string.death_cover_required_form_2,
                    R.string.death_cover_required_form_3
                ),
                requiredSubmit = listOf(
                    R.string.death_cover_required_submit_1,
                    R.string.death_cover_required_submit_2,
                    R.string.death_cover_required_submit_3,
                    R.string.death_cover_required_submit_4
                ),
                description = R.string.death_cover_desc
            ),

            SubmitClaimReason(
                title = R.string.total_loss_of_income_title,
                requiredForm = listOf(
                    R.string.total_loss_of_income_form_1,
                    R.string.total_loss_of_income_form_2
                ),
                requiredSubmit = listOf(
                    R.string.total_loss_of_income_required_submit_1,
                    R.string.total_loss_of_income_required_submit_2,
                    R.string.total_loss_of_income_required_submit_3
                ),
                description = R.string.total_loss_of_income_desc
            ),

            SubmitClaimReason(
                title = R.string.temporary_disability_title,
                requiredForm = listOf(
                    R.string.temporary_disability_form_1,
                    R.string.temporary_disability_form_2,
                    R.string.temporary_disability_form_3,
                    R.string.temporary_disability_form_4,
                    R.string.temporary_disability_form_5),
                requiredSubmit = listOf(
                    R.string.temporary_disability_required_submit_1,
                    R.string.temporary_disability_required_submit_2,
                    R.string.temporary_disability_required_submit_3,
                    R.string.temporary_disability_required_submit_4
                ),
                description = R.string.temporary_disability_desc
            ),

            SubmitClaimReason(
                title = R.string.permanent_disability_title,
                requiredForm = listOf(
                    R.string.permanent_disability_form_1,
                    R.string.permanent_disability_form_2,
                    R.string.permanent_disability_form_3,
                    R.string.permanent_disability_form_4,
                    R.string.permanent_disability_form_5
                    ),
                requiredSubmit = listOf(
                    R.string.permanent_disability_required_submit_1,
                    R.string.permanent_disability_required_submit_2,
                    R.string.permanent_disability_required_submit_3,
                    R.string.permanent_disability_required_submit_4,
                ),
                description = R.string.permanent_disability_desc
            ),
            SubmitClaimReason(
                title = R.string.critical_illness_title,
                requiredForm = listOf(
                    R.string.critical_illness_form_1,
                    R.string.critical_illness_form_2,
                    R.string.critical_illness_form_3,
                    R.string.critical_illness_form_4
                ),
                requiredSubmit = listOf(
                    R.string.critical_illness_required_submit_1,
                    R.string.critical_illness_required_submit_2,
                    R.string.critical_illness_required_submit_3
                ),
                description = R.string.critical_illness_desc
            )
        )
    }
}