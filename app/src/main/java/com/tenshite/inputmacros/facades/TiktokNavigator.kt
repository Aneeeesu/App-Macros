package com.tenshite.inputmacros.facades

import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
import com.tenshite.inputmacros.MyAccessibilityService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

public class TiktokNavigator public constructor(accessibilityService: MyAccessibilityService) :
    AppNavigator(
        accessibilityService = accessibilityService,
        screens = hashMapOf<Int, AppScreen>(
            Screens.Home.ordinal to AppScreen(
                Screens.Home.ordinal, listOf(
                    AppPath(Screens.Profile.ordinal,
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == PROFILE_CONST }) }),
                    AppPath(Screens.Messages.ordinal,
                        {
                            accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node ->
                                node.contentDescription != null && node.contentDescription.contains(
                                    MESSAGES_CONST
                                )
                            })
                        }),
                    AppPath(Screens.Search.ordinal,
                        {
                            val filteredNodes =
                                accessibilityService.cachedNodesInWindow.filter { it.className == "android.widget.ImageView" && it.bounds.top > 0 && it.bounds.right > 0};
                            val maxValue = filteredNodes.minByOrNull { it.bounds.bottom }?.bounds?.bottom ?: 0

                            filteredNodes.filter { it.bounds.bottom == maxValue }.maxByOrNull { it.bounds.left }?.let {
                                accessibilityService.clickNode(it)
                            }
                        }),
                )
            ),
            Screens.Profile.ordinal to AppScreen(
                Screens.Profile.ordinal, listOf(
                    AppPath(Screens.Home.ordinal,
                        {
                            accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node ->
                                node.contentDescription != null && node.contentDescription.contains(
                                    HOME_CONST
                                )
                            })
                        }),
                    AppPath(Screens.Messages.ordinal,
                        {
                            accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node ->
                                node.contentDescription != null && node.contentDescription.contains(
                                    MESSAGES_CONST
                                )
                            })
                        }),
                )
            ),
            Screens.Search.ordinal to AppScreen(
                Screens.Search.ordinal, listOf(
                    AppPath(Screens.Home.ordinal)
                        { accessibilityService.performGlobalAction(GLOBAL_ACTION_BACK);runBlocking { delay(300)} ; accessibilityService.performGlobalAction(GLOBAL_ACTION_BACK); },
                )
            ),

            Screens.Searched.ordinal to AppScreen(
                Screens.Searched.ordinal, listOf(
                    AppPath(Screens.Search.ordinal)
                    { accessibilityService.performGlobalAction(GLOBAL_ACTION_BACK); },
                )
            ),

            Screens.Messages.ordinal to AppScreen(
                Screens.Messages.ordinal, listOf(
                    AppPath(Screens.Profile.ordinal)
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == PROFILE_CONST}) },
                    AppPath(Screens.Home.ordinal)
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription.contains(HOME_CONST)}) },
                )
            ),
            Screens.DMs.ordinal to AppScreen(
                Screens.DMs.ordinal, listOf(
                    AppPath(Screens.Messages.ordinal)
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.className == "android.widget.Button" }) },
                )
            ),
            Screens.ScrollingSearched.ordinal to AppScreen(
                Screens.ScrollingSearched.ordinal, listOf(
                    AppPath(Screens.Searched.ordinal)
                    { accessibilityService.performGlobalAction(GLOBAL_ACTION_BACK); },
                )
            ),
            Screens.SearchedLives.ordinal to AppScreen(
                Screens.SearchedLives.ordinal, listOf(
                    AppPath(Screens.Searched.ordinal)
                    { accessibilityService.performGlobalAction(GLOBAL_ACTION_BACK); },
                )
            )
        ), appIntent = "com.zhiliaoapp.musically"
    ) {

    companion object {
        val PROFILE_CONST = "Profil"
        val HOME_CONST = "Domů"
        val SEARCH_CONST = "Hledat"
        val SEARCHED_CONST = "Nejlepší"
        val MESSAGES_CONST = "Doručená"
        val IN_DM_MESSAGE_CONST = "Zpráva..."
        val IN_DM_SHARE_CONST = "Sdílet příspěvek"
        val IN_DM_SEND_CONST = "Poslat"
        val SROLLING_SEACH_CONST = "Sledovat"
        val SCROLLING_SEARCH_ALTERNATIVE_CONST = "Sledovat už"
        val SCROLLING_SEARCH_ALTERNATIVE2_CONST = "Hled"
        val SEACHED_LIVES_CONST = "Psát..."
        val LIKE_CONST = "ajkov"
    }


    fun navigateToScreen(screenId: Screens): Deferred<Boolean> {
        return super.navigateToScreen(screenId.ordinal);
    }

    public enum class Screens {
        None,
        Home,
        Profile,
        Search,
        Messages,
        LiveStreams,
        Searched,
        ScrollingSearched,
        SearchedLives,
        DMs,
    }

    override suspend fun getCurrentScreen(): AppScreen? {
        val nodes = accessibilityService.cachedNodesInWindow
        if((accessibilityService.currentPackageName != appIntent))
            return null

        if (nodes.firstOrNull { node -> node.className == "android.widget.FrameLayout" && node.contentDescription == HOME_CONST && node.isSelected } != null)
            return screens[Screens.Home.ordinal]!!
        if (nodes.firstOrNull { node -> node.className == "android.widget.FrameLayout" && node.contentDescription == PROFILE_CONST && node.isSelected } != null)
            return screens[Screens.Profile.ordinal]!!
        if (nodes.firstOrNull { node -> node.contentDescription != null && node.contentDescription.contains(SEARCHED_CONST) && node.isSelected } != null)
            return screens[Screens.Messages.ordinal]!!
        if(nodes.firstOrNull {
            node -> node.text != null && (node.text.toString().contains(IN_DM_MESSAGE_CONST) || node.text.toString().contains(IN_DM_SHARE_CONST)) ||
                    node.contentDescription != null && node.contentDescription.toString().contains(IN_DM_SEND_CONST)
        } != null)
            return screens[Screens.DMs.ordinal]!!
        if(nodes.firstOrNull { node -> node.text != null && (node.text.toString().contains(SEARCH_CONST)) } != null)
            return screens[Screens.Search.ordinal]!!
        if(nodes.firstOrNull { node -> node.text != null && (node.text.toString().contains(SEARCHED_CONST)) } != null)
            return screens[Screens.Searched.ordinal]!!
        if(nodes.firstOrNull { node -> node.text != null && (node.text.toString().contains(SROLLING_SEACH_CONST) && !node.text.toString().contains(SCROLLING_SEARCH_ALTERNATIVE_CONST)) } != null)
            return screens[Screens.ScrollingSearched.ordinal]!!
        if(nodes.firstOrNull { node -> node.text != null && (node.text.toString().contains(SCROLLING_SEARCH_ALTERNATIVE2_CONST)) } != null)
            return screens[Screens.ScrollingSearched.ordinal]!!
        if(nodes.firstOrNull { node -> node.text != null && (node.text.toString().contains(SEACHED_LIVES_CONST)) } != null)
            return screens[Screens.SearchedLives.ordinal]!!
        return null;
    }
}