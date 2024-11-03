package com.tenshite.inputmacros.controllers

import android.accessibilityservice.AccessibilityService
import android.util.Log

abstract class ControllerBase {
    val commands = HashMap<String,()->Unit>()

    protected val service : AccessibilityService
    constructor(service: AccessibilityService){
        this.service = service

        val map = HashMap<String, String>()
    }
    abstract val controllerName : String


    open fun ExecuteIntent(commandName: String){
        Log.d("AAAAAAAAAAAAAAAAAAAAAAAAAAA",service.rootInActiveWindow.packageName.toString())
    }
}


