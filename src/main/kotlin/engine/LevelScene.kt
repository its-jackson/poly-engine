package org.poly.engine

class LevelScene(
    private val game: Game,
    private val logger: Logger = Logger("Level Scene")
) : Scene(
    game,
    logger
) {
    override fun tick(dt: Float) {

    }
}