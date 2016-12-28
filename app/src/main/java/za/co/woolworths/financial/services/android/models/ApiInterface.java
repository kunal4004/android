package za.co.woolworths.financial.services.android.models;


import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import za.co.woolworths.financial.services.android.models.dto.AccountResponse;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.AuthoriseLoanRequest;
import za.co.woolworths.financial.services.android.models.dto.AuthoriseLoanResponse;
import za.co.woolworths.financial.services.android.models.dto.BankAccountTypes;
import za.co.woolworths.financial.services.android.models.dto.CLIEmailResponse;
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse;
import za.co.woolworths.financial.services.android.models.dto.ContactUsConfigResponse;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferRequest;
import za.co.woolworths.financial.services.android.models.dto.CreateOfferResponse;
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDevice;
import za.co.woolworths.financial.services.android.models.dto.CreateUpdateDeviceResponse;
import za.co.woolworths.financial.services.android.models.dto.DeaBanks;
import za.co.woolworths.financial.services.android.models.dto.DeleteMessageResponse;
import za.co.woolworths.financial.services.android.models.dto.IssueLoanRequest;
import za.co.woolworths.financial.services.android.models.dto.IssueLoanResponse;
import za.co.woolworths.financial.services.android.models.dto.LocationResponse;
import za.co.woolworths.financial.services.android.models.dto.LoginRequest;
import za.co.woolworths.financial.services.android.models.dto.LoginResponse;
import za.co.woolworths.financial.services.android.models.dto.MessageReadRequest;
import za.co.woolworths.financial.services.android.models.dto.MessageResponse;
import za.co.woolworths.financial.services.android.models.dto.Offer;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.models.dto.ReadMessagesResponse;
import za.co.woolworths.financial.services.android.models.dto.TransactionHistoryResponse;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetail;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetailResponse;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;

public interface ApiInterface {

    @Headers({"Content-Type: application/json", "Accept: application/json", "Media-Type: application/json"})
    @GET("/user/accounts")
    AccountResponse getAccount(
            @Header("apiId") String apiId,
            @Header("sha1Password") String sha1Password,
            @Header("deviceVersion") String deviceVersion,
            @Header("deviceModel") String deviceModel,
            @Header("network") String network,
            @Header("os") String os,
            @Header("osVersion") String osVersion,
            @Header("userAgent") String userAgent,
            @Header("userAgentVersion") String userAgentVersion,
            @Header("sessionToken") String sessionToken,
            @Query("productOfferingId") String productOfferingId);

    @Headers({"Content-Type: application/json", "Accept: application/json", "Media-Type: application/json"})
    @GET("/user/accounts")
    AccountsResponse getAccounts(
            @Header("apiId") String apiId,
            @Header("sha1Password") String sha1Password,
            @Header("deviceVersion") String deviceVersion,
            @Header("deviceModel") String deviceModel,
            @Header("network") String network,
            @Header("os") String os,
            @Header("osVersion") String osVersion,
            @Header("userAgent") String userAgent,
            @Header("userAgentVersion") String userAgentVersion,
            @Header("sessionToken") String sessionToken);

    @Headers({"Content-Type: application/json", "Accept: application/json", "Media-Type: application/json"})
    @GET("/user/account/{productOfferingId}/history")
    TransactionHistoryResponse getAccountTransactionHistory(
            @Header("apiId") String apiId,
            @Header("sha1Password") String sha1Password,
            @Header("deviceVersion") String deviceVersion,
            @Header("deviceModel") String deviceModel,
            @Header("network") String network,
            @Header("os") String os,
            @Header("osVersion") String osVersion,
            @Header("userAgent") String userAgent,
            @Header("userAgentVersion") String userAgentVersion,
            @Header("sessionToken") String sessionToken,
            @Path("productOfferingId") String productOfferingId);

    @Headers({"Content-Type: application/json", "Accept: application/json", "Media-Type: application/json"})
    @GET("/user/vouchers")
    VoucherResponse getVouchers(
            @Header("apiId") String apiId,
            @Header("sha1Password") String sha1Password,
            @Header("deviceVersion") String deviceVersion,
            @Header("deviceModel") String deviceModel,
            @Header("network") String network,
            @Header("os") String os,
            @Header("osVersion") String osVersion,
            @Header("userAgent") String userAgent,
            @Header("userAgentVersion") String userAgentVersion,
            @Header("sessionToken") String sessionToken);

    @Headers({"Content-Type: application/json", "Accept: application/json", "Media-Type: application/json"})
    @POST("/user/session")
    LoginResponse login(
            @Header("apiId") String apiId,
            @Header("sha1Password") String sha1Password,
            @Header("deviceVersion") String deviceVersion,
            @Header("deviceModel") String deviceModel,
            @Header("network") String network,
            @Header("os") String os,
            @Header("osVersion") String osVersion,
            @Header("userAgent") String userAgent,
            @Header("userAgentVersion") String userAgentVersion,
            @Body LoginRequest loginRequest);

    @Headers({"Content-Type: application/json", "Accept: application/json", "Media-Type: application/json"})
    @POST("/user/loan/request")
    IssueLoanResponse issueLoan(
            @Header("apiId") String apiId,
            @Header("sha1Password") String sha1Password,
            @Header("deviceVersion") String deviceVersion,
            @Header("deviceModel") String deviceModel,
            @Header("network") String network,
            @Header("os") String os,
            @Header("osVersion") String osVersion,
            @Header("userAgent") String userAgent,
            @Header("userAgentVersion") String userAgentVersion,
            @Header("sessionToken") String sessionToken,
            @Body IssueLoanRequest issueLoanRequest);

    @Headers({"Content-Type: application/json", "Accept: application/json", "Media-Type: application/json"})
    @POST("/user/loan/authorise")
    AuthoriseLoanResponse authoriseLoan(
            @Header("apiId") String apiId,
            @Header("sha1Password") String sha1Password,
            @Header("deviceVersion") String deviceVersion,
            @Header("deviceModel") String deviceModel,
            @Header("network") String network,
            @Header("os") String os,
            @Header("osVersion") String osVersion,
            @Header("userAgent") String userAgent,
            @Header("userAgentVersion") String userAgentVersion,
            @Header("sessionToken") String sessionToken,
            @Body AuthoriseLoanRequest authoriseLoanRequest);

    @Headers({"Content-Type: application/json", "Accept: application/json", "Media-Type: application/json"})
    @GET("/config/contactus")
    ContactUsConfigResponse getContactUsConfig(
            @Header("apiId") String apiId,
            @Header("sha1Password") String sha1Password,
            @Header("deviceVersion") String deviceVersion,
            @Header("deviceModel") String deviceModel,
            @Header("network") String network,
            @Header("os") String os,
            @Header("osVersion") String osVersion,
            @Header("userAgent") String userAgent,
            @Header("userAgentVersion") String userAgentVersion);

    @Headers({"Content-Type: application/json", "Accept: application/json", "Media-Type: application/json"})
    @GET("/{appVersion}")
    ConfigResponse getConfig(
            @Header("Authorization-X") String AuthorizationX,
            @Header("UUID") String UUID,
            @Path("appVersion") String appVersion
          );
    @Headers({"Content-Type: application/json", "Accept: application/json", "Media-Type: application/json"})
    @GET("/user/locations")
    LocationResponse getStoresLocation(
            @Header("apiId") String apiId,
            @Header("sha1Password") String sha1Password,
            @Header("deviceVersion") String deviceVersion,
            @Header("deviceModel") String deviceModel,
            @Header("network") String network,
            @Header("os") String os,
            @Header("osVersion") String osVersion,
            @Header("userAgent") String userAgent,
            @Header("userAgentVersion") String userAgentVersion,
            @Header("sessionToken") String sessionToken,
            @Query("lat") String lat ,
            @Query("lon") String lon,
            @Query("searchString") String searchString,
            @Query("radius") String radius
            );

    @Headers({"Content-Type: application/json", "Accept: application/json", "Media-Type: application/json"})
    @GET("/user/messages")
    MessageResponse getMessages(
            @Header("apiId") String apiId,
            @Header("sha1Password") String sha1Password,
            @Header("deviceVersion") String deviceVersion,
            @Header("deviceModel") String deviceModel,
            @Header("network") String network,
            @Header("os") String os,
            @Header("osVersion") String osVersion,
            @Header("userAgent") String userAgent,
            @Header("userAgentVersion") String userAgentVersion,
            @Header("sessionToken") String sessionToken,
            @Query("pageSize") int pageSize,
            @Query("pageNumber") int pageNumber

    );

    @Headers({"Content-Type: application/json", "Accept: application/json", "Media-Type: application/json"})
    @DELETE("/user/messages/{id}")
    DeleteMessageResponse getDeleteresponse(
            @Header("apiId") String apiId,
            @Header("sha1Password") String sha1Password,
            @Header("deviceVersion") String deviceVersion,
            @Header("deviceModel") String deviceModel,
            @Header("network") String network,
            @Header("os") String os,
            @Header("osVersion") String osVersion,
            @Header("userAgent") String userAgent,
            @Header("userAgentVersion") String userAgentVersion,
            @Header("sessionToken") String sessionToken,
            @Path("id") String id
    );
    @Headers({"Content-Type: application/json", "Accept: application/json", "Media-Type: application/json"})
    @PUT("/user/messages")
    ReadMessagesResponse setReadMessages(
            @Header("apiId") String apiId,
            @Header("sha1Password") String sha1Password,
            @Header("deviceVersion") String deviceVersion,
            @Header("deviceModel") String deviceModel,
            @Header("network") String network,
            @Header("os") String os,
            @Header("osVersion") String osVersion,
            @Header("userAgent") String userAgent,
            @Header("userAgentVersion") String userAgentVersion,
            @Header("sessionToken") String sessionToken,
            @Body MessageReadRequest readMessages
    );
    @Headers({"Content-Type: application/json", "Accept: application/json", "Media-Type: application/json"})
    @POST("/user/devices")
    CreateUpdateDeviceResponse createUpdateDevice(
            @Header("apiId") String apiId,
            @Header("sha1Password") String sha1Password,
            @Header("deviceVersion") String deviceVersion,
            @Header("deviceModel") String deviceModel,
            @Header("network") String network,
            @Header("os") String os,
            @Header("osVersion") String osVersion,
            @Header("userAgent") String userAgent,
            @Header("userAgentVersion") String userAgentVersion,
            @Header("sessionToken") String sessionToken,
            @Body CreateUpdateDevice device
    );
    @Headers({"Content-Type: application/json", "Accept: application/json", "Media-Type: application/json"})
    @GET("/user/cli/DEABanks")
    DeaBanks getDeaBanks(
            @Header("apiId") String apiId,
            @Header("sha1Password") String sha1Password,
            @Header("deviceVersion") String deviceVersion,
            @Header("deviceModel") String deviceModel,
            @Header("network") String network,
            @Header("os") String os,
            @Header("osVersion") String osVersion,
            @Header("userAgent") String userAgent,
            @Header("userAgentVersion") String userAgentVersion,
            @Header("sessionToken") String sessionToken,
            @Query("pageSize") int pageSize,
            @Query("pageNumber") int pageNumber
    );
    @Headers({"Content-Type: application/json", "Accept: application/json", "Media-Type: application/json"})
    @GET("/user/cli/DEABankAccountTypes")
    BankAccountTypes getBankAccountTypes(
            @Header("apiId") String apiId,
            @Header("sha1Password") String sha1Password,
            @Header("deviceVersion") String deviceVersion,
            @Header("deviceModel") String deviceModel,
            @Header("network") String network,
            @Header("os") String os,
            @Header("osVersion") String osVersion,
            @Header("userAgent") String userAgent,
            @Header("userAgentVersion") String userAgentVersion,
            @Header("sessionToken") String sessionToken,
            @Query("pageSize") int pageSize,
            @Query("pageNumber") int pageNumber
    );

    @Headers({"Content-Type: application/json", "Accept: application/json", "Media-Type: application/json"})
    @POST("/user/cli/offer")
    CreateOfferResponse createOfferRequest(
            @Header("apiId") String apiId,
            @Header("sha1Password") String sha1Password,
            @Header("deviceVersion") String deviceVersion,
            @Header("deviceModel") String deviceModel,
            @Header("network") String network,
            @Header("os") String os,
            @Header("osVersion") String osVersion,
            @Header("userAgent") String userAgent,
            @Header("userAgentVersion") String userAgentVersion,
            @Body CreateOfferRequest createOfferRequest);

    @Headers({"Content-Type: application/json", "Accept: application/json", "Media-Type: application/json"})
    @GET("/user/cli/offerActive")
    OfferActive getActiveOfferRequest(
            @Header("apiId") String apiId,
            @Header("sha1Password") String sha1Password,
            @Header("deviceVersion") String deviceVersion,
            @Header("deviceModel") String deviceModel,
            @Header("network") String network,
            @Header("os") String os,
            @Header("osVersion") String osVersion,
            @Header("sessionToken") String sessionToken,
            @Query("productOfferingId") String productOfferingId);

    @Headers({"Content-Type: application/json", "Accept: application/json", "Media-Type: application/json"})
    @POST("/user/cli/offer/email")
    CLIEmailResponse cliSendEmailRquest(
            @Header("apiId") String apiId,
            @Header("sha1Password") String sha1Password,
            @Header("deviceVersion") String deviceVersion,
            @Header("deviceModel") String deviceModel,
            @Header("network") String network,
            @Header("os") String os,
            @Header("sessionToken") String sessionToken,
            @Header("osVersion") String osVersion,
            @Body String email);

    @Headers({"Content-Type: application/json", "Accept: application/json", "Media-Type: application/json"})
    @POST("/user/cli/offer/email")
    UpdateBankDetailResponse cliUpdateBankRequest(
            @Header("apiId") String apiId,
            @Header("sha1Password") String sha1Password,
            @Header("deviceVersion") String deviceVersion,
            @Header("deviceModel") String deviceModel,
            @Header("network") String network,
            @Header("os") String os,
            @Header("sessionToken") String sessionToken,
            @Header("osVersion") String osVersion,
            @Body UpdateBankDetail updateBankDetail);

}
