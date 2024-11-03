package com.tenshite.inputmacros.controllers

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.util.Log

abstract class ShortFormContentControllerBase(service: AccessibilityService) : ControllerBase(service) {
    init {
        commands["SwipeDown"] = {SwipeDown()}
        commands["SwipeUp"] = {SwipeUp()}
    }

    override fun ExecuteIntent(commandName: String) {
        if(commands.containsKey(commandName)){
            super.ExecuteIntent(commandName)
            commands[commandName]?.invoke()
        }
        else
            Log.d("ShortFormContentControllerBase", "Command not found")
    }



    open fun SwipeDown(){
        Log.d("ShortFormContentControllerBase", "SwipeDown")
        val bounds = Rect();
        service.rootInActiveWindow.getBoundsInScreen(bounds)
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

        service.dispatchGesture(gesture, null, null)
    }
    fun SwipeUp(){
        Log.d("ShortFormContentControllerBase", "SwipeUp")
        val bounds = Rect();
        service.rootInActiveWindow.getBoundsInScreen(bounds)
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

        service.dispatchGesture(gesture, null, null)
    }
}


