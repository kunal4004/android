package za.co.woolworths.financial.services.android.models.dto;

import javax.annotation.Nullable;

/**
 * Created by denysvera on 2016/04/29.
 */
public class ConfigResponse {
    @Nullable public Configs configs;
    public Response response;
    public int httpCode;
}