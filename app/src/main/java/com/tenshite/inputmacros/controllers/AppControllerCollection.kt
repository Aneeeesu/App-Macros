package com.tenshite.inputmacros.controllers

import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import com.tenshite.inputmacros.facades.AppFacadeBase
import kotlinx.coroutines.*
import kotlinx.coroutines.async

class AppControllerCollection {
    val controllers = HashMap<String, AppFacadeBase>();

    fun AddController(appController: AppFacadeBase){
        controllers[appController.controllerName] = appController
    }

    fun ExecuteIntent(intent: String, args: Bundle?){
        val intentDelimited = intent.split(".");
        if(intentDelimited.count()<4)
            throw Exception("Intent must be in format 'controller.action'");
        Thread {
            var returnValues = ""
             runBlocking {
                returnValues = controllers[intentDelimited[3]]?.executeIntent(intentDelimited[4], args) ?: "";
            }
            if(args?.getString("id") != null)
                Log.d("AppControllerEvent", "${args.getString("id")} $returnValues");
            else
            Log.d("AppControllerEvent", "Intent executed");
        }.start();
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


