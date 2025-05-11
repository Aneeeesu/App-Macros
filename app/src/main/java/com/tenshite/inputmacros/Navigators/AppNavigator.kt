package com.tenshite.inputmacros.Navigators

import android.content.Intent
import android.util.Log
import com.tenshite.inputmacros.MyAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.util.LinkedList
import java.util.Queue


/**
 * Base class for app navigators
 */
abstract class AppNavigator(
    val accessibilityService: MyAccessibilityService,
    val screens: HashMap<Int, AppScreen>,
    val appIntent: String
) {
    // if naviagtion is running
    var isNavigating = false
    // desired screen id
    var desiredScreen: Int = -1

    /**
     * Allows other code await some screen change
     */
    suspend fun waitForScreenChange(filter: () -> Boolean) {
        accessibilityService.eventFlow.filter {
            filter()
        }.first()
    }

    /**
     * Navigate to some screen
     * @param screenId id of screen to navigate to
     * @return returns deffered if it succeeded or not
     */
    open fun navigateToScreen(screenId: Int): Deferred<Boolean> =
        CoroutineScope(Dispatchers.Default).async {
            try {
                // check if already navigating
                if (isNavigating)
                    throw IllegalArgumentException("Navigation in progress")
                isNavigating = true

                //if screen is null open app
                if (accessibilityService.currentPackageName != appIntent) {
                    //open taget app
                    val launchIntent =
                        accessibilityService.packageManager.getLaunchIntentForPackage(appIntent)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        accessibilityService.startActivity(launchIntent)
                        try {
                            delay(10000)
                        } catch (e: TimeoutCancellationException) {
                            isNavigating = false
                            throw IllegalArgumentException("Navigation timeout")
                        }
                    } else {
                        // TikTok app is not installed, handle accordingly
                    }
                }

                // get current screen
                var currentPos: AppScreen? = getCurrentScreen()

                // use bfs to navigate to target screen through the map
                try {
                    while (currentPos != screens[screenId]!!) {
                        try {
                            withTimeout(2000) {
                                currentPos = getCurrentScreen()
                            }
                        } catch (e: TimeoutCancellationException) {
                            isNavigating = false
                            Log.e("AppNavigator", "screan search Navigation timeout")
                            throw IllegalArgumentException("Navigation timeout")
                        }
                        //retry if lost
                        var retryCounter = 0
                        while (currentPos == null) {
                            currentPos = getCurrentScreen()
                            Log.e("AppNavigator", "currentPos is null")
                            if (retryCounter > 15) {
                                isNavigating = false
                                throw IllegalArgumentException("Current screen not found")
                            }
                            delay(1000)
                            retryCounter++
                        }
                        // if current pos is already there leave loop
                        if(currentPos?.screenId == screenId)
                            break

                        //find the path
                        val path = findPathToScreen(currentPos!!, screens[screenId]!!)
                        if (path == null) {
                            isNavigating = false
                            Log.e("AppNavigator", "path is null")
                            throw IllegalArgumentException("Path not found")
                        }

                        //get action to get to next screen
                        val action = path[0].getScreenPath(path[1].screenId)
                            ?: throw IllegalArgumentException("Invalid screen navigationTree")

                        desiredScreen = path[1].screenId
                        val res =
                            async {
                                //wait for the screen to change
                                waitForScreenChange(filter = { runBlocking { getCurrentScreen()?.screenId == desiredScreen } })
                            }
                        action.invoke()
                        try {
                            withTimeout(5000) {
                                res.await()
                            }
                        } catch (e: Exception) {
                            continue
                        }
                    }
                } catch (e: Exception) {
                    isNavigating = false
                    Log.e("AppNavigator", e.stackTraceToString())
                    throw IllegalArgumentException("Navigation timeout")
                }
                Log.d("AppNavigator", "Navigation completed")
                isNavigating = false
                return@async true
            } catch (e: Exception) {
                Log.e("AppNavigator", e.stackTraceToString())
                isNavigating = false
                return@async false
            }
        }


    /**
     * gets current screen
     */
    abstract suspend fun getCurrentScreen(): AppScreen?


    /**
     * Uses BFS to create a path from one screen to another
     * @param endScreen The screen where the path ends.
     * @param parentMap A map of screen IDs to their parent screens.
     * @return A list of screens representing the path from startScreen to endScreen.
     * */
    private fun createPath(
        endScreen: AppScreen,
        parentMap: Map<Int, AppScreen>,
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


    /**
     * Uses BFS to find a path from startScreen to endScreen
     * @param startScreen The screen where the path starts.
     * @param endScreen The screen where the path ends.
     * @return A list of screens representing the path from startScreen to endScreen, or null if no path exists.
     */
    fun findPathToScreen(startScreen: AppScreen, endScreen: AppScreen): List<AppScreen>? {
        val visited = HashSet<Int>()// Track visited vertices
        val queue: Queue<AppScreen> = LinkedList() // Queue for BFS
        val parentMap = mutableMapOf<Int, AppScreen>() // To track the path

        visited.add(startScreen.screenId)
        queue.add(startScreen) // Enqueue the starting vertex

        while (queue.isNotEmpty()) {
            val currentScreen = queue.poll()!! // Dequeue a vertex from the queue

            // Check if we reached the end screen
            if (currentScreen.screenId == endScreen.screenId) {
                return createPath(currentScreen, parentMap)
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
        return null
    }
}

/**
 * Represents a path in the app navigation tree.
 * @param screenId The ID of the screen associated with this path.
 * @param navAction The action to perform when navigating to this screen.
 */
class AppPath(val screenId: Int, val navAction: () -> Unit) {
}

/**
 * Represents a screen in the app navigation tree.
 * @param screenId The ID of the screen.
 * @param paths A list of paths leading to other screens from this screen.
 */
class AppScreen(val screenId: Int, val paths: List<AppPath>) {

    fun getScreenPath(screen: Int): (() -> Unit)? {
        return paths.firstOrNull { it.screenId == screen }?.navAction
    }
}