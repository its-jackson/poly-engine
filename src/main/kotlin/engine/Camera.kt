package org.poly.engine

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f

class Camera(
    val position: Vector2f = Vector2f(0.0f, 0.0f)
) {
    private val projectionMatrix: Matrix4f = Matrix4f()
    private val viewMatrix: Matrix4f = Matrix4f()

    // TODO Screen dimensions (make this configurable)
    private val screenWidth: Float = 32.0f * 40.0f
    private val screenHeight: Float = 32.0f * 21.0f

    init {
        updateProjectionMatrix()
    }

    /**
     * Updates the projection matrix to match the current screen dimensions.
     */
    fun updateProjectionMatrix() {
        projectionMatrix.identity()
        projectionMatrix.ortho(
            0.0f, screenWidth,
            0.0f, screenHeight,
            0.0f, 100.0f // Adjust the near and far planes as needed
        )
    }

    /**
     * Updates and returns the view matrix based on the current position.
     */
    fun getViewMatrix(): Matrix4f {
        val cameraFront = Vector3f(0.0f, 0.0f, -1.0f)
        val cameraUp = Vector3f(0.0f, 1.0f, 0.0f)

        viewMatrix.identity()
        viewMatrix.lookAt(
            Vector3f(position.x, position.y, 20.0f), // Camera position
            cameraFront.add(position.x, position.y, 0.0f), // Target position
            cameraUp // Up direction
        )

        return viewMatrix
    }

    /**
     * Returns the current projection matrix.
     */
    fun getProjectionMatrix(): Matrix4f {
        return projectionMatrix
    }
}
