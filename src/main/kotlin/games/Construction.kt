package org.poly.games

import org.poly.engine.Game
import org.poly.engine.GameLifeCycle
import org.poly.engine.GameState

fun constructGame(
    state: GameState,
    lifeCycle: GameLifeCycle
) = Game(
    state = state,
    lifeCycle = lifeCycle
)

fun constructMarioState() = GameState(
    title = "Mario",
    width = 1280,
    height = 720
)

fun constructMarioLifeCycle() = MarioLifeCycle()