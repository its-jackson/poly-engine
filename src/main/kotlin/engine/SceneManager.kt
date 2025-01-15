package org.poly.engine

class SceneManager {
    private val logger = Logger("Scene Manager")

    var activeScene: Scene? = null
        private set

    var state: SceneState? = null
        private set

    fun reset() {
        activeScene = null
        state = null

        logger.info("Reset: activeScene=$activeScene, state=$state")
    }

    fun changeScene(
        sceneState: SceneState,
        game: Game
    ) {
        logger.info("Switched to: $sceneState, from $state")

        state = sceneState
        activeScene = when (sceneState) {
            SceneState.LEVEL_EDITOR -> LevelEditorScene(
                game = game,
                camera = Camera()
            )

            SceneState.LEVEL -> LevelScene(
                game = game,
                camera = Camera()
            )
        }.also {
            it.start()
        }
    }
}
