package za.co.woolworths.financial.services.android.models.dto;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.CreditCardDelivery;
import javax.annotation.Nullable;

import za.co.woolworths.financial.services.android.models.dto.bpi.BalanceProtectionInsurance;
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.InAppChat;
import za.co.woolworths.financial.services.android.models.dto.contact_us.ContactUs;
import za.co.woolworths.financial.services.android.models.dto.quick_shop.QuickShopDefaultValues;
import za.co.woolworths.financial.services.android.models.dto.whatsapp.WhatsApp;

public class Configs {
    @Nullable
    public Environment enviroment;
    public int checkInterval;
    public Expiry expiry;
    @Nullable
    public Defaults defaults;
    @Nullable
    public AbsaBankingOpenApiServices absaBankingOpenApiServices;
    @Nullable
    public PayMyAccount payMyAccount;
    @Nullable
    public QuickShopDefaultValues quickShopDefaultValues;
    @Nullable
    public InstantCardReplacement instantCardReplacement;
    @Nullable
    public VirtualTempCard virtualTempCard;
    @Nullable
    public ApplyNowLinks applyNowLinks;
    @Nullable
    public ArrayList<String> whitelistedDomainsForQRScanner;
    @Nullable
    public Sts sts;
    @Nullable
    public CreditCardActivation creditCardActivation;
    public CreditCardDelivery creditCardDelivery;
    @Nullable
    public WhatsApp whatsApp;
    @Nullable
    public ArrayList<ContactUs> contactUs;
    @Nullable
    public ClickAndCollect clickAndCollect;
    @Nullable
    public InAppChat inAppChat;
    @Nullable
    public ProductDetailsPage productDetailsPage;
    @Nullable
    public CreditView creditView;
    @Nullable
    public DashConfig dashConfig;
    @Nullable
    public CreditLimitIncrease creditLimitIncrease;
    @Nullable
    public BalanceProtectionInsurance balanceProtectionInsurance;
}
