package za.co.woolworths.financial.services.android.ui.fragments.integration.viewmodel

import za.co.woolworths.financial.services.android.ui.fragments.integration.service.cekd.IAbsaContentEncryptionKeyId
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.common.ISessionKeyGenerator
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.create_alias.ICreateAlias
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.get_all_balances.IAbsaBalanceEnquiryFacadeGetAllBalance
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.get_archive_statements.IAbsaGetArchivedStatementList
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.get_individual_statements.IAbsaGetIndividualStatement
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.login.IAbsaLogin
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.register_credential.IAbsaRegisterCredentials
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.validate_card_and_pin.IValidateCardAndPin
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.validate_sure_checks.IValidateSureCheck

class AbsaRegisterCardAndPinDelegateImpl(
    private val sessionKeyGenerator: ISessionKeyGenerator,
    private val cekd: IAbsaContentEncryptionKeyId,
    private val validateCardAndPin: IValidateCardAndPin,
    private val validateSureChecks: IValidateSureCheck,
    private val createAlias: ICreateAlias) :
    ISessionKeyGenerator by sessionKeyGenerator,
    IAbsaContentEncryptionKeyId by cekd,
    IValidateCardAndPin by validateCardAndPin,
    IValidateSureCheck by validateSureChecks,
    ICreateAlias by createAlias

class AbsaRegisterCredentialDelegateImpl(
    private val registerCredential: IAbsaRegisterCredentials) :
    IAbsaRegisterCredentials by registerCredential

class AbsaLoginDelegateImpl(
    private val cekd: IAbsaContentEncryptionKeyId,
    private val login: IAbsaLogin
) : IAbsaContentEncryptionKeyId by cekd,
    IAbsaLogin by login

class AbsaShowStatementDelegateImpl(
    private val allBalances: IAbsaBalanceEnquiryFacadeGetAllBalance,
    private val archivedStatement: IAbsaGetArchivedStatementList,
    private val individualStatement: IAbsaGetIndividualStatement) :
    IAbsaBalanceEnquiryFacadeGetAllBalance by allBalances,
    IAbsaGetArchivedStatementList by archivedStatement,
    IAbsaGetIndividualStatement by individualStatement