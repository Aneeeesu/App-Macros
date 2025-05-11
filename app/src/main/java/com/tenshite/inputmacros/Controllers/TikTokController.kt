package com.tenshite.inputmacros.Controllers

import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.tenshite.inputmacros.MyAccessibilityService
import com.tenshite.inputmacros.AccessibilityDataExtractor
import com.tenshite.inputmacros.Navigators.TiktokNavigator
import kotlinx.coroutines.*


/**
 * Inherits from ShortFormContentControllerBase and allows controlling the tiktok app.
 * @param accessibilityService The accessibility service instance used to interact with the OS
 */
class TikTokController(accessibilityService: MyAccessibilityService) :
    ShortFormContentControllerBase(accessibilityService) {
        //Controller name for resolving
    override val controllerName = "TikTok"
    // Navigator instance for navigating through the app
    private val navigator = TiktokNavigator(accessibilityService = accessibilityService)

    // Last searched item to prevent searching the same content again and again
    private var lastSearchedItem = ""

    // List of commands in this controller
    init {
        commands["Like"] = { likeVideo(); "" }
        commands["Comment"] = { Log.d("TiktokClass", "Comment"); "" }
        commands["Share"] = { Log.d("TiktokClass", "Share"); "" }
        commands["Follow"] = { Log.d("TiktokClass", "Follow"); "" }
        commands["Unfollow"] = { Log.d("TiktokClass", "Unfollow"); "" }
        commands["NavigateToHome"] =
            { navigator.navigateToScreen(TiktokNavigator.Screens.Home).await();"" }
        commands["NavigateToProfile"] =
            { navigator.navigateToScreen(TiktokNavigator.Screens.Profile).await(); "" }
        commands["NavigateToMessages"] =
            { navigator.navigateToScreen(TiktokNavigator.Screens.Messages).await();"" }
        commands["NavigateToSearch"] =
            { navigator.navigateToScreen(TiktokNavigator.Screens.Search).await();"" }
        commands["Search"] = { bundle -> search(bundle);"" }
        commands["OpenDMs"] = { bundle -> openDMs(bundle);"" }
        commands["SendDM"] = { bundle -> sendDM(bundle);"" }
    }

    /**
     * Searches for a given content in the TikTok app.
     * @param bundle The bundle containing the search query.
     */
    private suspend fun search(bundle: Bundle?) {

        // Check if the query is null or empty
        val text = bundle?.getString("query") ?: return
        val currentScreen = navigator.getCurrentScreen()
        if ((lastSearchedItem == text) && (currentScreen != null &&
                    (currentScreen.screenId == TiktokNavigator.Screens.ScrollingSearched.ordinal || currentScreen.screenId == TiktokNavigator.Screens.SearchedLives.ordinal))
        ) {
            return
        }
        // Navigate to the search screen
        navigator.navigateToScreen(TiktokNavigator.Screens.Search).await()
        delay(500)
        // Click on the search bar
        accessibilityService.clickNode(
            accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.className == "android.widget.EditText" }
        )
        delay(500)
        // Set the text in the search bar
        val arguments = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                bundle.getString("query")
            )
        }
        delay(500)
        // Perform the action to set the text
        AccessibilityDataExtractor.First(
                accessibilityService.rootInActiveWindow,
                { node -> node.className == "android.widget.EditText" }
            )?.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        // Click on the search button
        val seeked =
            accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.text?.toString() == "Hledat" }
        delay(500)
        accessibilityService.clickNode(
            seeked
        )
        delay(5000)
        // Check if the search was successful
        lastSearchedItem = text
        // swipes and clickes on some video
        // it is done like this because I was not able to find specific video nodes
        swipeDown(null)
        likeVideo()
        return
    }

    /**
     * Likes the current video
     */
    private fun likeVideo() {
        Log.d("TikTok", "likeVideo: liked vid")
        // Finds the node with like button and clicks it
        val likeNode =
            accessibilityService.cachedNodesInWindow.firstOrNull { node ->
                node.contentDescription != null && node.contentDescription.contains(
                    TiktokNavigator.LIKE_CONST
                ) && node.bounds.top > 0
            }
        if (likeNode == null) {
            Log.d("TikTok", "likeVideo: like button not found")
            return
        }
        accessibilityService.clickNode(likeNode)
    }

    /**
     * Swipes the screen down by a given distance.
     * @param bundle if no distance is provided, it will swipe down by 200 pixels
     */
    private suspend fun sendDM(bundle: Bundle?) {
        if (bundle?.getString("message") == null) {
            Log.d("TiktokClass", "SendDM: message not provided")
            return
        }
        delay(2000)
        accessibilityService.clickNode(
            accessibilityService.cachedNodesInWindow.firstOrNull { node ->
                node.className == "android.widget.EditText"
            })
        delay(2000)
        val arguments = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                bundle.getString("message")
            )
        }
        delay(2000)
        AccessibilityDataExtractor.First(
                accessibilityService.rootInActiveWindow,
                { node -> node.className == "android.widget.EditText" }
            )?.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        delay(2000)

        val filteredNodes =
            accessibilityService.cachedNodesInWindow.filter { it.className == "android.widget.ImageView" }
        val maxValue = filteredNodes.maxByOrNull { it.bounds.bottom }?.bounds?.bottom ?: 0

        filteredNodes.filter { it.bounds.bottom == maxValue }.maxByOrNull { it.bounds.left }?.let {
            accessibilityService.clickNode(it)
        }
        accessibilityService.performGlobalAction(GLOBAL_ACTION_BACK)
        return
    }

    /**
     * Opens the DMs with a given user.
     * @param bundle Bundle with username
     */
    private suspend fun openDMs(bundle: Bundle?) {
        // Check if the username is null
        if (bundle?.getString("username") == null) {
            Log.d("TiktokClass", "OpenDMs: username not provided")
            return
        }
        // Navigate to the messages screen
        navigator.navigateToScreen(TiktokNavigator.Screens.Messages).await()
        CoroutineScope(Dispatchers.Default).async {
            val res =
                async { navigator.waitForScreenChange(filter = { runBlocking { navigator.getCurrentScreen()?.screenId == TiktokNavigator.Screens.DMs.ordinal } }) }
            // clicks on the user
            accessibilityService.clickNode(
                accessibilityService.cachedNodesInWindow.firstOrNull { node ->
                    node.InBounds() &&
                            node.text != null && node.text.toString() ==
                            bundle.getString("username")!!

                }
            )
            withTimeout(20000) {
                res.await()
            }
        }.await()
    }

    /**
     * Gets the description of the current video
     * @return The description of the current video
     */
    override fun getDescription(): String {
        // Gets description of short form content
        try {
            val desiredNode =
                accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.className.toString() == "com.bytedance.tux.input.TuxTextLayoutView" && node.bounds.bottom - node.bounds.top > 0 }

            if (desiredNode != null)
                return desiredNode.text.toString()
        } catch (e: Exception) {
            return ""
        }
        return ""
    }

    companion object{
        const val PHOTO_CONST = "Foto"
        const val LIVE_CONST = "LIVE now"
        const val AD_CONST = "Reklama"
    }

    /**
     * Returns the current content type of the screen.
     * @return The content type of the screen.
     */
    override suspend fun getContentType(): ContentType {
        // if some other screen return unknown
        if (navigator.getCurrentScreen()?.screenId != TiktokNavigator.Screens.Home.ordinal && navigator.getCurrentScreen()?.screenId != TiktokNavigator.Screens.ScrollingSearched.ordinal) {
            return ContentType.Unknown
        }
        // else find node identyfying the content type
        if (accessibilityService.cachedNodesInWindow.firstOrNull { node ->
                node.text != null && node.text.contains(
                    AD_CONST
                ) && node.bounds.bottom - node.bounds.top > 0
            } != null) {
            return ContentType.Ad
        }

        if (accessibilityService.cachedNodesInWindow.firstOrNull { node ->
                node.text != null && node.text.contains(
                    PHOTO_CONST
                ) && node.bounds.bottom - node.bounds.top > 0
            } != null) {
            return ContentType.Image
        }

        if (accessibilityService.cachedNodesInWindow.firstOrNull { node ->
                node.text != null && node.text.contains(
                    LIVE_CONST
                ) && node.bounds.bottom - node.bounds.top > 0
            } != null) {
            return ContentType.LiveStream
        }
        // there is no node on normal videos
        return ContentType.Video
    }
}

