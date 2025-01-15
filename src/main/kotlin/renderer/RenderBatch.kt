package org.poly.renderer

import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil
import org.poly.components.SpriteRenderer
import org.poly.engine.Game
import org.poly.engine.Startable
import java.util.*

class RenderBatch(
    private val game: Game,
    private val maxBatchSize: Int,
    private val shader: Shader = Shader.constructDefault()
) : Startable {
    private var vaoID = -1
    private var vboID = -1
    private var numOfSprites = 0

    private val vertices = FloatArray(maxBatchSize * 4 * VERTEX_SIZE) // 4 vertices quads
    private val sprites = arrayOfNulls<SpriteRenderer>(maxBatchSize)

    var hasRoom = true
        private set

    init {
        shader.compileAndLink(game)
    }

    fun addSprite(spriteRenderer: SpriteRenderer) {
        val index = numOfSprites
        sprites[index] = spriteRenderer
        numOfSprites += 1

        val sprite = sprites[index] ?: return
        var offset = index * 4 * VERTEX_SIZE // (4 vertices per sprite)
        val colour = sprite.colour

        val xOffsets = arrayOf(1.0f, 1.0f, 0.0f, 0.0f)
        val yOffsets = arrayOf(1.0f, 0.0f, 0.0f, 1.0f)

        for (i in 0 until 4) {
            val gameObj = sprite.gameObject ?: return
            val transform = gameObj.transform

            vertices[offset + 0] = transform.position.x + (xOffsets[i] * transform.scale.x) // x
            vertices[offset + 1] = transform.position.y + (yOffsets[i] * transform.scale.y) // y

            // get colour
            vertices[offset + 2] = colour.x
            vertices[offset + 3] = colour.y
            vertices[offset + 4] = colour.z
            vertices[offset + 5] = colour.w

            offset += VERTEX_SIZE
        }

        if (numOfSprites >= maxBatchSize) {
            hasRoom = false
        }
    }

    fun render() {
        // 1) Build a FloatBuffer for only the used portion
        val usedFloats = numOfSprites * 4 * VERTEX_SIZE
        val vertexBuffer = MemoryUtil.memAllocFloat(usedFloats)
        vertexBuffer.put(vertices, 0, usedFloats)
        vertexBuffer.flip()

        // 2) Upload only the used portion
        glBindBuffer(GL_ARRAY_BUFFER, vboID)
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer)

        MemoryUtil.memFree(vertexBuffer)

        // Shader gpu usage
        val projMatrix = game.activeScene?.camera?.getProjectionMatrix() ?: return
        val viewMatrix = game.activeScene?.camera?.getViewMatrix() ?: return

        shader.use()
        shader.uploadMatrix4f("uProjection", projMatrix)
        shader.uploadMatrix4f("uView", viewMatrix)

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
        val elements = IntArray(maxBatchSize * 6)

        for (i in 0 until maxBatchSize) {
            loadElementIndices(elements, i)
        }

        return elements
    }

    private fun loadElementIndices(
        elements: IntArray,
        index: Int
    ) {
        // 3, 2, 0, 0, 2, 1      7, 6, 4, 4, 6, 5
        val offsetArrayIndex = 6 * index
        val offset = 4 * index

        // triangle 1
        elements[offsetArrayIndex + 0] = offset + 3
        elements[offsetArrayIndex + 1] = offset + 2
        elements[offsetArrayIndex + 2] = offset + 0

        // triangle 2
        elements[offsetArrayIndex + 3] = offset + 0
        elements[offsetArrayIndex + 4] = offset + 2
        elements[offsetArrayIndex + 5] = offset + 1
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
