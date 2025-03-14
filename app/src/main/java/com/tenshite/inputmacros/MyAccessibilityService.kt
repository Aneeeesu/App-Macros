package com.tenshite.inputmacros

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.tenshite.inputmacros.controllers.AppControllerCollection
import com.tenshite.inputmacros.facades.AccessibilityDataExtractor
import com.tenshite.inputmacros.facades.NovinkyCZContentfacade
import com.tenshite.inputmacros.facades.TikTokContentFacade
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.random.Random
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Collections

class MyAccessibilityService : AccessibilityService() {
    var cachedNodesInWindow = listOf<cachedNode>()
    val eventFlow = MutableSharedFlow<Unit>()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action == "com.tenshite.inputmacros.printScreen"){
                listAllElements(rootInActiveWindow)
            }
            try {
                appControllerCollection.ExecuteIntent(intent?.action ?: "", intent?.extras)
            }
            catch (e: Exception){
                Log.e("AccessibilityService", "Error executing intent", e)
            }
        }
    }

    private val appControllerCollection = AppControllerCollection()

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
        cachedNodesInWindow = Collections.synchronizedList(mutableListOf<cachedNode>())


        val job = GlobalScope.launch(Dispatchers.Default) {
            while(true) {
                val rootNode = rootInActiveWindow
                cachedNodesInWindow =
                    AccessibilityDataExtractor.SelectNodes(rootNode) { true }.map { cachedNode(it) }

                eventFlow.emit(Unit)
                delay(500);
            }
        }

        appControllerCollection.AddController(TikTokContentFacade(this))
        appControllerCollection.AddController(NovinkyCZContentfacade(this))
        val filter = appControllerCollection.getIntentFilter(packageName)
        filter.addAction("com.tenshite.inputmacros.printScreen")
        registerReceiver(receiver, filter)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    // list all elements in the view
    private fun listAllElements(node: AccessibilityNodeInfo) {
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            Log.d("AccessibilityService", "Child: $child")
            listAllElements(child)
        }
    }



    override fun onInterrupt() {
        // Handle service interruption
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    fun clickNode(node: cachedNode?) {
        if (node == null) {
            return
        }

        val randomIntInRange = Random.nextInt(5, 15)

        val bounds = node.bounds

        val path = Path().apply {
            moveTo(
                bounds.left + (Random.nextFloat()/2 + 0.25f) * bounds.width(),
                bounds.top + (Random.nextFloat()/2 + 0.25f) * bounds.height()
            )  // Replace x and y with target node coordinates
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, Random.nextLong(110, 160)))
            .build()

        dispatchGesture(gesture, null, null)
    }
}

class cachedNode(node: AccessibilityNodeInfo) {
    val text = node.text
    val contentDescription = node.contentDescription
    val className = node.className
    val viewId = node.viewIdResourceName
    val bounds = Rect()
    val isSelected = node.isSelected
    val isClickable = node.isClickable

    init {
        node.getBoundsInScreen(bounds)
    }
}