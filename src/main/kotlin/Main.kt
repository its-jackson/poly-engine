package org.poly

import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import org.poly.engine.*

private var game: Game? = null

fun main() {
    game = constructMarioGame().also {
        it.execute()
    }
}

private fun constructMarioGame(): Game {
    val state = constructMarioState()
    val lifeCycle = constructMarioLifeCycle()

    return constructGame(
        state = state,
        lifeCycle = lifeCycle
    )
}

private fun constructMarioState() = GameState(
    title = "Mario",
    width = 1280,
    height = 720
)

private fun constructMarioLifeCycle(): GameLifeCycle {
    return object : GameLifeCycle {
        override fun init(game: Game) {
            // Set the games scene to the level editor on start-up
            game.changeScene(SceneState.LEVEL_EDITOR)
        }

        override fun tick(game: Game) {
            if (KeyListening.isKeyPressed(GLFW_KEY_ESCAPE)) {
                game.terminate()
            }
        }

        override fun cleanup(game: Game) {
            //
        }
    }
}

private fun constructGame(
    state: GameState,
    lifeCycle: GameLifeCycle
): Game {
    return Game(
        state = state,
        lifeCycle = lifeCycle
    )
}