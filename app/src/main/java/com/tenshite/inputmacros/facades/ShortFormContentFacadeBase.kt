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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.*

abstract class ShortFormContentFacadeBase(service: MyAccessibilityService) : AppFacadeBase(service) {
    init {
        commands["SwipeDown"] = { SwipeDown()}
        commands["SwipeUp"] = {SwipeUp()}
    }

    override suspend fun executeIntent(commandName: String, args: Bundle?) {
        if(commands.containsKey(commandName)){
            super.executeIntent(commandName,args)
        }
        else
            Log.d("ShortFormContentControllerBase", "Command not found")
    }





    protected open suspend fun SwipeDown(){
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

        // Suspend until the gesture is completed or cancelled
        return suspendCancellableCoroutine { continuation ->
            Handler(Looper.getMainLooper()).post {
                accessibilityService.dispatchGesture(
                    gesture,
                    object : AccessibilityService.GestureResultCallback() {
                        override fun onCompleted(gestureDescription: GestureDescription?) {
                            if (continuation.isActive) {
                                continuation.resume(Unit) // Resume coroutine on success
                            }
                        }

                        override fun onCancelled(gestureDescription: GestureDescription?) {
                            if (continuation.isActive) {
                                continuation.resumeWithException(Exception("Gesture cancelled"))
                            }
                        }
                    },
                    null
                )
            }
        }
    }

    protected open suspend fun SwipeUp(){
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

        // Dispatch the swipe gesture

        // Suspend until the gesture is completed or cancelled
        return suspendCancellableCoroutine { continuation ->
            Handler(Looper.getMainLooper()).post {
                accessibilityService.dispatchGesture(
                    gesture,
                    object : AccessibilityService.GestureResultCallback() {
                        override fun onCompleted(gestureDescription: GestureDescription?) {
                            if (continuation.isActive) {
                                continuation.resume(Unit) // Resume coroutine on success
                            }
                        }

                        override fun onCancelled(gestureDescription: GestureDescription?) {
                            if (continuation.isActive) {
                                continuation.resumeWithException(Exception("Gesture cancelled"))
                            }
                        }
                    },
                    null
                )
            }
        }
    }
}


