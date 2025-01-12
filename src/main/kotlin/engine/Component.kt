package org.poly.engine

abstract class Component(
    var gameObject: GameObject? = null
) : Tickable, Startable {
    // TODO Provide default impl
    override fun tick(dt: Float) {

    }

    // TODO Provide default impl
    override fun start() {

    }
}