package com.tenshite.inputmacros

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Path
import android.graphics.Rect
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.tenshite.inputmacros.controllers.AppControllerCollection
import com.tenshite.inputmacros.controllers.TikTokContentConroller
import kotlin.math.log

class MyAccessibilityService : AccessibilityService() {
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("AccessiblityBroadcastReciever:",intent?.action ?: "");
            if (intent?.action == "com.tenshite.inputmacros.SWIPE_DOWN") {
                val data = intent.getStringExtra("some_data")
                //swipeDown()
            }

            if (intent?.action == "com.tenshite.inputmacros.CheckAd") {
                //checkAd()
            }
        }
    }

    private val AppControllerCollection = AppControllerCollection()

    private fun checkAd(){
        val rootNode = rootInActiveWindow
        Log.d("AdFinder","${getChildNodeWithText(rootNode,"Sponzorováno")!=null}");
        //print content descriptions
        printAllContentDescriptions(rootNode)

        // Check if ad is present
        val adNode = rootNode.findAccessibilityNodeInfosByViewId("com.tiktok.shortvideo:id/ad_container")
    }

    private fun getChildNodeWithText(node: AccessibilityNodeInfo,text: CharSequence) : AccessibilityNodeInfo?{
        if(node.text != null && node.text.contains(text))
            return node
        else
            for(i in 0 until node.childCount){
                val childNode = getChildNodeWithText(node.getChild(i),text)
                if(childNode!=null)
                    return childNode
            }
        return null
    }

    private fun printAllContentDescriptions(node: AccessibilityNodeInfo){
        Log.d("AdFinderDetails", "${node.text?: "null"} ${node.contentDescription?.toString() ?: "null"}");
        for(i in 0 until node.childCount){
            printAllContentDescriptions(node.getChild(i))
        }
    }

    override fun onCreate() {
        super.onCreate()

        AppControllerCollection.AddController(TikTokContentConroller(this))
        registerReceiver(receiver,AppControllerCollection.getIntentFilter(packageName))
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            val source = event.source ?: return
            Toast.makeText(this, "Button pressed", Toast.LENGTH_SHORT).show()

            // Get button coordinates
            val location = IntArray(2)
            val bounds = Rect();
            source.getBoundsInScreen(bounds);

            val x = bounds.centerX()
            val y = bounds.centerY()

            Log.d(
                "AccessibilityService",
                "Button clicked: ${source.text} pos: ($x, $y)"
            )
            Log.d("AccessibilityService","Changes $event.windowChanges");
            Log.d("AccessibilityService","Window $rootInActiveWindow");

            //showCircleOverlay(x, y)
        }
    }


    override fun onInterrupt() {
        // Handle service interruption
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}