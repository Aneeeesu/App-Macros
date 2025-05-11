package com.tenshite.inputmacros

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.PowerManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.tenshite.inputmacros.Controllers.AppControllerCollection
import com.tenshite.inputmacros.Controllers.InstagramController
import com.tenshite.inputmacros.Controllers.NovinkyCZController
import com.tenshite.inputmacros.Controllers.TikTokController
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.random.Random
import kotlinx.coroutines.*
import kotlinx.coroutines.launch
import java.util.Collections

/**
 * A accessibility service handling ADB inputs and performing their requests
 */
class MyAccessibilityService : AccessibilityService() {
    var cachedNodesInWindow = listOf<cachedNode>()
    var currentPackageName : String? = null
    var screenBounds : Rect = Rect()
    val eventFlow = MutableSharedFlow<Unit>()

    /**
     * The receiver for the broadcast intents
     */
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action == "com.tenshite.inputmacros.printScreen"){
                listAllElements(rootInActiveWindow)
            }
            if(intent?.action == "com.tenshite.inputmacros.wakeup"){
                val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                val wakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "MyApp:WakeLock"
                )
                wakeLock.acquire(60 * 1000L) // Keeps screen on for a minute
                try {
                    runBlocking {
                        delay(1000)
                        appControllerCollection.ExecuteIntent(
                            "com.tenshite.inputmacros.TikTok.SwipeDown",
                            null
                        )
                    }
                }
                catch (e: Exception){
                    Log.e("AccessibilityService", "Error executing intent", e)
                }
            }

            try {
                appControllerCollection.ExecuteIntent(intent?.action ?: "", intent?.extras)
            }
            catch (e: Exception){
                Log.e("AccessibilityService", "Error executing intent", e)
            }
        }
    }

    /**
     * The collection of app controllers
     */
    private val appControllerCollection = AppControllerCollection()

    /**
     * Finds the first node with the given text
     * @param node The node to search in
     * @param text The text to search for
     * @return The first node with the given text, or null if not found
     */
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

    /**
     * Prints all content descriptions of the node and its children
     * @param node The node to print content descriptions for
     */
    private fun printAllContentDescriptions(node: AccessibilityNodeInfo){
        Log.d("AdFinderDetails", "${node.text?: "null"} ${node.contentDescription?.toString() ?: "null"}")
        for(i in 0 until node.childCount){
            printAllContentDescriptions(node.getChild(i))
        }
    }

    /**
     * Prepares the service
     */
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        // get all the nodes
        cachedNodesInWindow = Collections.synchronizedList(mutableListOf<cachedNode>())

        // start the coroutine to get the nodes periodically
        GlobalScope.launch(Dispatchers.Default) {
            while(true) {
                val rootNode = rootInActiveWindow
                cachedNodesInWindow =
                    AccessibilityDataExtractor.SelectNodes(rootNode) { true }.map { cachedNode(it) }
                currentPackageName = rootInActiveWindow?.packageName.toString()
                rootInActiveWindow?.getBoundsInScreen(screenBounds)

                eventFlow.emit(Unit)
                delay(200)
            }
        }

        // add all the controllers
        appControllerCollection.AddController(TikTokController(this))
        appControllerCollection.AddController(InstagramController(this))
        appControllerCollection.AddController(NovinkyCZController(this))
        val filter = appControllerCollection.getIntentFilter(packageName)
        filter.addAction("com.tenshite.inputmacros.printScreen")
        filter.addAction("com.tenshite.inputmacros.wakeup")
        //register the receiver
        registerReceiver(
            receiver,
            filter,
            Context.RECEIVER_EXPORTED
        )
    }


    // list all elements in the view
    private fun listAllElements(node: AccessibilityNodeInfo) {
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            Log.d("AccessibilityService", "Child: $child")
            listAllElements(child)
        }
    }



    /**
     * Cleans up the service
     */
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }

    /**
     * Handles accessibility events
     */
    fun clickNode(node: cachedNode?) {
        if (node == null) {
            return
        }

        val bounds = node.bounds

        val path = Path().apply {
            moveTo(
                bounds.left + (Random.nextFloat()/4) * bounds.width()/2,
                bounds.top + (Random.nextFloat()/4) * bounds.height()/2
            )  // Replace x and y with target node coordinates
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, Random.nextLong(110, 160)))
            .build()

        dispatchGesture(gesture, null, null)
    }
}

/**
 * A data class that caches the properties of an AccessibilityNodeInfo object.
 * It allows way faster access to the properties of the node.
 * @param node The AccessibilityNodeInfo object to cache.
 */
class cachedNode(node: AccessibilityNodeInfo) {
    val text = node.text
    val contentDescription = node.contentDescription
    val className = node.className
    val viewId = node.viewIdResourceName
    val bounds = Rect()
    val isSelected = node.isSelected
    val isClickable = node.isClickable
    fun InBounds() : Boolean{
        return bounds.top >= 0 &&  bounds.bottom < 2400 && bounds.left>=0 && bounds.right < 1080
    }

    init {
        node.getBoundsInScreen(bounds)
    }
}