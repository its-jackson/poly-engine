package org.poly.engine

import org.poly.renderer.Renderer

enum class Scenes {
    LEVEL_EDITOR,
    LEVEL
}

abstract class Scene(
    private val game: Game,
    private val logger: Logger
) : Tickable, Startable {
    val renderer = Renderer(game)
    val gameObjects = mutableListOf<GameObject>()

    var running = false
        private set

    init {
        
    }

    override fun start() {
        gameObjects.forEach {
            it.start()
            renderer.add(it)
        }

        running = true
    }

    fun addGameObjectToScene(gameObject: GameObject) {
        gameObjects.add(gameObject)

        if (running) {
            gameObject.start()
            renderer.add(gameObject)
        }
    }
}