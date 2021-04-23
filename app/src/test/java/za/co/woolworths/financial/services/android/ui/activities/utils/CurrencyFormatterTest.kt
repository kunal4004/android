package za.co.woolworths.financial.services.android.ui.activities.utils

import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertTrue
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

        val amountMap = mapOf<Any, String>(Pair(".1","0.10"),Pair("0.01","0.01"),Pair("1.10","1.10"),Pair("51.1","51.10"),Pair("-R.1", "-0.10"),Pair("-R.0", "0.00"), Pair("-12000", "-120.00"), Pair("-30000", "-300.00"), Pair("200", "2.00"),
                Pair("51.1","51.10"),Pair("221", "2.21"), Pair("101", "1.01"), Pair("4022", "40.22"), Pair("-120332", "-1 203.32"), Pair("5003", "50.03"), Pair("2102", "21.02"), Pair("30", "0.30"))

        conversionResult(amountMap, ::formatAmountFunc)
    }

    @Test
    fun check_if_amount_is_formatted_using_double_variable() {

        val amountMap = mapOf<Any, String>(Pair(130000.0, "130 000.00"), Pair(1200.0, "1 200.00"), Pair(300.00, "300.00"), Pair(20.0, "20.00"),
                Pair("51.1","51.10"),Pair(22.1, "22.10"), Pair(10.1, "10.10"), Pair(40.22, "40.22"), Pair(-1203.32, "-1 203.32"), Pair(50.03, "50.03"), Pair(21.02, "21.02"))

        conversionResult(amountMap, ::formatAmountFunc)
    }

    @Test
    fun check_if_amount_is_formatted_using_Int_variable() {

        val amountMap = mapOf<Any, String>(Pair(130000, "1 300.00"), Pair(12000, "120.00"), Pair(200, "2.00"), Pair(221, "2.21"),
                Pair("51.1","51.10"),Pair(101, "1.01"), Pair(4022, "40.22"), Pair(-120332, "-1 203.32"), Pair(5003, "50.03"), Pair(2102, "21.02"), Pair(30, "0.30"))

        conversionResult(amountMap, ::formatAmountFunc)
    }

    @Test
    fun verify_space_between_currency_symbol_and_amount_value() {

        val amountMap = mapOf<Any, String>(Pair("-111", "-R 1.11"),Pair("-1", "-R 0.01"), Pair("1", "R 0.01"), Pair("2", "R 0.02"), Pair("-2002", "-R 20.02"), Pair("-101", "-R 1.01"), Pair("12000", "R 120.00"), Pair("30000", "R 300.00"), Pair("200", "R 2.00"),
                Pair("221", "R 2.21"), Pair("101", "R 1.01"), Pair("4022", "R 40.22"), Pair("-120332", "-R 1 203.32"), Pair("5003", "R 50.03"), Pair("2102", "R 21.02"), Pair("30", "R 0.30"))

        conversionResult(amountMap, ::formatAmountWithSpacingFunc)
    }

    @Test
    fun verify_no_space_between_currency_symbol_and_amount_value() {
        val amountMap = mapOf<Any, String>(Pair("13000", "R130.00"), Pair("12000", "R120.00"), Pair("30000", "R300.00"), Pair("200", "R2.00"),
                Pair("221", "R2.21"), Pair("101", "R1.01"), Pair("4022", "R40.22"), Pair("-120332", "R-1 203.32"), Pair("5003", "R50.03"),
                Pair("2102", "R21.02"), Pair("30", "R0.30"))

        conversionResult(amountMap, ::formatAmountWithoutSpacingFunc)
    }


    private fun conversionResult(amount: Map<Any, String>, func: (amount: Any) -> Any) {
        amount.entries.forEach { entry ->
            val formattedAmount = func(entry.key)
            val expectedAmount = entry.value
            assertTrue(formattedAmount == expectedAmount)
        }
    }

    private fun formatAmountFunc(amount: Any) = currencyFormatter.formatAmountsToRandAndCent(amount)

    private fun formatAmountWithSpacingFunc(amount: Any) = currencyFormatter.formatAmountToRandAndCentWithSpacing(amount)

    private fun formatAmountWithoutSpacingFunc(amount: Any) = currencyFormatter.formatAmountToRandAndCentWithoutSpace(amount)

}