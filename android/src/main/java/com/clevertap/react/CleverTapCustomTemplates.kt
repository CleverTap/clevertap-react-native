package com.clevertap.react

import android.content.Context
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.inapp.customtemplates.CustomTemplateContext.FunctionContext
import com.clevertap.android.sdk.inapp.customtemplates.CustomTemplateContext.TemplateContext
import com.clevertap.android.sdk.inapp.customtemplates.CustomTemplateException
import com.clevertap.android.sdk.inapp.customtemplates.FunctionPresenter
import com.clevertap.android.sdk.inapp.customtemplates.JsonTemplatesProducer
import com.clevertap.android.sdk.inapp.customtemplates.TemplatePresenter
import com.clevertap.android.sdk.inapp.customtemplates.TemplateProducer
import com.facebook.react.bridge.Arguments
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object CleverTapCustomTemplates {

    @JvmStatic
    fun registerCustomTemplates(context: Context, vararg jsonAssets: String) {
        for (jsonAsset in jsonAssets) {
            val jsonDefinitions = readAsset(context, jsonAsset)
            // The SDK calls defineTemplates(ctConfig) once per CleverTap instance —
            // the default AND every secondary account, including instances created
            // later from JS. Each account gets the SAME json definitions, but wired
            // to presenters that know THAT account's id, so its template events can
            // be routed to the right JS handle. Parsing is delegated to the SDK's
            // own JsonTemplatesProducer.
            CleverTapAPI.registerCustomInAppTemplates(TemplateProducer { ctConfig ->
                JsonTemplatesProducer(
                    jsonDefinitions,
                    accountTemplatePresenter(ctConfig.accountId),
                    accountFunctionPresenter(ctConfig.accountId)
                ).defineTemplates(ctConfig)
            })
        }
    }

    private fun accountTemplatePresenter(accountId: String?) = object : TemplatePresenter {
        override fun onPresent(context: TemplateContext) {
            emitTemplateEvent(
                CleverTapEvent.CLEVERTAP_CUSTOM_TEMPLATE_PRESENT, context.templateName, accountId
            )
        }

        override fun onClose(context: TemplateContext) {
            emitTemplateEvent(
                CleverTapEvent.CLEVERTAP_CUSTOM_TEMPLATE_CLOSE, context.templateName, accountId
            )
        }
    }

    private fun accountFunctionPresenter(accountId: String?) = FunctionPresenter { context: FunctionContext ->
        emitTemplateEvent(
            CleverTapEvent.CLEVERTAP_CUSTOM_FUNCTION_PRESENT, context.templateName, accountId
        )
    }

    /**
     * The public payload of template events is (and stays) the template name STRING.
     * A string cannot carry the account tag, so native wraps it and the JS demux
     * unwraps it before user handlers run. An instance without an account id (never
     * seen in practice — the config requires one) degrades to the legacy untagged
     * string instead of failing.
     */
    private fun emitTemplateEvent(event: CleverTapEvent, templateName: String, accountId: String?) {
        if (accountId == null) {
            CleverTapEventEmitter.emit(event, templateName)
            return
        }
        val params = Arguments.createMap()
        params.putString(Constants.CT_ACCOUNT_ID_KEY, accountId)
        params.putString(Constants.CT_PAYLOAD_KEY, templateName)
        CleverTapEventEmitter.emit(event, params)
    }

    private fun readAsset(context: Context, asset: String): String {
        val assetManager = context.assets
        try {
            assetManager.open(asset).use { assetInputStream ->
                val reader =
                    BufferedReader(InputStreamReader(assetInputStream, StandardCharsets.UTF_8))

                return buildString {
                    var line = reader.readLine()
                    while (line != null) {
                        append(line)
                        line = reader.readLine()
                    }
                }
            }
        } catch (e: IOException) {
            throw CustomTemplateException("Could not read json asset", e)
        }
    }
}
