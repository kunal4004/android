package za.co.woolworths.financial.services.android.ui.fragments.integration.service.get_archive_statements

import za.co.absa.openbankingapi.woolworths.integration.dto.ArchivedStatement
import za.co.absa.openbankingapi.woolworths.integration.dto.Header
import za.co.woolworths.financial.services.android.util.Utils
import java.util.*

data class StatementListRequest(val header: Header?, val  accountNumber: String, val fromDate: String = Utils.getDate(6),val toDate: String = Utils.getDate(0))

data class ArchivedStatementListResponseProperty (val header: Header?,val archivedStatementList: ArrayList<ArchivedStatement>?)