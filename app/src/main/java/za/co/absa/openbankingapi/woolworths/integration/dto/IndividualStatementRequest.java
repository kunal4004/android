package za.co.absa.openbankingapi.woolworths.integration.dto;

import java.io.UnsupportedEncodingException;

public class IndividualStatementRequest {

    private ArchivedStatement archivedStatement;

    public IndividualStatementRequest(ArchivedStatement archivedStatement) {
        this.archivedStatement = archivedStatement;
    }


    public final String getUrlEncodedFormData() throws UnsupportedEncodingException {

        StringBuilder sb = new StringBuilder()
                .append("documentName=").append(this.archivedStatement.documentName)
                .append("&documentKey=").append(this.archivedStatement.documentKey)
                .append("&extendedDocumentKey=").append(this.archivedStatement.extendedDocumentKey)
                .append("&documentExtension=").append(this.archivedStatement.documentExtension)
                .append("&documentWorkingDate=").append(this.archivedStatement.documentWorkingDate)
                .append("&documentSize=").append(this.archivedStatement.documentSize)
                .append("&documentNumberPages=").append(this.archivedStatement.documentNumberPages)
                .append("&documentType=").append(this.archivedStatement.documentType)
                .append("&folderName=").append(this.archivedStatement.folderName)
                .append("&foldersEnvSuffix=").append(this.archivedStatement.foldersEnvSuffix);

        return sb.toString();
    }
}
