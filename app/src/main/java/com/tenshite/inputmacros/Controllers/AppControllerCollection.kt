package com.tenshite.inputmacros.Controllers

import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.*

/**
 * Class used for resolving specific commands and controllers
 */
class AppControllerCollection {
    //Map of all controllers
    val controllers = HashMap<String, AppControllerBase>()

    /**
     * Add controller to the collection
     * @param appController Controller to add
     */
    fun AddController(appController: AppControllerBase){
        controllers[appController.controllerName] = appController
    }

    /**
     * Finds controller and tells it to execute a command
     * @param intent Intent
     * @return Controller with the given name
     */
    fun ExecuteIntent(intent: String, args: Bundle?){
        val intentDelimited = intent.split(".")
        // check if there are 4 parts in the intent
        if(intentDelimited.count()<4)
            throw Exception("Intent must be in format 'controller.action'")
        // finds the controller, and executes the command on different thread
        Thread {
            var returnValues : String
             runBlocking {
                returnValues = controllers[intentDelimited[3]]?.executeIntent(intentDelimited[4], args) ?: ""
            }
            // if there is an id in the args, after it is completed use it to return value
            if(args?.getString("id") != null)
                Log.d("AppControllerEvent", "${args.getString("id")} $returnValues")
            else
            Log.d("AppControllerEvent", "Intent executed")
        }.start()
    }

    /**
     * Adds all actions to the intent filter
     * @param packageName Package name of the app
     * @return IntentFilter with all actions
     */
    fun getIntentFilter(packageName: String): IntentFilter{
        val filter = IntentFilter()
        for(controller in controllers){
            for(command in controller.value.commands){
                Log.d("IntentFilter", "$packageName.${controller.key}.${command.key}")
                filter.addAction("$packageName.${controller.key}.${command.key}")
            }
        }
        return filter
    }
}


