package org.poly.renderer

import org.lwjgl.BufferUtils.createIntBuffer
import org.lwjgl.opengl.GL11.*
import org.lwjgl.stb.STBImage.stbi_image_free
import org.lwjgl.stb.STBImage.stbi_load

class Texture(
    val filePath: String
) {
    private var texID: Int = 0

    init {
        // Generate texture
        texID = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, texID)

        // Set texture params and repeat img both directions
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)

        // Pixelate when stretching img
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)

        // Pixelate when shrinking img
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

        val width = createIntBuffer(1)
        val height = createIntBuffer(1)
        val channels = createIntBuffer(1)
        val image = stbi_load(filePath, width, height, channels, 0)

        if (image != null) {
            when {
                channels.get(0) == 3 -> {
                    glTexImage2D(
                        GL_TEXTURE_2D,
                        0,
                        GL_RGB, // No alpha value
                        width.get(0),
                        height.get(0),
                        0,
                        GL_RGB, // No alpha value
                        GL_UNSIGNED_BYTE,
                        image
                    )
                }
                channels.get(0) == 4 -> {
                    glTexImage2D(
                        GL_TEXTURE_2D,
                        0,
                        GL_RGBA,
                        width.get(0),
                        height.get(0),
                        0,
                        GL_RGBA,
                        GL_UNSIGNED_BYTE,
                        image
                    )
                }
            }

            stbi_image_free(image)
        }
    }

    fun bind() {
        glBindTexture(GL_TEXTURE_2D, texID)
    }

    fun unbind() {
        glBindTexture(GL_TEXTURE_2D, 0)
    }
}