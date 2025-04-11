package com.tenshite.inputmacros.facades

import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.tenshite.inputmacros.MyAccessibilityService
import kotlinx.coroutines.*


class TikTokContentFacade(accessibilityService: MyAccessibilityService) :
    ShortFormContentFacadeBase(accessibilityService) {
    override val controllerName = "TikTok"
    val navigator = TiktokNavigator(accessibilityService = accessibilityService);

    var lastSearchedItem = ""

    init {
        commands["Like"] = { likeVideo(); "" }
        commands["Comment"] = { Log.d("TiktokClass", "Comment"); "" }
        commands["Share"] = { Log.d("TiktokClass", "Share"); "" }
        commands["Follow"] = { Log.d("TiktokClass", "Follow"); "" }
        commands["Unfollow"] = { Log.d("TiktokClass", "Unfollow"); "" }
        commands["NavigateToHome"] = { navigator.navigateToScreen(TiktokNavigator.Screens.Home).await();"" }
        commands["NavigateToProfile"] = { navigator.navigateToScreen(TiktokNavigator.Screens.Profile).await(); "" }
        commands["NavigateToMessages"] = { navigator.navigateToScreen(TiktokNavigator.Screens.Messages).await();"" }
        commands["NavigateToSearch"] = { navigator.navigateToScreen(TiktokNavigator.Screens.Search).await();"" }
        commands["Search"] = { bundle -> search(bundle);"" }
        commands["OpenDMs"] = { bundle -> openDMs(bundle);"" }
        commands["SendDM"] = { bundle -> sendDM(bundle);"" }
    }

    private suspend fun search(bundle: Bundle?){

        val text = bundle?.getString("query") ?: return;
        val currentScreen = navigator.getCurrentScreen()
        if((lastSearchedItem == text) && (currentScreen != null &&
                    (currentScreen.screenId == TiktokNavigator.Screens.ScrollingSearched.ordinal || currentScreen.screenId == TiktokNavigator.Screens.SearchedLives.ordinal))){
            return
        }
        navigator.navigateToScreen(TiktokNavigator.Screens.Search).await()
        delay(500);
        accessibilityService.clickNode(
            accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.className == "android.widget.EditText" }
        )
        delay(500);
        val arguments = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                bundle?.getString("query")
            )
        }
        delay(500);
        AccessibilityDataExtractor.First(
            accessibilityService.rootInActiveWindow,
            { node -> node.className == "android.widget.EditText" }
        )?.let { info ->
            info.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        }
        val seeked = accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.text?.toString() == "Hledat" }
        delay(500);
        accessibilityService.clickNode(
            seeked
        )
        delay(5000)
        lastSearchedItem = text
        swipeDown(null);
        likeVideo()
        return
    }

    private fun likeVideo() {
        Log.d("TikTOk", "likeVideo: liked vid")
        accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node ->
            node.contentDescription != null && node.contentDescription.contains(
                "ajkov"
            ) && node.bounds.top > 0
        });
    }

    private suspend fun sendDM(bundle: Bundle?) {
        if (bundle?.getString("message") == null) {
            Log.d("TiktokClass", "SendDM: message not provided")
            return
        }
        delay(500);
        accessibilityService.clickNode(
            accessibilityService.cachedNodesInWindow.firstOrNull { node ->
                node.className == "android.widget.EditText"
            })
        delay(500);
        val arguments = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                bundle.getString("message")
            )
        }
        delay(500);
        AccessibilityDataExtractor.First(
            accessibilityService.rootInActiveWindow,
            { node -> node.className == "android.widget.EditText" }
        )?.let { info ->
            info.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        }
        delay(1000);

        val filteredNodes =
            accessibilityService.cachedNodesInWindow.filter { it.className == "android.widget.ImageView" };
        val maxValue = filteredNodes.maxByOrNull { it.bounds.bottom }?.bounds?.bottom ?: 0

        filteredNodes.filter { it.bounds.bottom == maxValue }.maxByOrNull { it.bounds.left }?.let {
            accessibilityService.clickNode(it)
        }
        accessibilityService.performGlobalAction(GLOBAL_ACTION_BACK);
        return
    }


    private suspend fun openDMs(bundle: Bundle?) {
        if (bundle?.getString("username") == null) {
            Log.d("TiktokClass", "OpenDMs: username not provided")
            return
        }
        navigator.navigateToScreen(TiktokNavigator.Screens.Messages).await()
        CoroutineScope(Dispatchers.Default).async {
            val res =
                async { navigator.waitForScreenChange(filter = { runBlocking { navigator.getCurrentScreen()?.screenId == TiktokNavigator.Screens.DMs.ordinal } }) };
            accessibilityService.clickNode(
                accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.InBounds() &&
                    node.text != null && node.text.toString() ==
                        bundle.getString("username")!!

                }
            )
        val result = withTimeout(20000) {
            res.await()
            }
        }.await()
    }

    override fun getDescription(): String {
        val desiredNode = accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.className.toString() == "com.bytedance.tux.input.TuxTextLayoutView" && node.bounds.bottom - node.bounds.top > 0  }

        if(desiredNode != null)
            return desiredNode.text.toString()
        return ""
    }

    override suspend fun getContentType(): ContentType {
        if (navigator.getCurrentScreen()?.screenId != TiktokNavigator.Screens.Home.ordinal && navigator.getCurrentScreen()?.screenId != TiktokNavigator.Screens.ScrollingSearched.ordinal) {
            return ContentType.Unknown
        }
        if(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.text != null && node.text.contains("Reklama") && node.bounds.bottom - node.bounds.top > 0  } != null) {
            return ContentType.Ad
        }

        if(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.text != null && node.text.contains("Foto") && node.bounds.bottom - node.bounds.top > 0 } != null) {
            return ContentType.Image
        }

        if(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.text != null && node.text.contains("LIVE now") && node.bounds.bottom - node.bounds.top > 0 } != null) {
            return ContentType.LiveStream
        }
        return ContentType.Video
    }
}

