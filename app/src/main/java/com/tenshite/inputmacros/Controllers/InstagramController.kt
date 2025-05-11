package com.tenshite.inputmacros.Controllers

import com.tenshite.inputmacros.MyAccessibilityService
import com.tenshite.inputmacros.Navigators.InstagramNavigator

/**
 * Inherits from ShortFormContentControllerBase and allows controlling the Instagram app.
 * Compared to tiktok, this lacks most of the functionality, but it is still usable for basic interactions
 * @param myAccessibilityService The accessibility service instance used to interact with the OS
 */
class InstagramController(myAccessibilityService: MyAccessibilityService) :
    ShortFormContentControllerBase(myAccessibilityService) {
    override val controllerName = "Instagram"
    val navigator = InstagramNavigator(accessibilityService = accessibilityService)

    override fun getDescription(): String {
        return ""
    }

    init {
        commands["Like"] = { likeVideo(); "" }
        commands["NavigateToHome"] =
            { navigator.navigateToScreen(InstagramNavigator.Screens.Home).await();"" }
        commands["NavigateToProfile"] =
            { navigator.navigateToScreen(InstagramNavigator.Screens.Profile).await();"" }
        commands["NavigateToReels"] =
            { navigator.navigateToScreen(InstagramNavigator.Screens.Reels).await();"" }
        commands["NavigateToSearch"] =
            { navigator.navigateToScreen(InstagramNavigator.Screens.Search).await();"" }
    }

    /**
     * Clicks the like button
     */
    private fun likeVideo() {
        accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node ->
            node.contentDescription != null && node.contentDescription.toString() == InstagramNavigator.AD_CONST && node.bounds.top > 0
        })
    }

    //Gets the current content on the screen
    // As I never got an ad, I don't know what would be there
    override suspend fun getContentType(): ContentType {
        if (navigator.getCurrentScreen()?.screenId != InstagramNavigator.Screens.Home.ordinal || navigator.getCurrentScreen()?.screenId != InstagramNavigator.Screens.ScrollingSearched.ordinal) {
            return ContentType.Unknown
        }

        return ContentType.Video
    }
}
