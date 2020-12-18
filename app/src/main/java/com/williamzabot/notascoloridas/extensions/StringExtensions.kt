package com.williamzabot.notascoloridas.extensions

fun String.toDay() = this.substring(0, 2).toInt()
fun String.toMonth() = this.substring(2, 4).toInt() - 1
fun String.toYear() = this.substring(4, 8).toInt()
fun String.toHour() = this.substring(8, 10).toInt()
fun String.toMinute() = this.substring(10, 12).toInt()
