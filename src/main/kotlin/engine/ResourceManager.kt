package org.poly.engine

import renderer.Resource
import renderer.Shader
import renderer.Texture
import java.io.File
import kotlin.reflect.KClass

interface ResourceLoader<T : Resource> {
    fun load(path: String, game: Game): T
}

class ShaderLoader : ResourceLoader<Shader> {
    override fun load(path: String, game: Game): Shader {
        val shader = Shader(path)
        shader.compileAndLink(game)
        return shader
    }
}

class TextureLoader : ResourceLoader<Texture> {
    override fun load(path: String, game: Game): Texture {
        return Texture(path)
    }
}

class ResourceManager {
    private val resources = mutableMapOf<String, Resource>()
    private val loaders = mutableMapOf<KClass<out Resource>, ResourceLoader<out Resource>>()

    init {
        loaders[Shader::class] = ShaderLoader()
        loaders[Texture::class] = TextureLoader()
    }

    fun freeResources() {
        resources.forEach { it.value.dispose() }
        resources.clear()
    }

    fun freeLoaders() {
        // Doesn't hold onto any external resources that need explicit cleanup
        loaders.clear()
    }

    fun <T : Resource> destroyResource(path: String): Resource? {
        val fullPath = File(path).absolutePath
        val resource = resources.remove(fullPath) ?: return null
        resource.dispose()
        return resource
    }

    fun <T : Resource> forceReload(
        path: String,
        type: KClass<T>,
        game: Game
    ): T {
        val fullPath = File(path).absolutePath
        // Remove and dispose existing resource if any
        resources.remove(fullPath)?.dispose()
        // Now load a fresh one
        return load(fullPath, type, game)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Resource> getOrLoad(
        path: String,
        type: KClass<T>,
        game: Game
    ): T {
        // If we have it, return it
        val fullPath = File(path).absolutePath
        if (resources.containsKey(fullPath)) {
            return resources[fullPath] as T
        }
        // Otherwise, load it
        return load(fullPath, type, game)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T: Resource> load(
        fullPath: String,
        type: KClass<T>,
        game: Game
    ): T {
        val loader = loaders[type] as? ResourceLoader<T> ?: error("No loader found for resource type $type")
        val resource = loader.load(fullPath, game)
        resources[fullPath] = resource
        return resource
    }
}