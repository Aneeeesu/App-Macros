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

abstract class ShortFormContentFacadeBase(service: MyAccessibilityService) : AppFacadeBase(service) {
    init {
        commands["SwipeDown"] = { swipeDown()}
        commands["SwipeUp"] = {swipeUp()}
    }

    override suspend fun executeIntent(commandName: String, args: Bundle?) {
        if(commands.containsKey(commandName)){
            super.executeIntent(commandName,args)
        }
        else
            Log.d("ShortFormContentControllerBase", "Command not found")
    }





    protected open suspend fun swipeDown(){
        Log.d("ShortFormContentControllerBase", "SwipeDown")
        val bounds = Rect();
        accessibilityService.rootInActiveWindow.getBoundsInScreen(bounds)
        val path = Path()
        val startX = bounds.centerX().toFloat()
        val startY = bounds.bottom * 0.75f
        val endY = bounds.bottom * 0.25f

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

    protected open suspend fun swipeUp(){
        Log.d("ShortFormContentControllerBase", "SwipeUp")
        val bounds = Rect();
        accessibilityService.rootInActiveWindow.getBoundsInScreen(bounds)
        val path = Path()
        val startX = bounds.centerX().toFloat()
        val startY = bounds.bottom * 0.25f
        val endY = bounds.bottom * 0.75f

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
            delay(1000)
            Log.e("AppControllerEvent","Content=" + getContentType().toString())
        }
    }
}


