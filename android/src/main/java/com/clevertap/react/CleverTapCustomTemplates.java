package com.clevertap.react;

import static com.clevertap.react.CleverTapUtils.sendEventEnsureInitialization;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;

import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.inapp.customtemplates.CustomTemplateContext;
import com.clevertap.android.sdk.inapp.customtemplates.CustomTemplateException;
import com.clevertap.android.sdk.inapp.customtemplates.FunctionPresenter;
import com.clevertap.android.sdk.inapp.customtemplates.TemplatePresenter;
import com.facebook.react.ReactApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CleverTapCustomTemplates {

    public static void registerCustomTemplates(Context context, ReactApplication reactApplication, String... jsonAssets) {
        CleverTapCustomTemplates customTemplates = new CleverTapCustomTemplates(reactApplication);
        AssetManager assetManager = context.getAssets();
        for (String jsonAsset : jsonAssets) {
            try (InputStream assetInputStream = assetManager.open(jsonAsset)) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(assetInputStream, StandardCharsets.UTF_8));
                StringBuilder stringBuilder = new StringBuilder();
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line);
                    line = reader.readLine();
                }
                customTemplates.registerCustomTemplates(stringBuilder.toString());
            } catch (IOException e) {
                throw new CustomTemplateException("Could not read json definitions", e);
            }
        }
    }

    private ReactApplication mReactApplication;

    private final TemplatePresenter mTemplatePresenter = new TemplatePresenter() {

        @Override
        public void onClose(@NonNull CustomTemplateContext.TemplateContext context) {
            sendEventEnsureInitialization(CleverTapModule.CLEVERTAP_CUSTOM_TEMPLATE_CLOSE, context.getTemplateName(), mReactApplication);
        }

        @Override
        public void onPresent(@NonNull CustomTemplateContext.TemplateContext context) {
            sendEventEnsureInitialization(CleverTapModule.CLEVERTAP_CUSTOM_TEMPLATE_PRESENT, context.getTemplateName(), mReactApplication);
        }
    };

    private final FunctionPresenter mFunctionPresenter = context -> sendEventEnsureInitialization(CleverTapModule.CLEVERTAP_CUSTOM_FUNCTION_PRESENT, context.getTemplateName(), mReactApplication);

    public CleverTapCustomTemplates(@NonNull ReactApplication reactApplication) {
        mReactApplication = reactApplication;
    }

    public void registerCustomTemplates(@NonNull String jsonDefinitions) {
        CleverTapAPI.registerCustomInAppTemplates(jsonDefinitions, mTemplatePresenter, mFunctionPresenter);
    }
}
