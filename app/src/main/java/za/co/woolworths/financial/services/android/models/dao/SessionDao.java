package za.co.woolworths.financial.services.android.models.dao;

import java.util.HashMap;
import java.util.Map;

import za.co.woolworths.financial.services.android.util.PersistenceLayer;
import za.co.woolworths.financial.services.android.util.Utils;

/**
 * Created by eesajacobs on 2016/11/29.
 */

public class SessionDao extends BaseDao {
	private final String TAG = "SessionDao";
	private final String tableName = "Session";
	public KEY key;
	public String value;

	public enum KEY {
		STORES_USER_LAST_LOCATION("STORES_USER_LAST_LOCATION"),
		STORES_USER_SEARCH("STORES_USER_SEARCH"),
		STORES_PRODUCT_SEARCH("STORES_PRODUCT_SEARCH"),
		STORES_LATEST_PAYLOAD("STORES_LATEST_PAYLOAD"),
		UNREAD_MESSAGE_COUNT("UNREAD_MESSAGE_COUNT"),
		STORE_SHOPPING_LIST("STORE_SHOPPING_LIST"),
		USER_TOKEN("USER_TOKEN"),
		STORE_FINDER_ONE_TIME_POPUP("STORE_FINDER_ONE_TIME_POPUP"),
		CART_FIRST_ORDER_FREE_DELIVERY("CART_FIRST_ORDER_FREE_DELIVERY"),
		ON_BOARDING_SCREEN("ON_BOARDING_SCREEN"),
		SPLASH_VIDEO("SPLASH_VIDEO"),
		APP_VERSION("APP_VERSION"),
		LAST_KNOWN_LOCATION("LAST_KNOWN_LOCATION"),
		NOTIFICATION_ID("NOTIFICATION_ID"),
		CLI_SLIDE_EDIT_AMOUNT_TOOLTIP("CLI_SLIDE_EDIT_AMOUNT_TOOLTIP"),
		DEVICE_ID("DEVICE_ID"),
		DELIVERY_LOCATION_HISTORY("DELIVERY_LOCATION_HISTORY"),
		PRODUCT_IS_ACTIVE("PRODUCT_IS_ACTIVE"),
		ACCOUNT_AUTHENTICATION_STATE("ACCOUNT_AUTHENTICATION_STATE"),
		SESSION_STATE("SESSION_STATE"),
		STS_PARAMS("STS_PARAMS"),
		BIOMETRIC_AUTHENTICATION_STATE("BIOMETRIC_AUTHENTICATION_STATE"),
		BIOMETRIC_AUTHENTICATION_SESSION("BIOMETRIC_AUTHENTICATION_SESSION"),
		APP_INSTANCE_OBJECT("APP_INSTANCE_OBJECT"),
		ABSA_ENCRYPTION_KEY("ABSA_ENCRYPTION_KEY"),
		DELIVERY_OPTION("DELIVERY_OPTION"),
		FCM_TOKEN("FCM_TOKEN"),
		IN_APP_REVIEW("IN_APP_REVIEW"),
		LIVE_CHAT_EXTRAS("LIVE_CHAT_EXTRAS"),
		LIQUOR_MODAL_SHOWN("LIQUOR_MODAL_SHOWN"),
		APP_CONFIG("APP_CONFIG"),
		LINKED_DEVICE_LIST("LINKED_DEVICE_LIST"),
		ACCOUNT_PRODUCT_PAYLOAD("ACCOUNT_PRODUCT_PAYLOAD"),
		STORE_CARD_RESPONSE_PAYLOAD("STORE_CARD_RESPONSE_PAYLOAD"),
		CARD_NOT_RECEIVED_DIALOG_WAS_SHOWN("CARD_NOT_RECEIVED_DIALOG_WAS_SHOWN"),
		ANONYMOUS_USER_LOCATION_DETAILS("ANONYMOUS_USER_LOCATION_DETAILS"),
		DEVICE_IDENTITY_TOKEN("DEVICE_IDENTITY_TOKEN"),
		USER_DEVICE_IDENTITY_TOKEN("USER_DEVICE_IDENTITY_TOKEN"),
		FICA_LAST_REQUEST_TIME("FICA_LAST_REQUEST_TIME"),
		PET_INSURANCE_INTRODUCTION_SHOWED("PET_INSURANCE_INTRODUCTION_SHOWED"),
		SCHEDULE_CREDIT_CARD_DELIVERY_ON_ACCOUNT_LANDING("SCHEDULE_CREDIT_CARD_DELIVERY_ON_ACCOUNT_LANDING"),
		OC_CHAT_FCM_TOKEN("OC_CHAT_FCM_TOKEN"),
		SHOP_OPTIMISER_SQLITE_MODEL("SHOP_OPTIMISER_SQLITE_MODEL");
		OC_CHAT_FCM_TOKEN("OC_CHAT_FCM_TOKEN"),
		DY_SERVER_ID("DY_SERVER_ID"),
		DY_SESSION_ID("DY_SESSION_ID");

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

	public enum SESSION_STATE {
		ACTIVE(1),
		INACTIVE(0);
		/**
		 * @param sessionState
		 */
		private final Integer sessionState;

		private SESSION_STATE(final Integer sessionState) {
			this.sessionState = sessionState;
		}
	}


	public SessionDao(String id, KEY key, String value, String dateCreated, String dateUpdated) {
		super();

		this.id = id;
		this.key = key;
		this.value = value;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
	}

	public SessionDao() {
		super();
	}

	@Override
	public String getTableName() {
		return this.tableName;
	}

	public static SessionDao getByKey(final KEY key) {
		SessionDao sessionDao;

		try {
			String query = "SELECT * FROM Session WHERE [key] = ? ORDER BY id ASC LIMIT 1;";
			Map<String, String> result = PersistenceLayer.getInstance().executeReturnableQuery(query, new String[]{
                    Utils.aes256EncryptStringAsBase64String(key.toString())
			});

			String id = null;
			String value = null;
			String dateCreated = null;
			String dateUpdated = null;

			for (Map.Entry<String, String> entry : result.entrySet()) {

				if (entry.getKey().equals("id")) {
					id = entry.getValue();
				} else if (entry.getKey().equals("value")) {
					value = Utils.aes256DecryptBase64EncryptedString(entry.getValue());
				} else if (entry.getKey().equals("dateCreated")) {
					dateCreated = entry.getValue();
				} else if (entry.getKey().equals("dateUpdated")) {
					dateUpdated = entry.getValue();
				}
			}

			sessionDao = new SessionDao(id, key, value, dateCreated, dateUpdated);
		} catch (Exception e) {

			sessionDao = new SessionDao();
			sessionDao.key = key;
		}

		return sessionDao;
	}

	public void delete() throws Exception {
		String query = "DELETE FROM Session" +
				" WHERE [key] = ?";

		PersistenceLayer.getInstance().executeVoidQuery(query, new String[]{
                Utils.aes256EncryptStringAsBase64String(this.key.toString())
		});
	}

	@Override
	public void insert() throws Exception {
		String query = "INSERT INTO Session ([key], value) VALUES (?, ?);";

		Map<String, String> arguments = new HashMap<>();
		arguments.put("key", Utils.aes256EncryptStringAsBase64String(this.key.toString()));
		arguments.put("value", Utils.aes256EncryptStringAsBase64String(this.value));

		long rowid = PersistenceLayer.getInstance().executeInsertQuery(this.getTableName(), arguments);
		if (rowid == 0 || rowid == -1) {
			throw new RuntimeException("You Attempted to insert a new SessionDao record but not row id was returned. Insert failed!");
		}
	}

	@Override
	public void update() throws Exception {
		String query = "UPDATE Session" +
				" SET value = ?," +
				" dateUpdated = datetime()" +
				" WHERE [key] = ?";

		PersistenceLayer.getInstance().executeVoidQuery(query, new String[]{
                Utils.aes256EncryptStringAsBase64String(this.value), Utils.aes256EncryptStringAsBase64String(this.key.toString())
		});
	}

	public enum BIOMETRIC_AUTHENTICATION_STATE{
		ON(1),
		OFF(0);
		/**
		 * @param sessionState
		 */
		private final Integer state;

		private BIOMETRIC_AUTHENTICATION_STATE(final Integer state) {
			this.state = state;
		}
	}
}
