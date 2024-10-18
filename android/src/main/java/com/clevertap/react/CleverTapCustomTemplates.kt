package com.clevertap.react

import android.content.Context
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.inapp.customtemplates.CustomTemplateContext.FunctionContext
import com.clevertap.android.sdk.inapp.customtemplates.CustomTemplateContext.TemplateContext
import com.clevertap.android.sdk.inapp.customtemplates.CustomTemplateException
import com.clevertap.android.sdk.inapp.customtemplates.FunctionPresenter
import com.clevertap.android.sdk.inapp.customtemplates.TemplatePresenter
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object CleverTapCustomTemplates {

    private val templatePresenter: TemplatePresenter = object : TemplatePresenter {
        override fun onPresent(context: TemplateContext) {
            CleverTapEventEmitter.emit(
                CleverTapEvent.CLEVERTAP_CUSTOM_TEMPLATE_PRESENT, context.templateName
            )
        }

        override fun onClose(context: TemplateContext) {
            CleverTapEventEmitter.emit(
                CleverTapEvent.CLEVERTAP_CUSTOM_TEMPLATE_CLOSE, context.templateName
            )
        }
    }

    private val functionPresenter = FunctionPresenter { context: FunctionContext ->
        CleverTapEventEmitter.emit(
            CleverTapEvent.CLEVERTAP_CUSTOM_FUNCTION_PRESENT, context.templateName
        )
    }

    @JvmStatic
    fun registerCustomTemplates(context: Context, vararg jsonAssets: String) {
        for (jsonAsset in jsonAssets) {
            val jsonDefinitions = readAsset(context, jsonAsset)
            CleverTapAPI.registerCustomInAppTemplates(
                jsonDefinitions, templatePresenter, functionPresenter
            )
        }
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
