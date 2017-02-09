package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;

public class DynamicJsonConverter implements Converter {

    private Context mContext;

    public DynamicJsonConverter(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public Object fromBody(TypedInput typedInput, Type type) throws ConversionException {
        try {
            InputStream in = typedInput.in(); // convert the typedInput to String
            String string = fromStream(in);
            in.close(); // we are responsible to close the InputStream after use

            if (String.class.equals(type)) {
                return string;
            } else {
                return new Gson().fromJson(string, type); // convert to the supplied type, typically Object, JsonObject or Map<String, Object>
            }
        } catch (Exception e) { // a lot may happen here, whatever happens
            throw new ConversionException(e); // wrap it into ConversionException so retrofit can process it
        }
    }

    @Override
    public TypedOutput toBody(Object object) { // not required
        return null;
    }

    private String fromStream(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
            out.append("\r\n");
        }
        // save json value
        try {
            SessionDao sessionDao = new SessionDao(mContext);
            sessionDao.key = SessionDao.KEY.STORES_LATEST_PAYLOAD;
            sessionDao.value = out.toString();
            try {
                sessionDao.save();
            } catch (Exception e) {
                Log.e("TAG", e.getMessage());
            }
        } catch (Exception e) {
            Log.e("exception", String.valueOf(e));
        }

        System.out.println(out.toString());
        return out.toString();
    }
}