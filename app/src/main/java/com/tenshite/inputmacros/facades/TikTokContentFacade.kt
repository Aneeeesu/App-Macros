package com.tenshite.inputmacros.facades

import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.tenshite.inputmacros.MyAccessibilityService
import kotlinx.coroutines.*


class TikTokContentFacade(accessibilityService: MyAccessibilityService) :
    ShortFormContentFacadeBase(accessibilityService) {
    override val controllerName = "TikTok"
    val navigator = TiktokNavigator(accessibilityService = accessibilityService);

    init {
        commands["Like"] = { likeVideo() }
        commands["Comment"] = { Log.d("TiktokClass", "Comment"); CompletableDeferred(Unit) }
        commands["Share"] = { Log.d("TiktokClass", "Share"); CompletableDeferred(Unit) }
        commands["Follow"] = { Log.d("TiktokClass", "Follow"); CompletableDeferred(Unit) }
        commands["Unfollow"] = { Log.d("TiktokClass", "Unfollow"); CompletableDeferred(Unit) }
        commands["NavigateToHome"] = { navigator.navigateToScreen(Screens.Home).await() }
        commands["NavigateToProfile"] = { navigator.navigateToScreen(Screens.Profile).await() }
        commands["NavigateToSearch"] = { navigator.navigateToScreen(Screens.Search).await() }
        commands["NavigateToMessages"] = { navigator.navigateToScreen(Screens.Messages).await() }
        commands["OpenDMs"] = { bundle -> openDMs(bundle) }
        commands["SendDM"] = { bundle -> sendDM(bundle) }
    }


    private fun likeVideo(): Deferred<Unit> {
        accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node ->
            node.contentDescription != null && node.contentDescription.contains(
                "ajkov"
            )
        });
        return CompletableDeferred(Unit)
    }

    private suspend fun sendDM(bundle: Bundle?) {
        if (bundle?.getString("message") == null) {
            Log.d("TiktokClass", "SendDM: message not provided")
            return
        }
        accessibilityService.clickNode(
            accessibilityService.cachedNodesInWindow.firstOrNull { node ->
                node.className == "android.widget.EditText"
            })

        val arguments = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                bundle.getString("message")
            )
        }
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
        return
    }


    private suspend fun openDMs(bundle: Bundle?) {
        if (bundle?.getString("username") == null) {
            Log.d("TiktokClass", "OpenDMs: username not provided")
            return
        }
        navigator.navigateToScreen(Screens.Messages).await()
        CoroutineScope(Dispatchers.Default).async {
            val res =
                async { navigator.waitForScreenChange(filter = { runBlocking { navigator.getCurrentScreen()?.screenId == Screens.DMs.ordinal } }) };
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

    override suspend fun GetContentType(): ContentType {
        if (navigator.getCurrentScreen()?.screenId != Screens.Home.ordinal) {
            return ContentType.Unknown
        }
        if(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription.contains("Reklama") } != null) {
            return ContentType.Video
        }

        if(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription.contains("Foto") } != null) {
            return ContentType.Image
        }
        return ContentType.Video
    }
}

