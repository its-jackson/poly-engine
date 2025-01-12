package org.poly.engine

class GameObject(
    val name: String,
    val transform: Transform = Transform(),
    private val components: MutableMap<Class<out Component>, MutableSet<Component>> = mutableMapOf()
) : Tickable, Startable {
    override fun tick(dt: Float) {
        components.values.flatten().forEach { it.tick(dt) }
    }

    override fun start() {
        components.values.flatten().forEach { it.start() }
    }

    fun <T : Component> getComponents(componentClass: Class<T>) = runCatching {
        components[componentClass]
            ?.map { componentClass.cast(it) }
            ?: emptyList()
    }

    fun <T : Component> getComponent(componentClass: Class<T>) = runCatching {
        components[componentClass]
            ?.firstOrNull()
            ?.let { componentClass.cast(it) }
    }

    fun <T : Component> removeComponents(componentClass: Class<T>): Boolean {
        return components.remove(componentClass)?.isNotEmpty() == true
    }

    fun <T : Component> removeComponent(componentClass: Class<T>): Boolean {
        val componentSet = components[componentClass]
        return componentSet?.firstOrNull()?.let {
            componentSet.remove(it)
            if (componentSet.isEmpty()) components.remove(componentClass)
            true
        } ?: false
    }

    fun addComponent(vararg newComponents: Component) {
        newComponents.forEach { component ->
            val componentClass = component.javaClass
            components.computeIfAbsent(componentClass) { mutableSetOf() }.add(component)
            component.gameObject = this
        }
    }
}
