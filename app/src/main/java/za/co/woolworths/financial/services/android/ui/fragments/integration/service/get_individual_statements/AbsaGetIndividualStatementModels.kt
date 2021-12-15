package za.co.woolworths.financial.services.android.ui.fragments.integration.service.get_individual_statements

import za.co.absa.openbankingapi.woolworths.integration.dto.ArchivedStatement
import za.co.absa.openbankingapi.woolworths.integration.dto.Header
import java.lang.StringBuilder
import java.util.ArrayList

data class IndividualStatementRequestProperty( val archivedStatement: ArchivedStatement) {
    val urlEncodedFormData: String
        get() {
            val sb = StringBuilder()
                .append("documentName=").append(archivedStatement.documentName)
                .append("&documentKey=").append(archivedStatement.documentKey)
                .append("&extendedDocumentKey=").append(archivedStatement.extendedDocumentKey)
                .append("&documentExtension=").append(archivedStatement.documentExtension)
                .append("&documentWorkingDate=").append(archivedStatement.documentWorkingDate)
                .append("&documentSize=").append(archivedStatement.documentSize)
                .append("&documentNumberPages=").append(archivedStatement.documentNumberPages)
                .append("&documentType=").append(archivedStatement.documentType)
                .append("&folderName=").append(archivedStatement.folderName)
                .append("&foldersEnvSuffix=").append(archivedStatement.foldersEnvSuffix)
            return sb.toString()
        }
}


data class IndividualStatementResponseProperty (val header: Header?, var archivedStatementList: ArrayList<ArchivedStatement>?)