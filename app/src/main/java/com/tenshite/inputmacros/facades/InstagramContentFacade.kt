package com.tenshite.inputmacros.facades

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.tenshite.inputmacros.MyAccessibilityService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

class InstagramContentFacade(myAccessibilityService: MyAccessibilityService) :
    ShortFormContentFacadeBase(myAccessibilityService) {
    companion object {
        public val REELS_CONST = "Reels"
        public val PROFILE_CONST = "Profil"
        public val HOME_CONST = "Domů"
        public val SEARCH_CONST = "Hledat a prozkoumat"
    }


    override val controllerName = "Instagram"
    val navigator = InstagramNavigator(accessibilityService = accessibilityService);

    var lastSearchedItem = ""

    override fun getDescription(): String {
        return ""
    }

    init {
        commands["Like"] = { likeVideo(); "" }
//            commands["Comment"] = { Log.d("TiktokClass", "Comment"); CompletableDeferred(Unit) }
//            commands["Share"] = { Log.d("TiktokClass", "Share"); CompletableDeferred(Unit) }
//            commands["Follow"] = { Log.d("TiktokClass", "Follow"); CompletableDeferred(Unit) }
//            commands["Unfollow"] = { Log.d("TiktokClass", "Unfollow"); CompletableDeferred(Unit) }
        commands["NavigateToHome"] =
            { navigator.navigateToScreen(InstagramNavigator.Screens.Home).await();"" }
        commands["NavigateToProfile"] =
            { navigator.navigateToScreen(InstagramNavigator.Screens.Profile).await();"" }
        commands["NavigateToReels"] =
            { navigator.navigateToScreen(InstagramNavigator.Screens.Reels).await();"" }
        commands["NavigateToSearch"] =
            { navigator.navigateToScreen(InstagramNavigator.Screens.Search).await();"" }
//            commands["Search"] = { bundle -> search(bundle) }
//            commands["OpenDMs"] = { bundle -> openDMs(bundle) }
//            commands["SendDM"] = { bundle -> sendDM(bundle) }
    }

    private suspend fun search(bundle: Bundle?) {


        val text = bundle?.getString("query") ?: return;
        val currentScreen = navigator.getCurrentScreen()
        if ((lastSearchedItem == text) && (currentScreen != null && currentScreen.screenId == InstagramNavigator.Screens.ScrollingSearched.ordinal)) {
            return
        }
        navigator.navigateToScreen(InstagramNavigator.Screens.Search).await()
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
        val seeked =
            accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.text?.toString() == "Hledat" }
        delay(500);
        accessibilityService.clickNode(
            seeked
        )
        delay(5000)
        likeVideo()
        lastSearchedItem = text
        return
    }

    private fun likeVideo() {
        accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node ->
            node.contentDescription != null && node.contentDescription.toString() == "To se mi líbí" && node.bounds.top > 0
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
        navigator.navigateToScreen(InstagramNavigator.Screens.Messages).await()
        CoroutineScope(Dispatchers.Default).async {
            val res =
                async { navigator.waitForScreenChange(filter = { runBlocking { navigator.getCurrentScreen()?.screenId == InstagramNavigator.Screens.DMs.ordinal } }) };
            accessibilityService.clickNode(
                accessibilityService.cachedNodesInWindow.firstOrNull { node ->
                    node.text != null && node.text.contains(
                        bundle.getString("username")!!
                    )
                }
            )
            val result = withTimeout(20000) {
                res.await()
            }
        }.await()
    }

    override suspend fun getContentType(): ContentType {
        if (navigator.getCurrentScreen()?.screenId != InstagramNavigator.Screens.Home.ordinal || navigator.getCurrentScreen()?.screenId != InstagramNavigator.Screens.ScrollingSearched.ordinal) {
            return ContentType.Unknown
        }
        if (accessibilityService.cachedNodesInWindow.firstOrNull { node ->
                node.text != null && node.text.contains(
                    "Reklama"
                ) && node.bounds.bottom - node.bounds.top > 0
            } != null) {
            return ContentType.Ad
        }

        if (accessibilityService.cachedNodesInWindow.firstOrNull { node ->
                node.text != null && node.text.contains(
                    "Foto"
                ) && node.bounds.bottom - node.bounds.top > 0
            } != null) {
            return ContentType.Image
        }

        if (accessibilityService.cachedNodesInWindow.firstOrNull { node ->
                node.text != null && node.text.contains(
                    "LIVE now"
                ) && node.bounds.bottom - node.bounds.top > 0
            } != null) {
            return ContentType.LiveStream
        }
        return ContentType.Video
    }
}
