package com.tenshite.inputmacros.Controllers

import android.os.Bundle
import com.tenshite.inputmacros.MyAccessibilityService

/**
 * Base class for app facades.
 */
abstract class AppControllerBase(service: MyAccessibilityService) {
    // Map of commands
    val commands = HashMap<String, suspend (Bundle?) -> String>()

    // Map of screens
    protected val accessibilityService: MyAccessibilityService = service


    /**
     * Gets type of shown content
     */
    abstract suspend fun getContentType() : ContentType

    //Name of the controller
    abstract val controllerName: String


    /**
     * Executes the intent that was requested from this object
     */
    open suspend fun executeIntent(commandName: String, args: Bundle?) : String {
        return commands[commandName]?.invoke(args) ?: ""
    }
}

enum class ContentType
{
    Unknown,
    Video,
    Image,
    LiveStream,
    Ad
}