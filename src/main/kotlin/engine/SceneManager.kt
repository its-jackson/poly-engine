package org.poly.engine

class SceneManager {
    private val logger = Logger("Scene Manager")

    var currentScene: Scene? = null
        private set

    fun changeScene(
        newScene: Scenes,
        game: Game
    ) {
        when (newScene) {
            // TODO Maybe their is a better way instead of instantiating a new obj each time it changes
            Scenes.LEVEL_EDITOR -> {
                currentScene = LevelEditorScene(game)
                currentScene!!.start()
                logger.info("Changed to level editor scene")
            }

            Scenes.LEVEL -> {
                currentScene = LevelScene(game)
                currentScene!!.start()
                logger.info("Changed to level scene")
            }
        }
    }
}
