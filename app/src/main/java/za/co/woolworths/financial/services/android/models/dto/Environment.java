package za.co.woolworths.financial.services.android.models.dto;

/**
 * Created by denysvera on 2016/04/29.
 */
public class Environment {
    public String base_url;
    public String apiKey;
    public String sha1Password;
    public String ssoRedirectURI;
    public String stsURI;
    public String ssoRedirectURILogout;

    public String getApiId() {
        return apiKey;
    }

    public String getApiPassword() {
        return sha1Password;
    }

    public String getBase_url() {
        return base_url;
    }

    public String getSsoRedirectURI() {
        return ssoRedirectURI;
    }

    public void setStsURI(String stsURI) {
        this.stsURI = stsURI;
    }

    public void setApiId(String apiId) {
        this.apiKey = apiId;
    }

    public void setApiPassword(String sha1Password) {
        this.sha1Password = sha1Password;
    }

    public void setBase_url(String base_url) {
        this.base_url = base_url;
    }

    public void setSsoRedirectURI(String ssoRedirectURI) {
        this.ssoRedirectURI = ssoRedirectURI;
    }

    public String getStsURI() {
        return stsURI;
    }

    public String getSsoRedirectURILogout() {
        return ssoRedirectURILogout;
    }

    public void setSsoRedirectURILogout(String ssoRedirectURILogout) {
        this.ssoRedirectURILogout = ssoRedirectURILogout;
    }
}
