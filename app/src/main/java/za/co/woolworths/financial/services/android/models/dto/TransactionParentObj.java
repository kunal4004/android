package za.co.woolworths.financial.services.android.models.dto;

import java.util.List;

/**
 * Created by W7099877 on 13/12/2016.
 */

public class TransactionParentObj {

    String month;
    List<Transaction> transactionList;

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public List<Transaction> getTransactionList() {
        return transactionList;
    }

    public void setTransactionList(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }
}
