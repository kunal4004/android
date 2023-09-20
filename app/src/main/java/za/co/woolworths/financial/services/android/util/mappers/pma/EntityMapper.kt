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

/**
 * Solely for mapping objects from entity to domain
 *
 * @author Adebayo Oloyede
 *
 * @since 9.11.0 (13/07/2023)
 *
 * */
object EntityMapper {

    /**
     * Maps the config object [PMAPayByDebitOrderEntity] fetched from cache to the domain type which is [PMAPayByDebitOrderDomain]
     *
     * @author Adebayo Oloyede
     * @receiver [PMAPayByDebitOrderEntity] which is the type gotten from remote saver and saved into cache.
     * @return [PMAPayByDebitOrderDomain] which is the type used in the UI controller, PMAPayByDebitOrderFragment
     *
     * @since 9.11.0 (13/07/2023)
     * */
    fun PMAPayByDebitOrderEntity.toDomain(): PMAPayByDebitOrderDomain {
        return with(::PMAPayByDebitOrderDomain) {
            val header = this@toDomain.debitOrder.first().let { pmaPBDOEntityItem ->
                val headerContent = pmaPBDOEntityItem.content
                val title = pmaPBDOEntityItem.title!!
                PayByDebitOrderFragmentHeader(headerContent, title)
            }
            val body = this@toDomain.debitOrder[1].let { pmaPBDOEntityItem ->
                val content = pmaPBDOEntityItem.content!!.map {
                    PBDFragmentBodyContent(
                        it.text!!,
                    )
                }
                val footer = PBDFragmentFooter(
                    pmaPBDOEntityItem.footer!!.text,
                )
                val subtitle = pmaPBDOEntityItem.subtitle!!
                PayByDebitOrderFragmentBody(
                    content,
                    footer,
                    subtitle,
                )
            }
            PMAPayByDebitOrderDomain(
                header,
                body,
            )
        }
    }

    /**
     * Maps the object [PMAPayByDebitOrderDomain] to [PMAPayByDebitOrderEntity] which is the type fetched from server during config download
     *
     * @author Adebayo Oloyede
     * @receiver [PMAPayByDebitOrderDomain] which is the type used in the UI controller, PMAPayByDebitOrderFragment
     * @return [PMAPayByDebitOrderEntity] which is the type gotten from remote saver and saved into cache.
     *
     * @since 9.11.0 (18/07/2023)
     * */
    fun PMAPayByDebitOrderDomain.toEntity(): PMAPayByDebitOrderEntity =
        with(::PMAPayByDebitOrderEntity) {
            val debitOrderItem1 = this@toEntity.header.let { debitOrderHeader ->
                val title = debitOrderHeader.title
                val content = debitOrderHeader.headerContent
                PMAPayByDebitOrderEntityItem(
                    title = title,
                    content = content,
                    subtitle = null,
                    footer = null,
                )
            }

            val debitOrderItem2 = this@toEntity.body.let { debitOrderBody ->
                val subtitle = debitOrderBody.subtitle
                val content = debitOrderBody.content.map {
                    PBDFragmentHeaderContent(
                        email = null,
                        phone = null,
                        text = it.text,
                    )
                }
                val footer = Footer(debitOrderBody.footer.text)
                PMAPayByDebitOrderEntityItem(
                    subtitle = subtitle,
                    content = content,
                    footer = footer,
                    title = null,
                )
            }
            PMAPayByDebitOrderEntity(listOf(debitOrderItem1, debitOrderItem2))
        }
}
