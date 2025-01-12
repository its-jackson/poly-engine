package org.poly.engine

import org.lwjgl.glfw.GLFW.*

object KeyListening {
    private const val MAX_KEYS = 350

    private val keyPressed = arrayOfNulls<Boolean>(MAX_KEYS)

    fun keyCallback(
        window: Long,
        key: Int,
        scancode: Int,
        action: Int,
        mods: Int
    ) {
        if (key >= MAX_KEYS) {
            return
        }

        if (action == GLFW_PRESS) {
            keyPressed[key] = true
        } else if (action == GLFW_RELEASE) {
            keyPressed[key] = false
        }
    }

    fun isKeyPressed(key: Int): Boolean {
        return key < MAX_KEYS && keyPressed[key] == true
    }
}