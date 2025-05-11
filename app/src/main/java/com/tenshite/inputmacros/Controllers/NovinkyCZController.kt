package com.tenshite.inputmacros.Controllers

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.tenshite.inputmacros.MyAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign
import androidx.core.net.toUri

/**
 * Inherits from AppFacadeBase and allows controlling the NovinkyCZ website
 * @param accessibilityService The accessibility service instance used to interact with the OS
 */
class NovinkyCZController(accessibilityService: MyAccessibilityService) :
    AppControllerBase(accessibilityService) {
    // name of the controller to resolve the commands
    override val controllerName = "NovinkyCZ"

    //App commands
    init {
        commands["Open"] = { OpenWebsite(); "" }
        commands["FocusAd"] = { FocusAd().toString() }
        commands["SwipeDown"] = { Swipe(-200).await(); "" }
    }

    /**
     * Opens the Novinky.cz website in the default browser
     */
    private suspend fun OpenWebsite() {
        //launch intent with website
        val intent = Intent(Intent.ACTION_VIEW).apply { data = "https://www.novinky.cz".toUri() }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(accessibilityService.applicationContext.packageManager) != null) {
            accessibilityService.applicationContext.startActivity(intent)
        } else {
            // Handle case where no app can handle the intent
            Log.e("NovinkyCZContentFacade", "No app can handle this intent")
        }
        //wait for the website to load
        delay(1000)
    }

    /**
     * Focuses on the first ad on the screen
     * @return true if an ad was found and focused, false otherwise
     */
    private suspend fun FocusAd(): Boolean {
//        val nodes = AccessibilityDataExtractor.SelectNodes(accessibilityService.rootInActiveWindow,
//            { it.contentDescription?.toString()?.lowercase()?.contains("reklama") ?: false });

        delay(1000)
        var nodes = accessibilityService.cachedNodesInWindow.filter {
            it.contentDescription?.toString()?.lowercase()?.contains("reklama") ?: false
        }
        // go to te next add if there is one
        if (nodes.isNotEmpty()) {
            do {
                // get all ads
                nodes = accessibilityService.cachedNodesInWindow.filter {
                    it.contentDescription?.toString()?.lowercase()?.contains("reklama") ?: false
                }

                val screenBounds = accessibilityService.screenBounds
                val validNodes = nodes.filter { node -> node.bounds.top > screenBounds.bottom }
                // if there are no nodes give up
                if (validNodes.count() < 2)
                    return false
                // get the closest node to the top of the screen
                val nextAd = validNodes.minBy { node -> abs(node.bounds.top) }

                // swipe to the ad
                // it is separated to parts because of issues with data from accessibility service
                val distanceToSwipe = -screenBounds.bottom * 0.5 + nextAd.bounds.top
                if (abs(distanceToSwipe) > 200)
                    Swipe(min(distanceToSwipe.toInt(), screenBounds.bottom * 2)).await()
            } while (abs(distanceToSwipe) > screenBounds.bottom * 2)
            return true
        }
        return false
    }

    // this does not really work in this context
    /**
     * Gets current content, but this does not really work with websites.
     * @return ContentType.Unknown
     */
    override suspend fun getContentType(): ContentType {
        return ContentType.Unknown
    }

    /**
     * Swipes the screen down with accuracy of 300 pixels
     * @param distanceToSwipe The distance to swipe in pixels
     * @return Deferred<Unit>
     */
    @Suppress("NAME_SHADOWING")
    fun Swipe(distanceToSwipe: Int = 100): Deferred<Unit> =
        CoroutineScope(Dispatchers.Default).async {
            var distanceToSwipe = distanceToSwipe
            Log.d("ShortFormContentControllerBase", "SwipeDown")
            val bounds = Rect()
            accessibilityService.rootInActiveWindow.getBoundsInScreen(bounds)
            // gets the distance and direction to swipe
            val startX = bounds.centerX().toFloat()
            val startY = if (distanceToSwipe >= 0) bounds.bottom * 0.8f else (bounds.bottom * 0.2f)
            var endY = if (distanceToSwipe >= 0) bounds.bottom * 0.2f else bounds.bottom * 0.8f
            val maxSwipeDistance = endY - startY

            // If the distance to swipe is negative, we need to adjust the start and end points
            while (abs(distanceToSwipe) > 300) {
                val path = Path()

                val currentSwipe = sign(distanceToSwipe.toFloat()).toInt() * min(
                    abs(distanceToSwipe),
                    abs(maxSwipeDistance.toInt())
                )

                endY = startY - currentSwipe

                // makes a path
                path.moveTo(startX, startY)
                path.lineTo(startX, endY)
                path.lineTo(startX, endY - 1)

                //builds a swipe
                val gestureBuilder = GestureDescription.Builder()
                gestureBuilder.addStroke(
                    GestureDescription.StrokeDescription(
                        path,
                        0,
                        100
                    )
                )
                // touch to stop the swipe
                val path2 = Path()
                path2.moveTo(startX, endY)
                path2.lineTo(startX, endY)
                gestureBuilder.addStroke(
                    GestureDescription.StrokeDescription(
                        path2,
                        101,
                        100
                    )
                )


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
                withContext(Dispatchers.IO) {
                    delay(1200)
                    latch.await()
                    //Log.e("AppControllerEvent","Content=" + getContentType().toString())
                }


                distanceToSwipe -= currentSwipe
            }

        }

}