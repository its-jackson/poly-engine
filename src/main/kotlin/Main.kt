package org.poly

import org.poly.games.constructGame
import org.poly.games.constructMarioLifeCycle
import org.poly.games.constructMarioState

fun main() = playMario()

private fun playMario() {
    val marioState = constructMarioState()
    val marioLifeCycle = constructMarioLifeCycle()
    val marioGame = constructGame(marioState, marioLifeCycle)
    marioGame.execute()
}