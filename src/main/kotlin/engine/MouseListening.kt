package org.poly.engine

import org.lwjgl.glfw.GLFW.GLFW_PRESS
import org.lwjgl.glfw.GLFW.GLFW_RELEASE

object MouseListening {
    private const val MAX_BUTTONS = 3

    var scrollX: Double = 0.0
        private set // Restrict external modification
    var scrollY: Double = 0.0
        private set

    var currentX: Double = 0.0
        private set
    var currentY: Double = 0.0
        private set

    var lastX: Double = 0.0
        private set
    var lastY: Double = 0.0
        private set

    var isDragging = false
        private set

    private val mouseButtonPressed = arrayOfNulls<Boolean>(MAX_BUTTONS)

    val dx: Float
        get() =
            (lastX - currentX).toFloat()
    val dy: Float
        get() =
            (lastY - currentY).toFloat()

    fun cursorPositionCallback(
        window: Long,
        xPos: Double,
        yPos: Double
    ) {
        lastX = currentX
        lastY = currentY

        currentX = xPos
        currentY = yPos

        isDragging = mouseButtonPressed.any { it == true }
    }

    fun mouseButtonCallback(
        window: Long,
        button: Int,
        action: Int,
        mods: Int
    ) {
        // Ensure button pressed is valid (0,1,2)
        if (button >= MAX_BUTTONS) {
            return
        }

        if (action == GLFW_PRESS) {
            mouseButtonPressed[button] = true
        } else if (action == GLFW_RELEASE) {
            mouseButtonPressed[button] = false
            isDragging = false
        }
    }

    fun mouseScrollCallback(
        window: Long,
        xOffset: Double,
        yOffset: Double
    ) {
        scrollX = xOffset
        scrollY = yOffset
    }

    fun endFrame() {
        scrollX = 0.0
        scrollY = 0.0

        lastX = currentX
        lastY = currentY
    }

    fun isMouseButtonDown(button: Int): Boolean {
        // TODO - Need better way of handling
        if (button >= MAX_BUTTONS) {
            return false
        }

        return mouseButtonPressed[button] == true
    }
}