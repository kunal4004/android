package za.co.woolworths.financial.services.android.util.frag_nav;

public interface FragNavPopController {
    int tryPopFragments(int popDepth, FragNavTransactionOptions transactionOptions) throws UnsupportedOperationException;
}
