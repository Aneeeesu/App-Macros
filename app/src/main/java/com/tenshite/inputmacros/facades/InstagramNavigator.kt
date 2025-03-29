package com.tenshite.inputmacros.facades

import com.tenshite.inputmacros.MyAccessibilityService
import kotlinx.coroutines.Deferred

public class InstagramNavigator public constructor(accessibilityService: MyAccessibilityService) :
    AppNavigator(
        accessibilityService = accessibilityService,
        screens = hashMapOf<Int, AppScreen>(
            Screens.Home.ordinal to AppScreen(
                Screens.Home.ordinal, listOf(
                    AppPath(
                        Screens.Home.ordinal,
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == HOME_CONST }) }),
                    AppPath(
                        Screens.Profile.ordinal,
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == PROFILE_CONST }) }),
                    AppPath(
                        Screens.Reels.ordinal,
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == REELS_CONST }) }),
                    AppPath(
                        Screens.Search.ordinal,
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == SEARCH_CONST }) }),
                )
            ),
            Screens.Profile.ordinal to AppScreen(
                Screens.Profile.ordinal, listOf(
                    AppPath(
                        Screens.Home.ordinal,
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == HOME_CONST }) }),
                    AppPath(
                        Screens.Profile.ordinal,
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == PROFILE_CONST }) }),
                    AppPath(
                        Screens.Reels.ordinal,
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == REELS_CONST }) }),
                    AppPath(
                        Screens.Search.ordinal,
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == SEARCH_CONST }) }),
                )
            ),
            Screens.Search.ordinal to AppScreen(
                Screens.Search.ordinal, listOf(
                    AppPath(
                        Screens.Home.ordinal,
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == HOME_CONST }) }),
                    AppPath(
                        Screens.Profile.ordinal,
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == PROFILE_CONST }) }),
                    AppPath(
                        Screens.Reels.ordinal,
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == REELS_CONST }) }),
                    AppPath(
                        Screens.Search.ordinal,
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == SEARCH_CONST }) }),
                )
            ),
            Screens.Reels.ordinal to AppScreen(
                Screens.Reels.ordinal, listOf(
                    AppPath(
                        Screens.Home.ordinal,
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == HOME_CONST }) }),
                    AppPath(
                        Screens.Profile.ordinal,
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == PROFILE_CONST }) }),
                    AppPath(
                        Screens.Reels.ordinal,
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == REELS_CONST }) }),
                    AppPath(
                        Screens.Search.ordinal,
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == SEARCH_CONST }) }),
                )
            ),
        ), appIntent = "com.instagram.android"
    ) {

    companion object {
        public val REELS_CONST = "Reels"
        public val PROFILE_CONST = "Profil"
        public val HOME_CONST = "Domů"
        public val SEARCH_CONST = "Hledat a prozkoumat"
    }

    fun navigateToScreen(screenId: Screens): Deferred<Boolean> {
        return super.navigateToScreen(screenId.ordinal);
    }

    public enum class Screens {
        Home,
        Reels,
        Profile,
        Search,
        Messages,
        LiveStreams,
        Searched,
        ScrollingSearched,
        DMs,
    }

    override suspend fun getCurrentScreen(): AppScreen? {
        val nodes = accessibilityService.cachedNodesInWindow
        if (nodes.firstOrNull { node -> node.className == "android.widget.FrameLayout" && node.contentDescription == HOME_CONST && node.isSelected } != null)
            return screens[Screens.Home.ordinal]!!
        if (nodes.firstOrNull { node -> node.className == "android.widget.FrameLayout" && node.contentDescription == PROFILE_CONST && node.isSelected } != null)
            return screens[Screens.Profile.ordinal]!!
        if (nodes.firstOrNull { node -> node.className == "android.widget.FrameLayout" && node.contentDescription == REELS_CONST && node.isSelected } != null)
            return screens[Screens.Reels.ordinal]!!
        if (nodes.firstOrNull { node -> node.className == "android.widget.FrameLayout" && node.contentDescription == SEARCH_CONST && node.isSelected } != null)
            return screens[Screens.Search.ordinal]!!
        return null;
    }
}