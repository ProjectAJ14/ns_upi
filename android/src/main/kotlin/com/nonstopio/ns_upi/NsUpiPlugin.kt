package com.nonstopio.ns_upi

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Base64
import android.util.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.ByteArrayOutputStream

class NsUpiPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private var channel: MethodChannel? = null
    private var activity: ActivityPluginBinding? = null

    private var result: Result? = null
    private var requestCodeNumber = 201119

    private var hasResponded = false

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "ns_upi")
        channel?.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel?.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding
        binding.addActivityResultListener { requestCode, _, data ->
            if (requestCodeNumber == requestCode && result != null) {
                if (data != null) {
                    try {
                        val response = data.getStringExtra("response")!!
                        success(response)
                    } catch (ex: Exception) {
                        success("invalid_response")
                    }
                } else {
                    success("user_cancelled")
                }
            }
            true
        }
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        hasResponded = false
        this.result = result

        when (call.method) {
            "initiateTransaction" -> initiateTransaction(call)
            "getInstalledUpiApps" -> getInstalledUpiApps(result)
            else -> result.notImplemented()
        }
    }

    private fun initiateTransaction(call: MethodCall) {
        Log.d("ns_upi", "initiateTransaction")

        val app: String? = call.argument("app")
        val pa: String? = call.argument("pa")
        val pn: String? = call.argument("pn")
        val mc: String? = call.argument("mc")
        val tr: String? = call.argument("tr")
        val tn: String? = call.argument("tn")
        val am: String? = call.argument("am")
        val cu: String? = call.argument("cu")
        val url: String? = call.argument("url")

        try {
            var uriStr: String? = "upi://pay?pa=" + pa +
                    "&pn=" + Uri.encode(pn) +
                    "&tr=" + Uri.encode(tr) +
                    "&am=" + Uri.encode(am) +
                    "&cu=" + Uri.encode(cu)
            if (url != null) {
                uriStr += ("&url=" + Uri.encode(url))
            }
            if (mc != null) {
                uriStr += ("&mc=" + Uri.encode(mc))
            }
            if (tn != null) {
                uriStr += ("&tn=" + Uri.encode(tn))
            }
            uriStr += "&mode=00"
            val uri = Uri.parse(uriStr)

            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage(app)

            if (activity?.activity?.packageManager?.let { intent.resolveActivity(it) } == null) {
                success("activity_unavailable")
                return
            }

            activity?.activity?.startActivityForResult(intent, requestCodeNumber)
        } catch (ex: Exception) {
            Log.e("ns_upi", ex.toString())
            success("failed_to_open_app")
        }
    }

    private fun getInstalledUpiApps(result: Result) {
        Log.d("ns_upi", "getInstalledUpiApps")
        val uriBuilder = Uri.Builder()
        uriBuilder.scheme("upi").authority("pay")

        val uri = uriBuilder.build()
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val packageManager = activity?.activity?.packageManager

        try {
            val activities =
                packageManager?.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            //Log activities
            Log.d("ns_upi", activities.toString())
            val activityResponse = activities?.map {
                Log.d("ns_upi", it.toString())
                val packageName = it.activityInfo.packageName
                val drawable = packageManager.getApplicationIcon(packageName)

                val bitmap = getBitmapFromDrawable(drawable)
                val icon = encodeToBase64(bitmap)

                mapOf(
                    "packageName" to packageName,
                    "icon" to icon,
                    "priority" to it.priority,
                    "preferredOrder" to it.preferredOrder
                )
            }

            result.success(activityResponse)
        } catch (ex: Exception) {
            Log.e("ns_upi", ex.toString())
            result.error("getInstalledUpiApps", "exception", ex)
        }
    }

    private fun encodeToBase64(image: Bitmap): String? {
        val byteArrayOS = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOS)
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.NO_WRAP)
    }

    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        val bmp: Bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bmp
    }

    private fun success(o: String) {
        if (!hasResponded) {
            hasResponded = true
            result?.success(o)
        }
    }
}
