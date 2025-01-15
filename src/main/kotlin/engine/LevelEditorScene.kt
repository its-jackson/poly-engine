package org.poly.engine

import org.joml.Vector2f
import org.joml.Vector4f
import org.poly.components.SpriteRenderer
import java.awt.event.KeyEvent

class LevelEditorScene(
    override val camera: Camera,
    private val game: Game,
    private val logger: Logger = Logger("Level Editor Scene")
) : Scene(
    camera,
    game,
    logger,
) {
    private var changingScene = false
    private var timeToChangeScene = TIME_TO_CHANGE_SCENE
    private val fadeRate = 1.0f / TIME_TO_CHANGE_SCENE // Precompute fade rate

    init {
        val xOffset = 10
        val yOffset = 10

        val totalWidth = (600 - xOffset * 2)
        val totalHeight = (300 - yOffset * 2)
        val sizeX = totalWidth / 100.0f
        val sizeY = totalHeight / 100.0f

        // 10,000 iterations
        for (x in 1..100) {
            for (y in 1..100) {
                val xPos = xOffset + (x * sizeX)
                val yPos = yOffset + (y * sizeY)

                GameObject(
                    name = "GameObject=$x, $y",
                    transform = Transform(
                        position = Vector2f(xPos, yPos),
                        scale = Vector2f(sizeX, sizeY)
                    )
                ).also {
                    it.addComponent(
                        SpriteRenderer(
                            Vector4f(
                                xPos / totalWidth,
                                yPos / totalHeight,
                                1f,
                                1f
                            )
                        )
                    )
                }.also {
                    addGameObjectToScene(it)
                }
            }
        }
    }

    override fun tick(dt: Float) {
        tickGameObjects(dt)
        renderer.renderBatches()

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
        game.sceneManager.changeScene(SceneState.LEVEL, game)
        game.state.r = 1.0f
        game.state.g = 1.0f
        game.state.b = 1.0f
    }

    companion object {
        private const val TIME_TO_CHANGE_SCENE = 2.0f
    }
}