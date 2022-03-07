package com.clevertap.react;

import android.util.Log;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONObject;

public class CleverTapUtils {

    private static final String TAG = "CleverTapUtils";

    @SuppressWarnings({"TypeParameterExplicitlyExtendsObject", "rawtypes"})
    public static WritableMap getWritableMapFromMap(Map<String, ? extends Object> var1) {
        JSONObject extras = var1 != null ? new JSONObject(var1) : new JSONObject();
        WritableMap extrasParams = Arguments.createMap();
        Iterator extrasKeys = extras.keys();
        while (extrasKeys.hasNext()) {
            String key = null;
            String value = null;
            try {
                key = extrasKeys.next().toString();
                value = extras.get(key).toString();
            } catch (Throwable t) {
                Log.e(TAG, t.getLocalizedMessage());
            }

            if (key != null && value != null) {
                extrasParams.putString(key, value);
            }
        }
        return extrasParams;
    }

}
