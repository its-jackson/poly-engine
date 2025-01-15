package org.poly.engine

import org.poly.renderer.Renderer

enum class SceneState {
    LEVEL_EDITOR,
    LEVEL
}

abstract class Scene(
    open val camera: Camera,
    private val game: Game,
    private val logger: Logger
) : Tickable, Startable {
    protected val renderer = Renderer(game)
    private val gameObjects = mutableListOf<GameObject>()

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

    fun tickGameObjects(dt: Float) = gameObjects.forEach {
        it.tick(dt)
    }

    fun addGameObjectToScene(gameObject: GameObject) {
        gameObjects.add(gameObject)

        if (running) {
            gameObject.start()
            renderer.add(gameObject)
        }
    }
}