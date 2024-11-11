package com.tenshite.inputmacros.facades

import android.view.accessibility.AccessibilityNodeInfo

class AccessibilityDataExtractor{
    companion object {
        fun First(
            node: AccessibilityNodeInfo?,
            condition: (node: AccessibilityNodeInfo) -> Boolean
        ): AccessibilityNodeInfo? {
            if (node == null) return null

            if (condition(node))
                return node

            // Recursively collect child nodes
            for (i in 0 until node.childCount) {
                val result = First(node.getChild(i), condition)
                if (result != null)
                    return result
            }
            return null;
        }

        //className: android.widget.TextView;[a-zA-Z: ; \[\] -_]+longClickable: true;[a-zA-Z: ; \[\] -_]+\n

        //select nodes with condition
        fun SelectNodes(
            node: AccessibilityNodeInfo?,
            condition: (node: AccessibilityNodeInfo) -> Boolean
        ): List<AccessibilityNodeInfo> {
            val result = mutableListOf<AccessibilityNodeInfo>()
            if (node == null) return result

            if (condition(node))
                result.add(node)

            // Recursively collect child nodes
            for (i in 0 until node.childCount) {
                result.addAll(SelectNodes(node.getChild(i), condition))
            }
            return result;
        }
    }
}