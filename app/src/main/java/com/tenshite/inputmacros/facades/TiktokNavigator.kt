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
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == "Profil" }) }),
                    AppPath(Screens.Messages.ordinal,
                        {
                            accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node ->
                                node.contentDescription != null && node.contentDescription.contains(
                                    "Doručená"
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
                                    "Domů"
                                )
                            })
                        }),
                    AppPath(Screens.Messages.ordinal,
                        {
                            accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node ->
                                node.contentDescription != null && node.contentDescription.contains(
                                    "Doručená"
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
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == "Profil"}) },
                    AppPath(Screens.Home.ordinal)
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription.contains("Domů")}) },
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
        if((accessibilityService.rootInActiveWindow.packageName != appIntent))
            return null

        if (nodes.firstOrNull { node -> node.className == "android.widget.FrameLayout" && node.contentDescription == "Domů" && node.isSelected } != null)
            return screens[Screens.Home.ordinal]!!
        if (nodes.firstOrNull { node -> node.className == "android.widget.FrameLayout" && node.contentDescription == "Profil" && node.isSelected } != null)
            return screens[Screens.Profile.ordinal]!!
        if (nodes.firstOrNull { node -> node.contentDescription != null && node.contentDescription.contains("Doručená") && node.isSelected } != null)
            return screens[Screens.Messages.ordinal]!!
        if(nodes.firstOrNull {
            node -> node.text != null && (node.text.toString().contains("Zpráva...") || node.text.toString().contains("Sdílet příspěvek")) ||
                    node.contentDescription != null && node.contentDescription.toString().contains("Poslat")
        } != null)
            return screens[Screens.DMs.ordinal]!!
        if(nodes.firstOrNull { node -> node.text != null && (node.text.toString().contains("Hledat")) } != null)
            return screens[Screens.Search.ordinal]!!
        if(nodes.firstOrNull { node -> node.text != null && (node.text.toString().contains("Nejlepší")) } != null)
            return screens[Screens.Searched.ordinal]!!
        if(nodes.firstOrNull { node -> node.text != null && (node.text.toString().contains("Sledovat") && !node.text.toString().contains("Sledovat už")) } != null)
            return screens[Screens.ScrollingSearched.ordinal]!!
        if(nodes.firstOrNull { node -> node.text != null && (node.text.toString().contains("Hled")) } != null)
            return screens[Screens.ScrollingSearched.ordinal]!!
        if(nodes.firstOrNull { node -> node.text != null && (node.text.toString().contains("Psát...")) } != null)
            return screens[Screens.SearchedLives.ordinal]!!
        return null;
    }
}