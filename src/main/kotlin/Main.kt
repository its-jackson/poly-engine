package org.poly

import org.poly.engine.Game
import org.poly.engine.GameState

private var state: GameState? = null
private var game: Game? = null

fun main() = app()

private fun app() {
    state = GameState(
        title = "Mario",
        width = 1280,
        height = 720
    )

    game = Game(state!!).also {
        it.execute()
    }
}