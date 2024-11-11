package com.tenshite.inputmacros.facades

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.view.accessibility.AccessibilityNodeInfo
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import com.tenshite.inputmacros.MyAccessibilityService
import kotlin.random.Random

abstract class AppFacadeBase {
    val commands = HashMap<String, (Bundle?) -> Unit>()

    protected val accessibilityService: MyAccessibilityService

    constructor(service: MyAccessibilityService) {
        this.accessibilityService = service

        val map = HashMap<String, String>()
    }

    abstract val controllerName: String


    open fun ExecuteIntent(commandName: String, args: Bundle?) {
        commands[commandName]?.invoke(args)
    }
}


