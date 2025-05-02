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
import com.tenshite.inputmacros.MyAccessibilityService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

class NovinkyCZContentfacade(accessibilityService: MyAccessibilityService) :
    AppFacadeBase(accessibilityService) {
    override val controllerName = "NovinkyCZ"

    init {
        commands["Open"] = { OpenWebsite(); "" }
        commands["FocusAd"] = { FocusAd().toString() }
        commands["SwipeDown"] = { Swipe(-200); "" }
    }

    private suspend fun OpenWebsite() {
        //launch intent with website
        val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse("https://www.novinky.cz") }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(accessibilityService.applicationContext.packageManager) != null) {
            accessibilityService.applicationContext.startActivity(intent)
        } else {
            // Handle case where no app can handle the intent
            Log.e("NovinkyCZContentFacade", "No app can handle this intent")
        }
        delay(1000)
    }

    private suspend fun FocusAd() : Boolean {
//        val nodes = AccessibilityDataExtractor.SelectNodes(accessibilityService.rootInActiveWindow,
//            { it.contentDescription?.toString()?.lowercase()?.contains("reklama") ?: false });

        delay(1000)
        var nodes = accessibilityService.cachedNodesInWindow.filter {
            it.contentDescription?.toString()?.lowercase()?.contains("reklama") ?: false
        }
        if (nodes.isNotEmpty()) {
            do {
                nodes = accessibilityService.cachedNodesInWindow.filter {
                    it.contentDescription?.toString()?.lowercase()?.contains("reklama") ?: false
                }
                val screenBounds = accessibilityService.screenBounds
                val validNodes = nodes.filter { node -> node.bounds.top > screenBounds.bottom };
                if (validNodes.isEmpty())
                    return false
                val nextAd = validNodes.minBy { node -> abs(node.bounds.top) }

                val distanceToSwipe = -screenBounds.bottom * 0.5 + nextAd.bounds.top
                if (abs(distanceToSwipe) > 200)
                    Swipe(min(distanceToSwipe.toInt(),screenBounds.bottom*2)).await()
            }while (abs(distanceToSwipe) > screenBounds.bottom*2)
            return true
        }
        return false
    }

    override suspend fun getContentType(): ContentType {
        TODO("Not yet implemented")
    }

    @Suppress("NAME_SHADOWING")
    fun Swipe(distanceToSwipe: Int = 100): Deferred<Unit> =
        CoroutineScope(Dispatchers.Default).async {
            var distanceToSwipe = distanceToSwipe
            Log.d("ShortFormContentControllerBase", "SwipeDown")
            val bounds = Rect();
            accessibilityService.rootInActiveWindow.getBoundsInScreen(bounds)
            val startX = bounds.centerX().toFloat()
            val startY = if (distanceToSwipe >= 0) bounds.bottom * 0.8f else (bounds.bottom * 0.2f)
            var endY = if (distanceToSwipe >= 0) bounds.bottom * 0.2f else bounds.bottom * 0.8f
            val maxSwipeDistance = endY - startY

            while (abs(distanceToSwipe) > 300) {
                val path = Path()

                val currentSwipe = sign(distanceToSwipe.toFloat()).toInt() * min(abs(distanceToSwipe), abs(maxSwipeDistance.toInt()))

                endY = startY - currentSwipe

                path.moveTo(startX,startY)
                path.lineTo(startX,endY)
                path.lineTo(startX,endY-1)

                val gestureBuilder = GestureDescription.Builder()
                gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 100)) // Swipe lasts 500 ms
                val path2 = Path()
                path2.moveTo(startX,endY)
                path2.lineTo(startX,endY)
                gestureBuilder.addStroke(GestureDescription.StrokeDescription(path2, 101, 100)) // Swipe lasts 500 ms


                val gesture = gestureBuilder.build()


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
                withContext(Dispatchers.IO) {
                    delay(1200)
                    latch.await()
                    //Log.e("AppControllerEvent","Content=" + getContentType().toString())
                }


                distanceToSwipe -= currentSwipe
            }

        }

}