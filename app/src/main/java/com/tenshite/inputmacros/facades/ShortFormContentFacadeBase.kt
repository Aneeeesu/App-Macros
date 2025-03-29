package com.tenshite.inputmacros.facades

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.tenshite.inputmacros.MyAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import kotlin.math.max
import kotlin.math.min

abstract class ShortFormContentFacadeBase(service: MyAccessibilityService) : AppFacadeBase(service) {
    init {
        commands["SwipeDown"] = { bundle ->  swipeDown(bundle)}
        commands["SwipeUp"] = {bundle ->  swipeUp(bundle)}
    }

    override suspend fun executeIntent(commandName: String, args: Bundle?) {
        if(commands.containsKey(commandName)){
            super.executeIntent(commandName,args)
        }
        else
            Log.d("ShortFormContentControllerBase", "Command not found")
    }





    protected open suspend fun swipeDown(bundle: Bundle?){
        Log.d("ShortFormContentControllerBase", "SwipeDown")
        val bounds = Rect();
        accessibilityService.rootInActiveWindow.getBoundsInScreen(bounds)
        val path = Path()
        val startX = bounds.centerX().toFloat()


        val startY = bounds.bottom * 0.65f
        var endY = bounds.bottom * 0.25f

        if(bundle != null && bundle.getFloat("dist") != 0.0f){
            endY = bounds.bottom * 0.65f - bundle.getFloat("dist")
        }

        endY = max(bounds.top.toFloat(),endY)


        path.moveTo(startX,startY)
        path.lineTo(startX,endY)

        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 500)) // Swipe lasts 500 ms

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

            latch.await()
            delay(1000)
            Log.e("AppControllerEvent","Content=" +  getContentType().toString())
        }
    }

    protected open suspend fun swipeUp(bundle: Bundle?){
        val bounds = Rect();
        accessibilityService.rootInActiveWindow.getBoundsInScreen(bounds)
        val path = Path()
        val startX = bounds.centerX().toFloat()


        val startY = bounds.bottom * 0.35f
        var endY = bounds.bottom * 0.75f

        if(bundle != null && bundle.getFloat("dist") != 0.0f){
            endY = bounds.bottom * 0.35f + bundle!!.getFloat("dist")
        }

        endY = max(bounds.top.toFloat(),endY)

        path.moveTo(startX,startY)
        path.lineTo(startX,endY)

        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 100)) // Swipe lasts 500 ms

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
            latch.await()
            delay(300)
            Log.e("AppControllerEvent","Content=" + getContentType().toString())
        }
    }
}


