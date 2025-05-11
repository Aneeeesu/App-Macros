package com.tenshite.inputmacros

import android.view.accessibility.AccessibilityNodeInfo

/**
 * A utility class for extracting data from accessibility nodes.
 * It provides methods to find the first node that matches a condition and to select all nodes that match a condition.
 */
class AccessibilityDataExtractor{

    companion object {
        /**
         * Finds the first node that matches the given condition.
         * @param node The root node to start searching from.
         * @param condition A lambda function that defines the condition to match.
         * @return The first node that matches the condition, or null if no such node is found.
         */
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
            return null
        }
        /**
         * Selects all nodes that match the given condition.
         * @param node The root node to start searching from.
         * @param condition A lambda function that defines the condition to match.
         * @return A list of nodes that match the condition.
         */
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
            return result
        }
    }
}