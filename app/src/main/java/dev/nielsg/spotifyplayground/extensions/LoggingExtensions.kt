package dev.nielsg.spotifyplayground.extensions

import android.util.Log

val Any.TAG: String
	get() {
		val tag = javaClass.simpleName
		return if (tag.length <= 23) tag else tag.substring(0, 23)
	}

fun Any.log(string: String) {
	Log.d(TAG, string)
}

fun Any.logError(string: String, e: Exception?) {
	Log.e(TAG, string, e)
}

fun Any.logError(string: String, e: Throwable?) {
	Log.e(TAG, string, e)
}

fun Any.logError(string: String) {
	Log.e(TAG, string)
}