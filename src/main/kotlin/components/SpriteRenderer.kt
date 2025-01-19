package org.poly.components

import org.joml.Vector2f
import org.joml.Vector4f
import org.poly.engine.Component

class SpriteRenderer(
    val colour: Vector4f,
) : Component() {
    val texCoords: Array<Vector2f> = arrayOf(
        Vector2f(1f, 1f),
        Vector2f(1f, 0f),
        Vector2f(0f, 1f),
        Vector2f(0f, 0f)
    )

    override fun tick(dt: Float) {
        super.tick(dt)
    }

    override fun start() {
        super.start()
    }
}