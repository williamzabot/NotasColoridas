package com.williamzabot.notascoloridas.extensions

import android.content.Context
import android.graphics.drawable.Drawable

fun transformDrawable(
    context: Context,
    colorInText: String
) = context.resources.getDrawable(context.resources.getIdentifier(colorInText, "drawable", context.packageName))