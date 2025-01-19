package org.poly.games

import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import org.poly.engine.*

class MarioLifeCycle : GameLifeCycle {
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
        game.logger.info(game.state)
    }
}