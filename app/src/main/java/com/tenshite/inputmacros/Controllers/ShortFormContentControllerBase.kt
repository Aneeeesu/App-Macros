package com.tenshite.inputmacros.Controllers

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

abstract class ShortFormContentControllerBase(service: MyAccessibilityService) : AppControllerBase(service) {
    init {
        commands["SwipeDown"] = { bundle -> val pair =  swipeDown(bundle); pair.first + " " + pair.second}
        commands["SwipeUp"] = {bundle ->
            val pair = swipeUp(bundle)
            pair.first + " " + pair.second
        }
    }

    override suspend fun executeIntent(commandName: String, args: Bundle?) : String {
        var returnVal = ""
        if(commands.containsKey(commandName)){
            returnVal = super.executeIntent(commandName,args)
        }
        else {
            Log.d("ShortFormContentControllerBase", "Command not found")
        }
        return returnVal
    }





    /**
     * Swipes Down the screen and returns the content type and description
     * @param bundle Bundle with dist argument, it is not mandatory but can be used to set the distance of the swipe, it is limited half of the screen height
     * @return ContentType
     */
    protected open suspend fun swipeDown(bundle: Bundle?) : Pair<String,String> {
        Log.d("ShortFormContentControllerBase", "SwipeDown")
        val bounds = Rect()
        bounds.set(accessibilityService.screenBounds)
        val path = Path()
        val startX = bounds.centerX().toFloat()


        val startY = bounds.bottom * 0.65f
        var endY = bounds.bottom * 0.25f

        if(bundle != null && bundle.getFloat("dist") != 0.0f){
            endY = bounds.bottom * 0.65f - bundle.getFloat("dist")
        }

        endY = max(bounds.top.toFloat(),endY)

        //prepare gesture
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
        }
        return Pair(getContentType().toString(),getDescription())
    }

    protected abstract fun getDescription() : String

    /**
     * Swipes Up the screen and returns the content type and description
     * @param bundle Bundle with dist argument, it is not mandatory but can be used to set the distance of the swipe, it is limited half of the screen height
     * @return ContentType
     */
    protected open suspend fun swipeUp(bundle: Bundle?) : Pair<String,String> {
        val bounds = Rect()
        accessibilityService.rootInActiveWindow.getBoundsInScreen(bounds)
        val path = Path()
        val startX = bounds.centerX().toFloat()


        val startY = bounds.bottom * 0.35f
        var endY = bounds.bottom * 0.75f

        if(bundle != null && bundle.getFloat("dist") != 0.0f){
            endY = bounds.bottom * 0.35f + bundle.getFloat("dist")
        }

        endY = max(bounds.top.toFloat(),endY)

        //prepare gesture
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
        }
        return Pair(getContentType().toString(),getDescription())
    }
}


