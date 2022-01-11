package za.co.woolworths.financial.services.android.models.dto;

import javax.annotation.Nullable;

import za.co.woolworths.financial.services.android.models.dto.app_config.AppConfig;

/**
 * Created by denysvera on 2016/04/29.
 */
public class ConfigResponse {
    @Nullable public AppConfig configs;
    public Response response;
    public int httpCode;
}