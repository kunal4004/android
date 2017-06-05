package za.co.woolworths.financial.services.android.models.dao;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import za.co.woolworths.financial.services.android.util.PersistenceLayer;

/**
 * Created by eesajacobs on 2016/11/29.
 */

public class SessionDao extends BaseDao {
    private final String TAG = "SessionDao";
    public KEY key;
    public String value;

    public enum KEY {
        STORES_USER_LAST_LOCATION("STORES_USER_LAST_LOCATION"),
        STORES_USER_SEARCH("STORES_USER_SEARCH"),
        STORES_PRODUCT_SEARCH("STORES_PRODUCT_SEARCH"),
        STORES_LATEST_PAYLOAD("STORES_LATEST_PAYLOAD"),
        UNREAD_MESSAGE_COUNT("UNREAD_MESSAGE_COUNT"),
        STORE_SHOPPING_LIST("STORE_SHOPPING_LIST"),
        STORE_VOUCHER_COUNT("STORE_VOUCHER_COUNT"),
        ACCOUNT_IS_ACTIVE("ACCOUNT_IS_ACTIVE"),
        USER_TOKEN("USER_TOKEN"),
        PRODUCTS_ONE_TIME_POPUP("PRODUCTS_ONE_TIME_POPUP"),
        ON_BOARDING_SCREEN("ON_BOARDING_SCREEN"),
        SPLASH_VIDEO("SPLASH_VIDEO");

        private final String text;

        /**
         * @param key
         */
        private KEY(final String key) {
            this.text = key;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    private SessionDao() {
    }

    public SessionDao(Context mContext) {
        super(mContext);
    }

    public SessionDao(Context mContext, KEY key) {
        super(mContext);
        this.key = key;
    }

    @Override
    public String getTableName() {
        return "Session";
    }

    public void save() throws Exception {
        try {
            //perform update first. If update fails, perform insert
            this.update();
            return;

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        try {
            //perform insert
            this.insert();
            return;

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public SessionDao get() throws Exception {
        String query = "SELECT * FROM Session WHERE [key] = ? ORDER BY id ASC LIMIT 1;";
        Map<String, String> result = PersistenceLayer.getInstance(mContext).executeReturnableQuery(query, new String[]{
                this.key.toString()
        });

        for (Map.Entry<String, String> entry : result.entrySet()) {

            if (entry.getKey().equals("id")) {
                this.id = entry.getValue();
            } else if (entry.getKey().equals("key")) {
                this.key = KEY.valueOf(entry.getValue());
            } else if (entry.getKey().equals("value")) {
                this.value = entry.getValue();
            } else if (entry.getKey().equals("dateCreated")) {
                this.dateCreated = entry.getValue();
            } else if (entry.getKey().equals("dateUpdated")) {
                this.dateUpdated = entry.getValue();
            }
        }

        return this;
    }

    public void delete() throws Exception {
        String query = "DELETE FROM Session" +
                " WHERE [key] = ?";

        PersistenceLayer.getInstance(mContext).executeVoidQuery(query, new String[]{
                this.key.toString()
        });
    }

    private void insert() throws Exception {
        String query = "INSERT INTO Session ([key], value) VALUES (?, ?);";

        Map<String, String> arguments = new HashMap<>();
        arguments.put("key", this.key.toString());
        arguments.put("value", this.value);

        long rowid = PersistenceLayer.getInstance(mContext).executeInsertQuery(this.getTableName(), arguments);
        if (rowid == 0 || rowid == -1) {
            throw new RuntimeException("You Attempted to insert a new SessionDao record but not row id was returned. Insert failed!");
        }
    }

    private void update() throws Exception {
        String query = "UPDATE Session" +
                " SET value = ?," +
                " dateUpdated = datetime()" +
                " WHERE [key] = ?";

        PersistenceLayer.getInstance(mContext).executeVoidQuery(query, new String[]{
                this.value, this.key.toString()
        });
    }
}
