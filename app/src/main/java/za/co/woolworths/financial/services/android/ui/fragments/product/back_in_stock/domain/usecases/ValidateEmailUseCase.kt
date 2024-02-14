package za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.domain.usecases

import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.core.functions.isEmailValid
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.models.ValidationResult

class ValidateEmailUseCase : BaseUseCase<String, ValidationResult> {
    companion object {
        const val EMAIL_CANNOT_BLANK = "email can not be blank"
        const val INVALID_EMAIL = "invalid email"
    }

    override fun execute(input: String): ValidationResult {
        if (input.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = EMAIL_CANNOT_BLANK
            )
        }
        if (!isEmailValid(input)) {
            return ValidationResult(
                successful = false,
                errorMessage = INVALID_EMAIL
            )
        }
        return ValidationResult(
            successful = true,
            errorMessage = null
        )
    }
}