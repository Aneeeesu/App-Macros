package com.tenshite.inputmacros.facades

import android.os.Bundle
import com.tenshite.inputmacros.MyAccessibilityService
import kotlinx.coroutines.*

abstract class AppFacadeBase {
    val commands = HashMap<String, suspend (Bundle?) -> String>()

    protected val accessibilityService: MyAccessibilityService

    constructor(service: MyAccessibilityService) {
        this.accessibilityService = service

        val map = HashMap<String, String>()
    }


    abstract suspend fun getContentType() : ContentType;

    abstract val controllerName: String


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
    Text,
    Ad
}