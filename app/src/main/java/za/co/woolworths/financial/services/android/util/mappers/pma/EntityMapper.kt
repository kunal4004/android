package za.co.woolworths.financial.services.android.util.mappers.pma

import za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.PMAPayByDebitOrderDomain
import za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.PMAPayByDebitOrderEntity
import za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.PMAPayByDebitOrderEntityItem
import za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.bodyContents.PBDFragmentBodyContent
import za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.bodyContents.PBDFragmentFooter
import za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.bodyContents.PayByDebitOrderFragmentBody
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
    fun PMAPayByDebitOrderEntity.toDomain(): PMAPayByDebitOrderDomain =
        with(::PMAPayByDebitOrderDomain) {
            val header = this@toDomain.debitOrder.first().let { pmaPBDOEntityItem ->
                with(::PayByDebitOrderFragmentHeader) {
                    callBy(
                        parameters.associateWith { parameter ->
                            when (parameter.name) {
                                PayByDebitOrderFragmentHeader::headerContent.name -> pmaPBDOEntityItem.content
                                PayByDebitOrderFragmentHeader::title.name -> pmaPBDOEntityItem.title!!
                                else -> {}
                            }
                        },
                    )
                }
            }
            val body = this@toDomain.debitOrder[1].let { pmaPBDOEntityItem ->
                with(::PayByDebitOrderFragmentBody) {
                    callBy(
                        parameters.associateWith { parameter ->
                            when (parameter.name) {
                                PayByDebitOrderFragmentBody::content.name -> pmaPBDOEntityItem.content.map {
                                    PBDFragmentBodyContent(
                                        it.text,
                                    )
                                }

                                PayByDebitOrderFragmentBody::footer.name -> PBDFragmentFooter(
                                    pmaPBDOEntityItem.footer!!.text,
                                )
                                PayByDebitOrderFragmentBody::subtitle.name -> pmaPBDOEntityItem.subtitle!!
                                else -> {}
                            }
                        },
                    )
                }
            }
            PMAPayByDebitOrderDomain(
                header,
                body,
            )
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
                with(::PMAPayByDebitOrderEntityItem) {
                    callBy(
                        parameters.associateWith { parameter ->
                            when (parameter.name) {
                                PMAPayByDebitOrderEntityItem::title.name -> debitOrderHeader.title
                                PMAPayByDebitOrderEntityItem::content.name -> debitOrderHeader.headerContent
                                else -> {}
                            }
                        },
                    )
                }
            }

            val debitOrderItem2 = this@toEntity.body.let { debitOrderBody ->
                with(::PMAPayByDebitOrderEntityItem) {
                    callBy(
                        parameters.associateWith { parameter ->
                            when (parameter.name) {
                                PMAPayByDebitOrderEntityItem::subtitle.name -> debitOrderBody.subtitle
                                PMAPayByDebitOrderEntityItem::content.name -> debitOrderBody.content
                                PMAPayByDebitOrderEntityItem::footer.name -> debitOrderBody.footer
                                else -> {}
                            }
                        },
                    )
                }
            }
            PMAPayByDebitOrderEntity(listOf(debitOrderItem1, debitOrderItem2))
        }
}
