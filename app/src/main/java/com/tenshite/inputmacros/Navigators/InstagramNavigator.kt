package com.tenshite.inputmacros.Navigators

import com.tenshite.inputmacros.MyAccessibilityService
import kotlinx.coroutines.Deferred

/**
 * Provides navigation for Instagram controller.
 * @param accessibilityService The accessibility service used for interacting with the OS.
 */
class InstagramNavigator(accessibilityService: MyAccessibilityService) :
    AppNavigator(
        // Pass the accessibility service to the parent class
        accessibilityService = accessibilityService,
        // HashMap of screens with their respective paths
        screens = hashMapOf<Int, AppScreen>(
            Screens.Home.ordinal to AppScreen(
                Screens.Home.ordinal, listOf(
                    AppPath(
                        Screens.Home.ordinal
                    ) { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == HOME_CONST }) },
                    AppPath(
                        Screens.Profile.ordinal
                    ) { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == PROFILE_CONST }) },
                    AppPath(
                        Screens.Reels.ordinal
                    ) { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == REELS_CONST }) },
                    AppPath(
                        Screens.Search.ordinal
                    ) { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == SEARCH_CONST }) },
                )
            ),
            Screens.Profile.ordinal to AppScreen(
                Screens.Profile.ordinal, listOf(
                    AppPath(
                        Screens.Home.ordinal
                    ) { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == HOME_CONST }) },
                    AppPath(
                        Screens.Profile.ordinal
                    ) { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == PROFILE_CONST }) },
                    AppPath(
                        Screens.Reels.ordinal
                    ) { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == REELS_CONST }) },
                    AppPath(
                        Screens.Search.ordinal
                    ) { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == SEARCH_CONST }) },
                )
            ),
            Screens.Search.ordinal to AppScreen(
                Screens.Search.ordinal, listOf(
                    AppPath(
                        Screens.Home.ordinal
                    ) { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == HOME_CONST }) },
                    AppPath(
                        Screens.Profile.ordinal
                    ) { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == PROFILE_CONST }) },
                    AppPath(
                        Screens.Reels.ordinal
                    ) { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == REELS_CONST }) },
                    AppPath(
                        Screens.Search.ordinal
                    ) { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == SEARCH_CONST }) },
                )
            ),
            Screens.Reels.ordinal to AppScreen(
                Screens.Reels.ordinal, listOf(
                    AppPath(
                        Screens.Home.ordinal
                    ) { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == HOME_CONST }) },
                    AppPath(
                        Screens.Profile.ordinal
                    ) { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == PROFILE_CONST }) },
                    AppPath(
                        Screens.Reels.ordinal
                    ) { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == REELS_CONST }) },
                    AppPath(
                        Screens.Search.ordinal
                    ) { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == SEARCH_CONST }) },
                )
            ),
        ), appIntent = "com.instagram.android"
    ) {
    //string constants
    companion object {
        const val AD_CONST = "To se mi líbí"
        const val REELS_CONST = "Reels"
        const val PROFILE_CONST = "Profil"
        const val HOME_CONST = "Domů"
        const val SEARCH_CONST = "Hledat a prozkoumat"
    }

    /**
     * Navigates to the specified screen.
     * @param screenId The ID of the screen to navigate to.
     * @return A Deferred object that resolves to true if the navigation was successful, false otherwise.
     */
    fun navigateToScreen(screenId: Screens): Deferred<Boolean> {
        return super.navigateToScreen(screenId.ordinal)
    }

    enum class Screens {
        Home,
        Reels,
        Profile,
        Search,
        ScrollingSearched,
    }


    /**
     * Returns the current screen of the app.
     * @return The current screen of the app, or null if not found.
     */
    override suspend fun getCurrentScreen(): AppScreen? {
        //try finding nodes containing element that is only on that screen
        val nodes = accessibilityService.cachedNodesInWindow
        //checks for home
        if (nodes.firstOrNull { node -> node.className == "android.widget.FrameLayout" && node.contentDescription == HOME_CONST && node.isSelected } != null)
            return screens[Screens.Home.ordinal]!!
        //checks for profile
        if (nodes.firstOrNull { node -> node.className == "android.widget.FrameLayout" && node.contentDescription == PROFILE_CONST && node.isSelected } != null)
            return screens[Screens.Profile.ordinal]!!
        //checks for reels
        if (nodes.firstOrNull { node -> node.className == "android.widget.FrameLayout" && node.contentDescription == REELS_CONST && node.isSelected } != null)
            return screens[Screens.Reels.ordinal]!!
        //checks for search
        if (nodes.firstOrNull { node -> node.className == "android.widget.FrameLayout" && node.contentDescription == SEARCH_CONST && node.isSelected } != null)
            return screens[Screens.Search.ordinal]!!
        // unknown screen
        return null
    }
}