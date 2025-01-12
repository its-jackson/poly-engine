package org.poly.engine

import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.NULL
import java.lang.System.nanoTime

data class GameState(
    val title: String,
    val width: Int,
    val height: Int,

    var dt: Float = 0.0f,
    var fps: Float = 0.0f,
    var elapsedTime: Float = 0.0f,
    var frameCount: Int = 0,

    var terminate: Boolean = false,

    var r: Float = 1.0f,
    var g: Float = 1.0f,
    var b: Float = 1.0f,
    var a: Float = 1.0f,
)

class Game(
    val state: GameState,
    val sceneManager: SceneManager = SceneManager(),
    val camera: Camera = Camera(),
) {
    private val logger: Logger = Logger("Game")
    private var handle: Long = NULL
    private var cursor: Long = NULL

    fun getScene() = sceneManager.currentScene

    fun terminate() {
        state.terminate = true
    }

    fun execute() {
        init()
        loop()
        end()
    }

    private fun init() {
        logger.info("Running LWJGL version: ${Version.getVersion()}")

        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set()

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        check(glfwInit()) {
            "Unable to initialize GLFW"
        }

        // Configure GLFW
        glfwDefaultWindowHints() // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable
        //glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE) // the window will be maximized

        // Create the window
        handle = glfwCreateWindow(state.width, state.height, state.title, NULL, NULL)
        if (handle == NULL) throw RuntimeException("Failed to create the GLFW window")

        // Create the custom cursor
        cursor = glfwCreateStandardCursor(GLFW_CROSSHAIR_CURSOR)
        if (cursor == NULL) throw RuntimeException("Failed to create the GLFW cursor")

        // Set mouse cursor pos callback
        glfwSetCursorPosCallback(handle, MouseListening::cursorPositionCallback)
        // Set mouse button callback
        glfwSetMouseButtonCallback(handle, MouseListening::mouseButtonCallback)
        // Set mouse scroll callback
        glfwSetScrollCallback(handle, MouseListening::mouseScrollCallback)

        // Set key callback
        glfwSetKeyCallback(handle, KeyListening::keyCallback)

        stackPush().use { stack ->
            val pWidth = stack.mallocInt(1) // int*
            val pHeight = stack.mallocInt(1) // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(handle, pWidth, pHeight)

            // Get the resolution of the primary monitor
            val vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor())

            // Center the window
            glfwSetWindowPos(
                handle,
                (vidmode!!.width() - pWidth[0]) / 2,
                (vidmode!!.height() - pHeight[0]) / 2
            )
        }

        // Make the OpenGL context current
        glfwMakeContextCurrent(handle)

        // Enable v-sync
        glfwSwapInterval(1)

        // Make the window visible
        glfwShowWindow(handle)

        // Set custom cursor
        // glfwSetCursor(handle, NULL)
        glfwSetCursor(handle, cursor)

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities()

        // Set the games scene to the level editor on start-up
        sceneManager.changeScene(Scenes.LEVEL_EDITOR, this)
    }

    private fun loop() {
        var startTime = nanoTime()

        while (!glfwWindowShouldClose(handle)) {
            val currentTime = nanoTime()
            state.dt = ((currentTime - startTime) / 1_000_000_000.0).toFloat() // Convert to seconds
            startTime = currentTime

            state.fps = (1 / (state.dt + 1e-9)).toFloat()
            state.frameCount++
            state.elapsedTime += state.dt

            // Proper Rendering Order:
            //  1. Clear the framebuffer.
            //  2. Set up any necessary state for rendering (e.g., binding shaders, VAOs).
            //  3. Render your scene.
            //  4. Swap buffers to display the rendered frame.
            // Clear the framebuffer before updating the scene
            glClearColor(state.r, state.g, state.b, state.a)
            // Clear both color and depth buffers
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            // Tick the current scene (render graphics, etc)
            sceneManager.currentScene?.tick(state.dt)
            // Swap the color buffers
            glfwSwapBuffers(handle)

            if (state.elapsedTime >= 1.0) {
                state.frameCount = 0
                state.elapsedTime = 0.0f
            }

            if (state.terminate || KeyListening.isKeyPressed(GLFW_KEY_ESCAPE)) {
                glfwSetWindowShouldClose(handle, true)
                return
            }

            glfwPollEvents()
        }
    }

    private fun end() {
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(handle)
        glfwDestroyWindow(handle)
        glfwDestroyCursor(cursor)

        // Terminate GLFW and free the error callback
        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }
}