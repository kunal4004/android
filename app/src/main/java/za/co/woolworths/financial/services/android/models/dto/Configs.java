package za.co.woolworths.financial.services.android.models.dto;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.chat.PresenceInAppChat;
import za.co.woolworths.financial.services.android.models.dto.quick_shop.QuickShopDefaultValues;

public class Configs {
    public Environment enviroment;
    public int checkInterval;
    public Expiry expiry;
    public Defaults defaults;
    public AbsaBankingOpenApiServices absaBankingOpenApiServices;
    public PresenceInAppChat presenceInAppChat;
    public QuickShopDefaultValues quickShopDefaultValues;
    public InstantCardReplacement instantCardReplacement;
    public VirtualTempCard virtualTempCard;
    public ArrayList<String> whitelistedDomainsForQRScanner;
}
