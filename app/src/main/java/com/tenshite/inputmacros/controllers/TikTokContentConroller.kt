package com.tenshite.inputmacros.controllers

import android.accessibilityservice.AccessibilityService

class TikTokContentConroller: ShortFormContentControllerBase {
    override val controllerName = "TikTok"
    constructor(accessibilityService: AccessibilityService) : super(accessibilityService){
    }
}


