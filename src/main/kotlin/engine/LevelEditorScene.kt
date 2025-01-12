package org.poly.engine

import java.awt.event.KeyEvent

class LevelEditorScene(
    private val game: Game,
    private val logger: Logger = Logger("Level Editor Scene")
) : Scene(
    game,
    logger,
) {
    private var changingScene = false
    private var timeToChangeScene = TIME_TO_CHANGE_SCENE
    private val fadeRate = 1.0f / TIME_TO_CHANGE_SCENE // Precompute fade rate

    init {

    }

    override fun tick(dt: Float) {
        // Update all game objects
        gameObjects.forEach { it.tick(dt) }

        // Set the changing scene var to true when triggered
        if (!changingScene && KeyListening.isKeyPressed(KeyEvent.VK_SPACE)) {
            changingScene = true
        }

        // Fade to black over 2 seconds before changing scenes
        if (changingScene && timeToChangeScene > 0) {
            fadeToBlack(dt)
        } else if (changingScene) {
            resetScene()
        }
    }

    private fun fadeToBlack(dt: Float) {
        timeToChangeScene -= dt
        game.state.r = (game.state.r - dt * fadeRate).coerceAtLeast(0.0f)
        game.state.g = (game.state.g - dt * fadeRate).coerceAtLeast(0.0f)
        game.state.b = (game.state.b - dt * fadeRate).coerceAtLeast(0.0f)
    }

    private fun resetScene() {
        changingScene = false
        timeToChangeScene = TIME_TO_CHANGE_SCENE
        game.sceneManager.changeScene(Scenes.LEVEL, game)
        game.state.r = 1.0f
        game.state.g = 1.0f
        game.state.b = 1.0f
    }

    companion object {
        private const val TIME_TO_CHANGE_SCENE = 2.0f
    }
}