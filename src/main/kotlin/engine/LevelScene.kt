package org.poly.engine

class LevelScene(
    override val camera: Camera,
    private val game: Game,
    private val logger: Logger = Logger("Level Scene")
) : Scene(
    camera,
    game,
    logger
) {
    override fun tick(dt: Float) {

    }
}