package com.tenshite.inputmacros.facades

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.tenshite.inputmacros.MyAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import kotlin.math.max
import kotlin.math.min

class NovinkyCZContentfacade(accessibilityService: MyAccessibilityService) :
    AppFacadeBase(accessibilityService) {
    override val controllerName = "NovinkyCZ"

    init {
//        commands["Open"] = { OpenWebsite() }
//        commands["FocusAd"] = { FocusAd() }
//        commands["SwipeDown"] = { Swipe(-200) }
    }

    private fun OpenWebsite() {
        //launch intent with website
        val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse("https://www.novinky.cz") }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(accessibilityService.applicationContext.packageManager) != null) {
            accessibilityService.applicationContext.startActivity(intent)
        } else {
            // Handle case where no app can handle the intent
            Log.e("NovinkyCZContentFacade", "No app can handle this intent")
        }
    }

    private fun FocusAd() {
//        val nodes = AccessibilityDataExtractor.SelectNodes(accessibilityService.rootInActiveWindow,
//            { it.contentDescription?.toString()?.lowercase()?.contains("reklama") ?: false });

        val nodes = accessibilityService.cachedNodesInWindow.filter {
            it.contentDescription?.toString()?.lowercase()?.contains("reklama") ?: false
        }
        if (nodes.isNotEmpty()) {
            Swipe(nodes.first().bounds.bottom)
        }
    }

    override suspend fun GetContentType(): ContentType {
        TODO("Not yet implemented")
    }

    @Suppress("NAME_SHADOWING")
    fun Swipe(distanceToSwipe: Int = 100) {
        var distanceToSwipe = distanceToSwipe
        Log.d("ShortFormContentControllerBase", "SwipeDown")
        val bounds = Rect();
        accessibilityService.rootInActiveWindow.getBoundsInScreen(bounds)
        val startX = bounds.centerX().toFloat()
        val startY = bounds.bottom * 0.2f
        val endY = bounds.bottom * 0.8f
        val maxSwipeDistance = endY - startY

        while (distanceToSwipe < 0) {
            val currentSwipe = max(distanceToSwipe, maxSwipeDistance.toInt())
            val path = Path()
            path.moveTo(startX, startY)
            path.lineTo(startX, startY + currentSwipe)

            val gestureBuilder = GestureDescription.Builder()
            gestureBuilder.addStroke(
                GestureDescription.StrokeDescription(
                    path,
                    0,
                    100
                )
            ) // Swipe lasts 500 ms

            val pause = Path()
            pause.moveTo(startX, startY + currentSwipe)
            pause.lineTo(startX, startY + currentSwipe)
            gestureBuilder.addStroke(
                GestureDescription.StrokeDescription(
                    pause,
                    0,
                    100
                )
            ) // Swipe lasts 500 ms

            val gesture = gestureBuilder.build()

            // Dispatch the swipe gesture

            val latch = CountDownLatch(1)
            Handler(Looper.getMainLooper()).post {
                accessibilityService.dispatchGesture(
                    gesture,
                    object : AccessibilityService.GestureResultCallback() {
                        override fun onCompleted(gestureDescription: GestureDescription?) {
                            latch.countDown() // Signal that the gesture is complete
                        }

                        override fun onCancelled(gestureDescription: GestureDescription?) {
                            latch.countDown() // Signal cancellation
                        }
                    },
                    null
                )

            }

// Wait for the gesture to finish
            latch.await()
            distanceToSwipe += currentSwipe
        }
    }
}