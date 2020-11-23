package za.co.woolworths.financial.services.android.models.dto;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.CreditCardDelivery;
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.InAppChat;
import za.co.woolworths.financial.services.android.models.dto.contact_us.ContactUs;
import za.co.woolworths.financial.services.android.models.dto.quick_shop.QuickShopDefaultValues;
import za.co.woolworths.financial.services.android.models.dto.whatsapp.WhatsApp;

public class Configs {
    public Environment enviroment;
    public int checkInterval;
    public Expiry expiry;
    public Defaults defaults;
    public AbsaBankingOpenApiServices absaBankingOpenApiServices;
    public PayMyAccount payMyAccount;
    public QuickShopDefaultValues quickShopDefaultValues;
    public InstantCardReplacement instantCardReplacement;
    public VirtualTempCard virtualTempCard;
    public ApplyNowLinks applyNowLinks;
    public ArrayList<String> whitelistedDomainsForQRScanner;
    public Sts sts;
    public CreditCardActivation creditCardActivation;
    public CreditCardDelivery creditCardDelivery;
    public WhatsApp whatsApp;
    public ArrayList<ContactUs> contactUs;
    public ClickAndCollect clickAndCollect;
    public InAppChat inAppChat;
    public ProductDetailsPage productDetailsPage;
    public CreditView creditView;
}
