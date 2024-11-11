package com.tenshite.inputmacros.facades

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import com.tenshite.inputmacros.MyAccessibilityService

abstract class ShortFormContentFacadeBase(service: MyAccessibilityService) : AppFacadeBase(service) {
    init {
        commands["SwipeDown"] = {SwipeDown()}
        commands["SwipeUp"] = {SwipeUp()}
    }

    override fun ExecuteIntent(commandName: String, args: Bundle?) {
        if(commands.containsKey(commandName)){
            super.ExecuteIntent(commandName,args)
        }
        else
            Log.d("ShortFormContentControllerBase", "Command not found")
    }



    protected open fun SwipeDown(){
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
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 100)) // Swipe lasts 500 ms

        val gesture = gestureBuilder.build()

        // Dispatch the swipe gesture

        accessibilityService.dispatchGesture(gesture, null, null)
    }
    protected open fun SwipeUp(){
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

        accessibilityService.dispatchGesture(gesture, null, null)
    }
}


