package com.clevertap.react;

import android.util.Log;

import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONException;
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

    public static WritableArray getWritableArrayFromDisplayUnitList(List<CleverTapDisplayUnit> list) {
        WritableArray writableArray = Arguments.createArray();

        if (list != null) {
            for (CleverTapDisplayUnit item : list) {
                if (item != null && item.getJsonObject() != null) {
                    WritableMap displayUnitMap = convertObjectToWritableMap(item.getJsonObject());
                    writableArray.pushMap(displayUnitMap);
                }
            }
        }

        return writableArray;
    }

    public static WritableMap convertObjectToWritableMap(JSONObject jsonObject) {
        WritableMap writableMap = Arguments.createMap();

        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                Object value = jsonObject.get(key);

                if (value instanceof String) {
                    writableMap.putString(key, (String) value);
                } else if (value instanceof Integer) {
                    writableMap.putInt(key, (Integer) value);
                } else if (value instanceof Boolean) {
                    writableMap.putBoolean(key, (Boolean) value);
                } else if (value instanceof Float || value instanceof Double) {
                    writableMap.putDouble(key, Double.parseDouble(value.toString()));
                } else if (value instanceof JSONObject) {
                    WritableMap nestedWritableMap = convertObjectToWritableMap((JSONObject) value);
                    writableMap.putMap(key, nestedWritableMap);
                } else if (value instanceof JSONArray) {
                    WritableArray nestedWritableArray = convertArrayToWritableArray((JSONArray) value);
                    writableMap.putArray(key, nestedWritableArray);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return writableMap;
    }

    private static WritableArray convertArrayToWritableArray(JSONArray jsonArray) {
        WritableArray writableArray = Arguments.createArray();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Object value = jsonArray.get(i);

                if (value instanceof String) {
                    writableArray.pushString((String) value);
                } else if (value instanceof Integer) {
                    writableArray.pushInt((Integer) value);
                } else if (value instanceof Boolean) {
                    writableArray.pushBoolean((Boolean) value);
                } else if (value instanceof Float || value instanceof Double) {
                    writableArray.pushDouble(Double.parseDouble(value.toString()));
                } else if (value instanceof JSONObject) {
                    WritableMap nestedWritableMap = convertObjectToWritableMap((JSONObject) value);
                    writableArray.pushMap(nestedWritableMap);
                } else if (value instanceof JSONArray) {
                    WritableArray nestedWritableArray = convertArrayToWritableArray((JSONArray) value);
                    writableArray.pushArray(nestedWritableArray);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return writableArray;
    }

    public static class MapUtil {

        public static JSONObject toJSONObject(ReadableMap readableMap) throws JSONException {
            JSONObject jsonObject = new JSONObject();

            ReadableMapKeySetIterator iterator = readableMap.keySetIterator();

            while (iterator.hasNextKey()) {
                String key = iterator.nextKey();
                ReadableType type = readableMap.getType(key);

                switch (type) {
                    case Null:
                        jsonObject.put(key, null);
                        break;
                    case Boolean:
                        jsonObject.put(key, readableMap.getBoolean(key));
                        break;
                    case Number:
                        jsonObject.put(key, readableMap.getDouble(key));
                        break;
                    case String:
                        jsonObject.put(key, readableMap.getString(key));
                        break;
                    case Map:
                        jsonObject.put(key, MapUtil.toJSONObject(readableMap.getMap(key)));
                        break;
                    case Array:
                        jsonObject.put(key, ArrayUtil.toJSONArray(readableMap.getArray(key)));
                        break;
                }
            }

            return jsonObject;
        }


        public static Map<String, Object> toMap(ReadableMap readableMap) {
            Map<String, Object> map = new HashMap<>();
            ReadableMapKeySetIterator iterator = readableMap.keySetIterator();

            while (iterator.hasNextKey()) {
                String key = iterator.nextKey();
                ReadableType type = readableMap.getType(key);

                switch (type) {
                    case Null:
                        map.put(key, null);
                        break;
                    case Boolean:
                        map.put(key, readableMap.getBoolean(key));
                        break;
                    case Number:
                        map.put(key, readableMap.getDouble(key));
                        break;
                    case String:
                        map.put(key, readableMap.getString(key));
                        break;
                    case Map:
                        map.put(key, MapUtil.toMap(readableMap.getMap(key)));
                        break;
                    case Array:
                        map.put(key, ArrayUtil.toArray(readableMap.getArray(key)));
                        break;
                }
            }

            return map;
        }

        public static WritableMap toWritableMap(Map<String, Object> map) {
            WritableMap writableMap = Arguments.createMap();
            Iterator iterator = map.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry pair = (Map.Entry) iterator.next();
                writableMap.merge(addValue((String) pair.getKey(), pair.getValue()));
            }

            return writableMap;
        }

        public static WritableMap addValue(String key, Object value) {
            WritableMap writableMap = Arguments.createMap();
            if (value == null) {
                writableMap.putNull(key);
            } else if (value instanceof Boolean) {
                writableMap.putBoolean(key, (Boolean) value);
            } else if (value instanceof Double) {
                writableMap.putDouble(key, (Double) value);
            } else if (value instanceof Integer) {
                writableMap.putInt(key, (Integer) value);
            } else if (value instanceof String) {
                writableMap.putString(key, (String) value);
            } else if (value instanceof Map) {
                writableMap.putMap(key, MapUtil.toWritableMap((Map<String, Object>) value));
            } else if (value.getClass() != null && (value.getClass().isArray() || value instanceof ArrayList)) {
                writableMap.putArray(key, ArrayUtil.toWritableArray((ArrayList) value));
            }
            return writableMap;
        }

        public static class ArrayUtil {

            public static JSONArray toJSONArray(ReadableArray readableArray) throws JSONException {
                JSONArray jsonArray = new JSONArray();

                for (int i = 0; i < readableArray.size(); i++) {
                    ReadableType type = readableArray.getType(i);

                    switch (type) {
                        case Null:
                            jsonArray.put(i, null);
                            break;
                        case Boolean:
                            jsonArray.put(i, readableArray.getBoolean(i));
                            break;
                        case Number:
                            jsonArray.put(i, readableArray.getDouble(i));
                            break;
                        case String:
                            jsonArray.put(i, readableArray.getString(i));
                            break;
                        case Map:
                            jsonArray.put(i, MapUtil.toJSONObject(readableArray.getMap(i)));
                            break;
                        case Array:
                            jsonArray.put(i, ArrayUtil.toJSONArray(readableArray.getArray(i)));
                            break;
                    }
                }

                return jsonArray;
            }

            public static Object[] toArray(ReadableArray readableArray) {
                Object[] array = new Object[readableArray.size()];

                for (int i = 0; i < readableArray.size(); i++) {
                    ReadableType type = readableArray.getType(i);

                    switch (type) {
                        case Null:
                            array[i] = null;
                            break;
                        case Boolean:
                            array[i] = readableArray.getBoolean(i);
                            break;
                        case Number:
                            array[i] = readableArray.getDouble(i);
                            break;
                        case String:
                            array[i] = readableArray.getString(i);
                            break;
                        case Map:
                            array[i] = MapUtil.toMap(readableArray.getMap(i));
                            break;
                        case Array:
                            array[i] = ArrayUtil.toArray(readableArray.getArray(i));
                            break;
                    }
                }

                return array;
            }

            public static WritableArray toWritableArray(ArrayList arrayList) {
                WritableArray writableArray = Arguments.createArray();

                for (int i = 0; i < arrayList.size(); i++) {
                    Object value = arrayList.get(i);

                    if (value == null) {
                        writableArray.pushNull();
                    }
                    if (value instanceof Boolean) {
                        writableArray.pushBoolean((Boolean) value);
                    }
                    if (value instanceof Double) {
                        writableArray.pushDouble((Double) value);
                    }
                    if (value instanceof Integer) {
                        writableArray.pushInt((Integer) value);
                    }
                    if (value instanceof String) {
                        writableArray.pushString((String) value);
                    }
                    if (value instanceof Map) {
                        writableArray.pushMap(MapUtil.toWritableMap((Map<String, Object>) value));
                    }
                    if (value.getClass().isArray()) {
                        writableArray.pushArray(ArrayUtil.toWritableArray((ArrayList) value));
                    }
                }

                return writableArray;
            }
        }
    }
}
