package sakebook.github.com.native_ads

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout

class UnifiedAdLayout(
        private val context: Context,
        messenger: BinaryMessenger,
        id: Int,
        arguments: HashMap<String, Any>
) : PlatformView {

    private companion object {

        private var seed = 1

    }

    private val identifier = seed++

    private val TAG = "UnifiedAdLayout"

    private val hostPackageName = arguments["package_name"] as String
    private val layoutRes = context.resources.getIdentifier(arguments["layout_name"] as String, "layout", hostPackageName)
    private val unifiedNativeAdView: UnifiedNativeAdView = View.inflate(context, layoutRes, null) as UnifiedNativeAdView
    private val rootLayout: ViewGroup = unifiedNativeAdView.findViewById(context.resources.getIdentifier("rootLayout", "id", hostPackageName))
    private val headlineView: TextView = unifiedNativeAdView.findViewById(context.resources.getIdentifier("flutter_native_ad_headline", "id", hostPackageName))
    private val bodyView: TextView = unifiedNativeAdView.findViewById(context.resources.getIdentifier("flutter_native_ad_body", "id", hostPackageName))
    private val callToActionView: TextView = unifiedNativeAdView.findViewById(context.resources.getIdentifier("flutter_native_ad_call_to_action", "id", hostPackageName))
    private val attributionView = unifiedNativeAdView.findViewById<TextView>(context.resources.getIdentifier("flutter_native_ad_attribution", "id", hostPackageName))

    private val mediaView: MediaView? = unifiedNativeAdView.findViewById(context.resources.getIdentifier("flutter_native_ad_media", "id", hostPackageName))
    private val iconView: ImageView = unifiedNativeAdView.findViewById(context.resources.getIdentifier("flutter_native_ad_icon", "id", hostPackageName))
    private val starRatingView: TextView? = unifiedNativeAdView.findViewById(context.resources.getIdentifier("flutter_native_ad_star", "id", hostPackageName))
    private val storeView: TextView? = unifiedNativeAdView.findViewById(context.resources.getIdentifier("flutter_native_ad_store", "id", hostPackageName))
    private val priceView: TextView? = unifiedNativeAdView.findViewById(context.resources.getIdentifier("flutter_native_ad_price", "id", hostPackageName))
    private val advertiserView: TextView? = unifiedNativeAdView.findViewById(context.resources.getIdentifier("flutter_native_ad_advertiser", "id", hostPackageName))

    private val methodChannel: MethodChannel = MethodChannel(messenger, "com.github.sakebook.android/unified_ad_layout_$id")
    private var ad: UnifiedNativeAd? = null

    private val placementId = arguments["placement_id"] as String
    private val tablet = arguments["tablet"] as Boolean
    private val attributionText = arguments["text_attribution"] as String

    init {
        applyTheme(dark = arguments["dark"] as Boolean)
        setupTestDevices(arguments["test_devices"] as MutableList<String>?)
        loadAd()
        setupMethodCallHandler()
    }

    private fun setupTestDevices(testDevicesIds: MutableList<String>?) {
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDevicesIds).build()
        MobileAds.setRequestConfiguration(configuration)
    }

    private fun loadAd() {

        log("loadAd() start....")

        AdLoader.Builder(context, placementId)
                .forUnifiedNativeAd {
                    ad = it
                    ensureUnifiedAd(it)
                }
                .withAdListener(object : AdListener() {

                    override fun onAdImpression() {
                        methodChannel.invokeMethod("onAdImpression", null)
                    }

                    override fun onAdLeftApplication() {
                        methodChannel.invokeMethod("onAdLeftApplication", null)
                    }

                    override fun onAdClicked() {
                        methodChannel.invokeMethod("onAdClicked", null)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        methodChannel.invokeMethod("onAdFailedToLoad", hashMapOf("errorCode" to error.code))
                    }

                    override fun onAdFailedToLoad(errorCode: Int) {
                        methodChannel.invokeMethod("onAdFailedToLoad", hashMapOf("errorCode" to errorCode))
                    }

                    override fun onAdLoaded() {
                        rootLayout.visibility = View.VISIBLE
                        methodChannel.invokeMethod("onAdLoaded", null)
                    }
                })
                .withNativeAdOptions(NativeAdOptions.Builder().build())
                .build()
                .loadAd(AdRequest.Builder().build())

        log("loadAd() end....")
    }

    private fun setupMethodCallHandler() {
        methodChannel.setMethodCallHandler { call, result ->
            when (call.method) {
                "applyTheme" -> {
                    applyTheme(dark = call.argument("dark")!!)
                    result.success(true)
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun applyTheme(dark: Boolean) {

        log("applyTheme($dark), started....")
        val palette = Palette(
                dark = dark,
                tablet = tablet,
                context = context,
                hostPackageName = hostPackageName
        )

        unifiedNativeAdView.setBackgroundColor(palette.backgroundColor)

        iconView.apply {
            val current = (layoutParams as ConstraintLayout.LayoutParams)
            current.width = palette.adImageSize
            current.height = palette.adImageSize
            layoutParams = current
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            iconView.foreground = palette.adImageForeground
        }

        headlineView.setTextColor(palette.textColor)
        headlineView.typeface = palette.roboto500
        headlineView.textSize = palette.adTextSize

        bodyView.setTextColor(palette.textColor)
        bodyView.typeface = palette.roboto500
        bodyView.textSize = palette.adTextSize

        attributionView.text = attributionText
        attributionView.textSize = palette.attributionTextSize
        attributionView.setTextColor(palette.attributionTextColor)
        attributionView.typeface = palette.roboto400
        attributionView.apply {
            val current = (layoutParams as ViewGroup.MarginLayoutParams)
            current.topMargin = palette.attributionTopOffset.toInt()
            layoutParams = current
        }

        callToActionView.setTextColor(palette.textColor)
        callToActionView.setBackgroundResource(palette.actionButtonBackgroundId)
        callToActionView.typeface = palette.roboto400
        callToActionView.textSize = palette.actionButtonTextSize
        callToActionView.apply {
            val current = (layoutParams as ViewGroup.MarginLayoutParams)
            current.topMargin = palette.actionButtonTopOffset
            layoutParams = current
        }
        log("applyTheme(), finished....")
    }

    override fun getView(): View {
        return unifiedNativeAdView
    }

    override fun dispose() {
        ad?.destroy()
        unifiedNativeAdView.removeAllViews()
        methodChannel.setMethodCallHandler(null)
    }

    private fun ensureUnifiedAd(ad: UnifiedNativeAd?) {

        log("ensureUnifiedAd() started....")

        headlineView.text = ad?.headline
        bodyView.text = ad?.body
        callToActionView.text = ad?.callToAction

        mediaView?.setMediaContent(ad?.mediaContent)
        iconView?.setImageDrawable(
                ad?.mediaContent?.mainImage
                        ?: ad?.images?.firstOrNull()?.drawable
                        ?: ad?.icon?.drawable
        )
        starRatingView?.text = "${ad?.starRating}"
        storeView?.text = ad?.store
        priceView?.text = ad?.price
        advertiserView?.text = ad?.advertiser

        unifiedNativeAdView.bodyView = bodyView
        unifiedNativeAdView.headlineView = headlineView
        unifiedNativeAdView.callToActionView = callToActionView

        unifiedNativeAdView.mediaView = mediaView
        unifiedNativeAdView.iconView = iconView
        unifiedNativeAdView.starRatingView = starRatingView
        unifiedNativeAdView.storeView = storeView
        unifiedNativeAdView.priceView = priceView
        unifiedNativeAdView.advertiserView = advertiserView

        try {
            unifiedNativeAdView.setNativeAd(ad)
        } catch (ignored: Throwable) {
        }

        log("ensureUnifiedAd() finished....")
    }

    private fun log(text: String) {
        Log.d(TAG, "dbgId: $identifier, $text thread: ${Thread.currentThread().name}")
    }


    private class AdLoadingStack {

        private val items = mutableListOf<UnifiedAdLayout>()

        fun scheduleAdLoading(adLayout: UnifiedAdLayout) {
            items.add(adLayout)
            if (items.size == 1) {
                loadNextAd()
            }
        }

        fun notifyAdLoaded(adLayout: UnifiedAdLayout) {
            remove(adLayout)
            if (items.isNotEmpty()) {
                loadNextAd()
            }
        }

        private fun loadNextAd() {
            items.lastOrNull()?.loadAd()
        }

        fun remove(adLayout: UnifiedAdLayout) {
            items.remove(adLayout)
        }
    }
}
