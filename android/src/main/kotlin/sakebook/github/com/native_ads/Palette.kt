package sakebook.github.com.native_ads

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import androidx.annotation.DimenRes
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

class Palette(
        private val dark: Boolean,
        private val tablet: Boolean,
        private val context: Context,
        private val hostPackageName: String
) {

    val textColor = color(
            darkIdName = "text_color_dark",
            lightIdName = "text_color_light"
    )

    @IdRes
    val actionButtonBackgroundId = drawableId(
            darkIdName = "bg_action_button_dark",
            lightIdName = "bg_action_button_light"
    )

    val adImageForeground = drawable(
            darkIdName = "fg_ad_image_dark",
            lightIdName = "fg_ad_image_light"
    )

    val backgroundColor = color(
            darkIdName = "bg_dark_theme",
            lightIdName = "bg_light_theme"
    )

    val adImageSize = dip(if (tablet) 96 else 72)
    val attributionTopOffset = dip(if (tablet) 10 else 5)
    val actionButtonTopOffset = dip(if (tablet) 12 else 5)

    val adTextSize = if (tablet) 18f else 14f
    val attributionTextSize = if (tablet) 14f else 12f
    val actionButtonTextSize = if (tablet) 10f else 9f

    val roboto500 = font("roboto_medium")
    val roboto400 = font("roboto_regular")

    private fun font(idName: String) = ResourcesCompat.getFont(
            context,
            context.resources.getIdentifier(idName, "font", hostPackageName)
    )

    @Suppress("SameParameterValue")
    private fun drawable(darkIdName: String, lightIdName: String) =
            ContextCompat.getDrawable(
                    context,
                    drawableId(
                            darkIdName = darkIdName,
                            lightIdName = lightIdName
                    )
            )

    private fun drawableId(darkIdName: String, lightIdName: String) =
            identifier(
                    darkIdName = darkIdName,
                    lightIdName = lightIdName,
                    type = "drawable"
            )

    private fun color(darkIdName: String, lightIdName: String) =
            ContextCompat.getColor(
                    context,
                    colorId(darkIdName, lightIdName)
            )

    private fun colorId(darkIdName: String, lightIdName: String) =
            identifier(
                    darkIdName = darkIdName,
                    lightIdName = lightIdName,
                    type = "color"
            )

    private fun identifier(
            darkIdName: String,
            lightIdName: String,
            type: String
    ): Int =
            context.resources.getIdentifier(
                    if (dark) darkIdName else lightIdName,
                    type,
                    hostPackageName
            )

    private fun dimen(idName: String) = context.resources.getDimension(dimenId(idName))

    @DimenRes
    private fun dimenId(idName: String) = context.resources.getIdentifier(idName, "dimen", hostPackageName)

    fun dip(value: Int) = (value * context.resources.displayMetrics.density).toInt()
}
