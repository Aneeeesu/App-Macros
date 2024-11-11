package com.tenshite.inputmacros.facades

import android.accessibilityservice.AccessibilityService
import android.app.Instrumentation
import android.inputmethodservice.InputMethodService
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.tenshite.inputmacros.MyAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.log

class TikTokContentFacade(accessibilityService: MyAccessibilityService) :
    ShortFormContentFacadeBase(accessibilityService) {
    override val controllerName = "TikTok"
    val navigator = TiktokNavigator(accessibilityService = accessibilityService);

    init {
        commands["Like"] = { LikeVideo() }
        commands["Comment"] = { Log.d("TiktokClass", "Comment") }
        commands["Share"] = { Log.d("TiktokClass", "Share") }
        commands["Follow"] = { Log.d("TiktokClass", "Follow") }
        commands["Unfollow"] = { Log.d("TiktokClass", "Unfollow") }
        commands["NavigateToHome"] = { navigator.navigateToScreen(Screens.Home) }
        commands["NavigateToProfile"] = { navigator.navigateToScreen(Screens.Profile) }
        commands["NavigateToSearch"] = { navigator.navigateToScreen(Screens.Search) }
        commands["NavigateToMessages"] = { navigator.navigateToScreen(Screens.Messages) }
        commands["OpenDMs"] = { bundle -> OpenDMs(bundle) }
        commands["SendDM"] = { bundle -> SendDm(bundle) }
    }


    private fun LikeVideo() {
        accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node ->
            node.contentDescription != null && node.contentDescription.contains(
                "ajkov"
            )
        });
    }

    private fun SendDm(bundle: Bundle?) {
        GlobalScope.launch(Dispatchers.Default) {
            if (bundle?.getString("message") == null) {
                Log.d("TiktokClass", "SendDM: message not provided")
                return@launch
            }
            accessibilityService.clickNode(
                accessibilityService.cachedNodesInWindow.firstOrNull { node ->
                    node.className == "android.widget.EditText"
                })

            val arguments = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, bundle.getString("message"))
            }
            AccessibilityDataExtractor.First(
                accessibilityService.rootInActiveWindow,
                { node -> node.className == "android.widget.EditText" }
            )?.let {
                info -> info.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
            }
            delay(1000);

            val filteredNodes =accessibilityService.cachedNodesInWindow.filter { it.className == "android.widget.ImageView" };
            val maxValue = filteredNodes.maxByOrNull { it.bounds.bottom }?.bounds?.bottom ?: 0

            filteredNodes.filter { it.bounds.bottom == maxValue }.maxByOrNull { it.bounds.left }?.let {
                accessibilityService.clickNode(it)
            }

        }
    }


    private fun OpenDMs(bundle: Bundle?) {
        runBlocking {
            if (bundle?.getString("username") == null) {
                Log.d("TiktokClass", "OpenDMs: username not provided")
                return@runBlocking
            }
            navigator.navigateToScreen(Screens.Messages).await()
            accessibilityService.clickNode(
                accessibilityService.cachedNodesInWindow.firstOrNull { node ->
                    node.text != null && node.text.contains(
                        bundle.getString("username")!!
                    )
                }
            )
        }
    }
}

