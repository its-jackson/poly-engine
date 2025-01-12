package org.poly.renderer

import org.lwjgl.opengl.GL30.*
import org.poly.components.SpriteRenderer
import org.poly.engine.Game
import org.poly.engine.Startable

class RenderBatch(
    private val game: Game,
    private val maxBatchSize: Int,
    private val shader: Shader = Shader.constructDefault()
) : Startable {
    private val sprites: Array<SpriteRenderer?> = arrayOfNulls(maxBatchSize)

    // 4 vertices quads
    private val vertices: Array<Float?> = arrayOfNulls(maxBatchSize * 4 * VERTEX_SIZE)

    private var numOfSprites = 0
    var hasRoom = true
        private set

    private var vaoID = -1
    private var vboID = -1

    init {
        shader.compileAndLink(game)
    }

    fun addSprite(spriteRenderer: SpriteRenderer) {
        val index = numOfSprites
        if (index >= maxBatchSize) {
            hasRoom = false
            return
        }

        sprites[index] = spriteRenderer
        numOfSprites += 1

        val sprite = sprites[index] ?: return

        var offset = index * 4 * VERTEX_SIZE // (4 vertices per sprite)
        val colour = sprite.colour

        // add vertices props
        var xAdd = 1.0f
        var yAdd = 1.0f

        for (i in 0 until 4) {
            when (i) {
                1 -> yAdd = 0.0f
                2 -> xAdd = 0.0f
                3 -> yAdd = 1.0f
            }

            // get position
            val gameObj = sprite.gameObject ?: return
            val transform = gameObj.transform
            vertices[offset] = transform.position.x + (xAdd * transform.scale.x)
            vertices[offset + 1] = transform.position.y + (yAdd * transform.scale.y)

            // get colour
            vertices[offset + 2] = colour.x
            vertices[offset + 3] = colour.y
            vertices[offset + 4] = colour.z
            vertices[offset + 5] = colour.w

            offset += VERTEX_SIZE
        }
    }

    fun render() { // TODO Looking for a better way of doing this!
        // Rebuffer all data every frame
        glBindBuffer(GL_ARRAY_BUFFER, vboID)
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices.mapNotNull { it }.toFloatArray())

        // Shader gpu usage
        shader.use()
        shader.uploadMatrix4f("uProjection", game.camera.getProjectionMatrix())
        shader.uploadMatrix4f("uView", game.camera.getViewMatrix())

        glBindVertexArray(vaoID)
        glEnableVertexAttribArray(0)
        glEnableVertexAttribArray(1)

        glDrawElements(GL_TRIANGLES, numOfSprites * 6, GL_UNSIGNED_INT, 0)

        glDisableVertexAttribArray(0)
        glDisableVertexAttribArray(1)
        glBindVertexArray(0)
        shader.detach()
    }

    override fun start() {
        // Generate and bind vertex array obj
        vaoID = glGenVertexArrays()
        glBindVertexArray(vaoID)

        // Allocate space for vertices
        vboID = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vboID)
        glBufferData(GL_ARRAY_BUFFER, (vertices.size * Float.SIZE_BYTES).toLong(), GL_DYNAMIC_DRAW)

        // Construct and upload indices buffer
        val eboID = glGenBuffers()
        val indices = generateIndices()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

        // Enable the buffer attribute pointers
        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POS_OFFSET)
        glEnableVertexAttribArray(0)

        glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, COLOR_OFFSET)
        glEnableVertexAttribArray(1)
    }

    private fun generateIndices(): IntArray {
        // 6 indices per quad (3 per triangle)
        val elements: Array<Int?> = arrayOfNulls(maxBatchSize * 6)

        for (i in 0 until maxBatchSize) {
            loadElementIndices(elements, i)
        }

        return elements.mapNotNull { it }
            .toIntArray()
    }

    private fun loadElementIndices(
        elements: Array<Int?>,
        index: Int
    ) {
        val offsetArrayIndex = 6 * index
        val offset = 4 * index

        // 3, 2, 0, 0, 2, 1      7, 6, 4, 4, 6, 5
        // triangle 1
        elements[offsetArrayIndex] = offset + 3
        elements[offsetArrayIndex + 1] = offset + 2
        elements[offsetArrayIndex + 2] = offset + 0
        // triangle 2
        elements[offsetArrayIndex + 3] = offset + 0
        elements[offsetArrayIndex + 4] = offset + 2
        elements[offsetArrayIndex + 5] = offset + 1
//        // triangle 3
//        elements[offsetArrayIndex + 6] = offset + 7
//        elements[offsetArrayIndex + 7] = offset + 6
//        elements[offsetArrayIndex + 8] = offset + 4
//        // triangle 4
//        elements[offsetArrayIndex + 9] = offset + 4
//        elements[offsetArrayIndex + 10] = offset + 6
//        elements[offsetArrayIndex + 11] = offset + 5
    }

    private companion object {
        // Vertex
        // ======
        // Pos                  Colour
        // float, float,        float, float, float, float
        private const val POS_SIZE = 2
        private const val COLOR_SIZE = 4
        private const val POS_OFFSET = 0L
        private const val COLOR_OFFSET = POS_OFFSET + POS_SIZE * Float.SIZE_BYTES
        private const val VERTEX_SIZE = 6
        private const val VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.SIZE_BYTES
    }
}
