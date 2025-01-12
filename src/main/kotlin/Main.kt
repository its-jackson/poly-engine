package org.poly

import org.poly.engine.Game
import org.poly.engine.GameState

fun main() {
    val gameState = GameState(
        title = "Mario by Jackson",
        width = 1280,
        height = 720
    )

    val game = Game(gameState)
    game.execute()
}