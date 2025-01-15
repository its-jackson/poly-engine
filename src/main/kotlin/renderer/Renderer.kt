package org.poly.renderer

import org.poly.components.SpriteRenderer
import org.poly.engine.Game
import org.poly.engine.GameObject

class Renderer(
    private val game: Game,
    private val maxBatchSize: Int = 1000
) {
    private val batches = mutableListOf<RenderBatch>()

    fun renderBatches() {
        batches.forEach {
            it.render()
        }
    }

    fun add(gameObject: GameObject) {
        val sprite = gameObject.getComponent(SpriteRenderer::class.java)
            .getOrNull()
            ?: return

        add(sprite)
    }

    private fun add(sprite: SpriteRenderer) {
        var added = false

        for (batch in batches) {
            if (batch.hasRoom) {
                batch.addSprite(sprite)
                added = true
                break
            }
        }

        // Make new batch and add sprite if all batches full
        if (!added) {
            val newBatch = RenderBatch(game, maxBatchSize).also { it.start() }
            batches.add(newBatch)
            newBatch.addSprite(sprite)
        }
    }
}