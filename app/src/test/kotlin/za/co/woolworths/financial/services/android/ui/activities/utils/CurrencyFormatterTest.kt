package za.co.woolworths.financial.services.android.ui.activities.utils

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import za.co.woolworths.financial.services.android.util.CurrencyFormatter

@RunWith(MockitoJUnitRunner::class)
class CurrencyFormatterTest {

    @Mock
    lateinit var currencyFormatter: CurrencyFormatter

    @Before
    fun before() {
        currencyFormatter = CurrencyFormatter()
    }

    @Test
    fun check_if_amount_is_formatted_using_string_variable_without_dot() {

        val rawAmount = mutableListOf("130000", "12000", "30000", "200", "221", "101", "4022", "-120332", "5003", "2102", "30")
        val expectedResult = mutableListOf("1 300.00", "120.00", "300.00", "2.00", "2.21", "1.01", "40.22", "-1 203.32", "50.03", "21.02", "0.30")

        conversionResult(rawAmount, expectedResult, ::formatAmountFunc)
    }

    @Test
    fun check_if_amount_is_formatted_using_double_variable() {

        val rawAmount = mutableListOf(130000.0, 1200.0, 300.00, 20.0, 22.1, 10.1, 40.22, -1203.32, 50.03, 21.02)
        val expectedResult = mutableListOf("130 000.00", "1 200.00", "300.00", "20.00", "22.10", "10.10", "40.22", "-1 203.32", "50.03", "21.02")

        conversionResult(rawAmount, expectedResult, ::formatAmountFunc)
    }

    @Test
    fun check_if_amount_is_formatted_using_Int_variable() {

        val rawAmount = mutableListOf(130000, 12000, 30000, 200, 221, 101, 4022, -120332, 5003, 2102, 30)
        val expectedResult = mutableListOf("1 300.00", "120.00", "300.00", "2.00", "2.21", "1.01", "40.22", "-1 203.32", "50.03", "21.02", "0.30")

        conversionResult(rawAmount, expectedResult, ::formatAmountFunc)
    }

    @Test
    fun check_if_formatted_amount_escape_decimal_format() {

        val rawAmount = mutableListOf("130000", "12000", "30000", "200", "221", "101", "4022", "-120332", "5003", "2102", "30")
        val expectedResult = mutableListOf("1 300", "120", "300", "2", "2.21", "1.01", "40.22", "-1 203.32", "50.03", "21.02", "0.30")

        conversionResult(rawAmount, expectedResult, ::formatAmountToRandEscapingDecimalFormat)

    }

    @Test
    fun verify_space_between_currency_symbol_and_amount_value() {

        val rawAmount = mutableListOf("130000", "12000", "30000", "200", "221", "101", "4022", "-120332", "5003", "2102", "30")
        val expectedResult = mutableListOf("R 1 300.00", "R 120.00", "R 300.00", "R 2.00", "R 2.21", "R 1.01", "R 40.22", "R -1 203.32", "R 50.03", "R 21.02", "R 0.30")

        conversionResult(rawAmount, expectedResult, ::formatAmountWithSpacingFunc)
    }

    @Test
    fun verify_no_space_between_currency_symbol_and_amount_value() {

        val rawAmount = mutableListOf("130000", "12000", "30000", "200", "221", "101", "4022", "-120332", "5003", "2102", "30")
        val expectedResult = mutableListOf("R1 300.00", "R120.00", "R300.00", "R2.00", "R2.21", "R1.01", "R40.22", "R-1 203.32", "R50.03", "R21.02", "R0.30")

        conversionResult(rawAmount, expectedResult, ::formatAmountWithoutSpacingFunc)
    }

    private fun conversionResult(rawAmount: Any, expectedResult: MutableList<String>, func: (amount: Any) -> Any) {
        (rawAmount as? MutableList<*>)?.forEachIndexed { index, unformattedAmount ->
            val formattedAmount = unformattedAmount?.let { func(it) }
            val expectedAmount = expectedResult[index]
            Assert.assertEquals(formattedAmount, expectedAmount)
        }
    }

    private fun formatAmountFunc(amount: Any) = currencyFormatter.formatAmountsToRandAndCent(amount)

    private fun formatAmountWithSpacingFunc(amount: Any) = currencyFormatter.formatAmountToRandAndCentWithSpacing(amount)

    private fun formatAmountWithoutSpacingFunc(amount: Any) = currencyFormatter.formatAmountToRandAndCentWithoutSpace(amount)

    private fun formatAmountToRandEscapingDecimalFormat(amount: Any) = currencyFormatter.formatAmountEscapeDecimal(amount)

}