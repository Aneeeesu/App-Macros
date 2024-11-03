package com.tenshite.inputmacros.controllers

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.IntentFilter
import android.graphics.Path
import android.graphics.Rect
import android.util.Log

class AppControllerCollection {
    val controllers = HashMap<String, ControllerBase>();

    fun AddController(appController: ControllerBase){
        controllers[appController.controllerName] = appController
    }

    fun ExecuteIntent(intent: String){
        val intentDelimited = intent.split(".");
        if(intentDelimited.count()>1)
            throw Exception("Intent must be in format 'controller.action'");
    }

    fun getIntentFilter(packageName: String): IntentFilter{
        val filter = IntentFilter();
        for(controller in controllers){
            for(command in controller.value.commands){
                Log.d("IntentFilter", "$packageName.${controller.key}.${command.key}");
                filter.addAction("$packageName.${controller.key}.${command.key}");
            }
        }
        return filter;
    }
}


