package za.co.woolworths.financial.services.android.util;

/**
 * Created by eesajacobs on 2016/11/29.
 */
public class PersistenceLayer {
    private static PersistenceLayer instance = new PersistenceLayer();

    public static PersistenceLayer getInstance() {
        return instance;
    }

    private PersistenceLayer() {

    }
}
