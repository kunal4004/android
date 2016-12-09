package za.co.woolworths.financial.services.android.models.dao;

import za.co.woolworths.financial.services.android.util.PersistenceLayer;

/**
 * Created by eesajacobs on 2016/11/29.
 */

public class SessionDao {

    //make the constructor private.
    private SessionDao() {}

    //singleton
    private static SessionDao instance = new SessionDao();
    public static SessionDao getInstance() {
        return instance;
    }
}
