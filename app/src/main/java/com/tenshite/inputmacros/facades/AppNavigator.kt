package com.tenshite.inputmacros.facades

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.tenshite.inputmacros.MyAccessibilityService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import java.util.LinkedList
import java.util.Queue
import kotlin.math.log


public enum class Screens {
    Home,
    Profile,
    Search,
    Messages,
}

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
            Screens.Messages.ordinal to AppScreen(
                Screens.Messages.ordinal, listOf(
                    AppPath(Screens.Profile.ordinal,
                        { accessibilityService.clickNode(accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription == "Profil" }) }),
                )
            )


        ), appIntent = "com.zhiliaoapp.musically"
    ) {

    fun navigateToScreen(screenId: Screens): Deferred<Boolean> {
        return super.navigateToScreen(screenId.ordinal);
    }

    override suspend fun getCurrentScreen(): AppScreen? {
        val rootNode = accessibilityService.rootInActiveWindow
        if (accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.className == "android.widget.FrameLayout" && node.contentDescription == "Domů" && node.isSelected } != null)
            return screens[Screens.Home.ordinal]!!
        if (accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.className == "android.widget.FrameLayout" && node.contentDescription == "Profil" && node.isSelected } != null)
            return screens[Screens.Profile.ordinal]!!
        if (accessibilityService.cachedNodesInWindow.firstOrNull { node -> node.contentDescription != null && node.contentDescription.contains("Doručená") && node.isSelected } != null)
            return screens[Screens.Messages.ordinal]!!


        return null;
    }
}

abstract class AppNavigator(
    val accessibilityService: MyAccessibilityService,
    val screens: HashMap<Int, AppScreen>,
    val appIntent: String
) {
    var isNavigating = false;
    var desiredScreen: Int = -1;

    // Collect the flow to wait until both events are triggered
    private suspend fun waitForScreenChange(filter: () -> Boolean) {
        accessibilityService.eventFlow.filter {  filter()
        }.first()
    }

    open fun navigateToScreen(screenId: Int): Deferred<Boolean> =
        CoroutineScope(Dispatchers.Default).async {
            if (isNavigating)
                throw IllegalArgumentException("Navigation in progress")
            isNavigating = true

            //if screen is null open tiktok
            if (accessibilityService.rootInActiveWindow.packageName != appIntent) {
                //open tiktok
                val launchIntent =
                    accessibilityService.packageManager.getLaunchIntentForPackage(appIntent)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    accessibilityService.startActivity(launchIntent);
                    try {
                        delay(1000);
                    } catch (e: TimeoutCancellationException) {
                        isNavigating = false;
                        throw IllegalArgumentException("Navigation timeout")
                    }
                } else {
                    // TikTok app is not installed, handle accordingly
                }
            }


            val currentPos: AppScreen?
            try {
                val result = withTimeout(2000) {
                    currentPos = getCurrentScreen()
                }
            } catch (e: TimeoutCancellationException) {
                isNavigating = false;
                Log.e("AppNavigator", "screan search Navigation timeout")
                throw IllegalArgumentException("Navigation timeout")
            }

            if (currentPos == null) {
                isNavigating = false;
                Log.e("AppNavigator", "currentPos is null")
                throw IllegalArgumentException("Current screen not found")
            }


            val path = findPathToScreen(currentPos, screens[screenId]!!)
            if (path == null) {
                isNavigating = false;
                Log.e("AppNavigator", "path is null")
                throw IllegalArgumentException("Path not found")
            }

            try {
                for (i in 1 until path.size) {
                    val action = path[i - 1].getScreenPath(path[i].screenId)
                        ?: throw IllegalArgumentException("Invalid screen navigationTree")

                    desiredScreen = path[i].screenId;
                    val res =
                        async { waitForScreenChange(filter = { runBlocking { getCurrentScreen()?.screenId == desiredScreen } }) };
                    action.invoke()

                    val result = withTimeout(20000) {
                        res.await();
                    }
                }
            } catch (e: Exception) {
                isNavigating = false;
                Log.e("AppNavigator", e.stackTraceToString())
                throw IllegalArgumentException("Navigation timeout")
            }
            Log.d("AppNavigator", "Navigation completed")
            isNavigating = false;
            return@async true
        }


    abstract suspend fun getCurrentScreen(): AppScreen?;


    private fun createPath(
        endScreen: AppScreen,
        parentMap: Map<Int, AppScreen>,
        startScreen: AppScreen
    ): List<AppScreen> {
        // Reconstruct path from endScreen to startScreen
        val path = mutableListOf<AppScreen>()
        var current: AppScreen? = endScreen

        while (current != null) {
            path.add(current)
            current = parentMap[current.screenId]
        }

        path.reverse() // Reverse to get the path from start to end
        return path
    }


    // BFS algorithm for getting path to next screen
    fun findPathToScreen(startScreen: AppScreen, endScreen: AppScreen): List<AppScreen>? {
        val visited = HashSet<Int>()// Track visited vertices
        val queue: Queue<AppScreen> = LinkedList() // Queue for BFS
        val parentMap = mutableMapOf<Int, AppScreen>() // To track the path

        visited.add(startScreen.screenId);
        queue.add(startScreen) // Enqueue the starting vertex

        while (queue.isNotEmpty()) {
            val currentScreen = queue.poll()!! // Dequeue a vertex from the queue

            // Check if we reached the end screen
            if (currentScreen.screenId == endScreen.screenId) {
                return createPath(currentScreen, parentMap, startScreen)
            }

            // Explore neighbors (paths to other screens)
            for (path in currentScreen.paths) {
                val neighbor = screens[path.screenId]
                    ?: throw IllegalArgumentException("Invalid screen navigationTree")
                if (!visited.contains(neighbor.screenId)) {
                    visited.add(neighbor.screenId) // Mark it as visited
                    queue.add(neighbor) // Enqueue the neighbor
                    parentMap[neighbor.screenId] = currentScreen // Track parent
                }
            }
        }
        return null;
    }
}

class AppPath(val screenId: Int, val navAction: () -> Unit) {
}

class AppScreen(val screenId: Int, val paths: List<AppPath>) {

    fun getScreenPath(screen: Int): (() -> Unit)? {
        return paths.firstOrNull { it.screenId == screen }?.navAction;
    }
}