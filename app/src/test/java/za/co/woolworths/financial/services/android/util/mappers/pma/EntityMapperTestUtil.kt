package za.co.woolworths.financial.services.android.util.mappers.pma

import za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.Footer
import za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.PMAPayByDebitOrderDomain
import za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.PMAPayByDebitOrderEntity
import za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.PMAPayByDebitOrderEntityItem
import za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.bodyContents.PBDFragmentBodyContent
import za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.bodyContents.PBDFragmentFooter
import za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.bodyContents.PayByDebitOrderFragmentBody
import za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.headerContents.PBDFragmentHeaderContent
import za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.headerContents.PayByDebitOrderFragmentHeader

object EntityMapperTestUtil {

    private val headerContentTitle = PBDFragmentHeaderContent(
        text = "Debit order is the most convenient way to pay your Woolies account.",
        phone = null,
        email = null,
    )
    private val headerContentBody = PBDFragmentHeaderContent(
        text = "To set up a debit order, call us on 0861 50 20 20 or e-mail queries@wfs.co.za with the subject line: Debit order",
        phone = "0861 50 20 20",
        email = "queries@wfs.co.za",
    )

    private val headerContent: List<PBDFragmentHeaderContent> = listOf(
        headerContentTitle,
        headerContentBody,
    )

    // Create the Header
    private val debitOrderHeader = PMAPayByDebitOrderEntityItem(
        title = "Pay By Debit Order",
        content = headerContent,
        footer = null,
        subtitle = null,
    )

    private val bodyContent: List<PBDFragmentHeaderContent> = listOf(
        PBDFragmentHeaderContent(text = "Bank name: ", phone = null, email = null),
        PBDFragmentHeaderContent(text = "Branch name and code: ", phone = null, email = null),
        PBDFragmentHeaderContent(text = "Bank account number: ", phone = null, email = null),
        PBDFragmentHeaderContent(text = "Account type: ", phone = null, email = null),
        PBDFragmentHeaderContent(
            text = "Debit order amount: (this can be either full payment, minimum payment, fixed amount or fixed percentage)",
            phone = null,
            email = null,
        ),
        PBDFragmentHeaderContent(
            text = "Debit order deduction date: ",
            phone = null,
            email = null,
        ),
    )
    private val footer =
        Footer("Note: If you are setting up a debit order for a Woolies Personal Loan, remember to also attach a clear copy of your valid SA Identity Document (if its an ID card include both sides).")

    private val debitOrderBody = PMAPayByDebitOrderEntityItem(
        subtitle = "Please also send us the following: ",
        content = bodyContent,
        footer = footer,
        title = null,
    )

    fun getSampleConfigPMAPayByDebitOrderEntity(): PMAPayByDebitOrderEntity {
        return PMAPayByDebitOrderEntity(
            debitOrder = listOf(debitOrderHeader, debitOrderBody),
        )
    }

    fun getPMAPayByDebitOrderDomain(): PMAPayByDebitOrderDomain {
        val header = PayByDebitOrderFragmentHeader(
            headerContent = headerContent,
            title = "Pay By Debit Order",
        )
        val body = PayByDebitOrderFragmentBody(
            content = bodyContent.map { PBDFragmentBodyContent(it.text!!) },
            footer = PBDFragmentFooter(footer.text),
            subtitle = "Please also send us the following: ",
        )
        return PMAPayByDebitOrderDomain(header, body)
    }
}
