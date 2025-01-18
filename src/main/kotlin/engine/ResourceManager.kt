package org.poly.engine

import org.poly.renderer.Shader
import org.poly.renderer.Texture
import java.io.File

class ResourceManager {
    private val shaders = mutableMapOf<String, Shader>()
    private val textures = mutableMapOf<String, Texture>()

    fun findShader(
        resourceName: String,
        game: Game
    ): Shader {
        val file = File(resourceName) // Full file path
        if (shaders.contains(file.absolutePath)) {
            return shaders[file.absolutePath]!!
        }

        val shader = Shader(resourceName).also { it.compileAndLink(game) }
        shaders[file.absolutePath] = shader // Stores the actual ref, not obj
        return shader
    }

    fun findTexture(resourceName: String): Texture {
        val file = File(resourceName) // Full file path
        if (textures.contains(file.absolutePath)) {
            return textures[file.absolutePath]!!
        }

        val texture = Texture(resourceName)
        textures[file.absolutePath] = texture
        return texture
    }
}