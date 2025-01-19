package renderer

import org.joml.*
import org.lwjgl.BufferUtils.createFloatBuffer
import org.lwjgl.BufferUtils.createIntBuffer
import org.lwjgl.opengl.GL30.*
import org.lwjgl.stb.STBImage.stbi_image_free
import org.lwjgl.stb.STBImage.stbi_load
import org.poly.engine.Game
import org.poly.engine.Logger
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

sealed interface Disposable {
    fun dispose()
}

sealed class Resource(val path: String) : Disposable

class Texture(private val filePath: String) : Resource(filePath) {
    private var texID: Int = 0

    override fun dispose() {
        unbind()
    }

    init {
        // Generate texture
        texID = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, texID)

        // Set texture params and repeat img both directions
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)

        // Pixelate when stretching img
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)

        // Pixelate when shrinking img
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

        val width = createIntBuffer(1)
        val height = createIntBuffer(1)
        val channels = createIntBuffer(1)
        val image = stbi_load(filePath, width, height, channels, 0)

        if (image != null) {
            when {
                channels.get(0) == 3 -> {
                    glTexImage2D(
                        GL_TEXTURE_2D,
                        0,
                        GL_RGB, // No alpha value
                        width.get(0),
                        height.get(0),
                        0,
                        GL_RGB, // No alpha value
                        GL_UNSIGNED_BYTE,
                        image
                    )
                }
                channels.get(0) == 4 -> {
                    glTexImage2D(
                        GL_TEXTURE_2D,
                        0,
                        GL_RGBA,
                        width.get(0),
                        height.get(0),
                        0,
                        GL_RGBA,
                        GL_UNSIGNED_BYTE,
                        image
                    )
                }
            }

            stbi_image_free(image)
        }
    }

    fun bind() {
        glBindTexture(GL_TEXTURE_2D, texID)
    }

    fun unbind() {
        glBindTexture(GL_TEXTURE_2D, 0)
    }
}

class Shader(private val filePath: String) : Resource(filePath) {
    private val logger = Logger("Shader")

    private var shaderProgramID = 0
    private var vertexID = 0
    private var fragmentID = 0
    private var vertexSrc = ""
    private var fragmentSrc = ""
    private var active = false

    override fun dispose() {
        detach()
    }

    init {
        try {
            // Read the file content
            val shaderSource = Files.readString(Paths.get(filePath))

            // Split the shader source into sections based on #type
            val lines = shaderSource.split("\n")
            var currentType: String? = null

            val vertexBuilder = StringBuilder()
            val fragmentBuilder = StringBuilder()

            for (line in lines) {
                when {
                    line.startsWith("#type") -> {
                        currentType = line.substring(6).trim() // Get "vertex" or "fragment"
                    }
                    currentType == "vertex" -> {
                        vertexBuilder.appendLine(line)
                    }
                    currentType == "fragment" -> {
                        fragmentBuilder.appendLine(line)
                    }
                }
            }

            vertexSrc = vertexBuilder.toString().trim()
            fragmentSrc = fragmentBuilder.toString().trim()

            if (vertexSrc.isEmpty() || fragmentSrc.isEmpty()) {
                throw IOException("Failed to parse shader file: Vertex or Fragment shader source is missing.")
            }

        } catch (e: IOException) {
            logger.error("Error reading shader file: ${e.message}")
            e.printStackTrace()
        }

        // logger.debug(vertexSrc)
        // logger.debug(fragmentSrc)
    }

    fun use() {
        if (active) return

        glUseProgram(shaderProgramID)
        active = true
    }

    fun detach() {
        glUseProgram(0)
        active = false
    }

    fun uploadTexture(
        name: String,
        slot: Int
    ) {
        val loc = glGetUniformLocation(shaderProgramID, name)
        use()

        glUniform1i(loc, slot)
    }

    fun uploadMatrix4f(
        name: String,
        mat4: Matrix4f
    ) {
        val loc = glGetUniformLocation(shaderProgramID, name)
        use()

        val matBuffer = createFloatBuffer(16)
        mat4.get(matBuffer)
        glUniformMatrix4fv(loc, false, matBuffer)
    }

    fun uploadMatrix3f(
        name: String,
        mat3: Matrix3f
    ) {
        val loc = getUniformLocation(name)
        use()

        val matBuffer = createFloatBuffer(9)
        mat3.get(matBuffer)
        glUniformMatrix3fv(loc, false, matBuffer)
    }

    fun uploadVec4f(
        name: String,
        vec4: Vector4f
    ) {
        val loc = glGetUniformLocation(shaderProgramID, name)
        use()

        glUniform4f(loc, vec4.x, vec4.y, vec4.z, vec4.w)
    }

    fun uploadVec3f(
        name: String,
        vec3: Vector3f
    ) {
        val loc = getUniformLocation(name)
        use()

        glUniform3f(loc, vec3.x, vec3.y, vec3.z)
    }

    fun uploadVec2f(
        name: String,
        vec2: Vector2f
    ) {
        val loc = getUniformLocation(name)
        use()

        glUniform2f(loc, vec2.x, vec2.y)
    }

    private fun getUniformLocation(name: String): Int {
        val loc = glGetUniformLocation(shaderProgramID, name)
        if (loc == -1) {
            throw IllegalArgumentException("Uniform location not found for name: $name")
        }
        return loc
    }

    fun uploadFloat(
        name: String,
        float: Float
    ) {
        val loc = glGetUniformLocation(shaderProgramID, name)
        use()

        glUniform1f(loc, float)
    }

    fun uploadInt(
        name: String,
        int: Int
    ) {
        val loc = glGetUniformLocation(shaderProgramID, name)
        use()

        glUniform1i(loc, int)
    }

    fun compileAndLink(game: Game) {
        compile(game)
        link(game)
    }

    private fun compile(game: Game) {
        vertexID = compileShader(
            type = GL_VERTEX_SHADER,
            source = vertexSrc,
            shaderName = "vertex",
            game = game
        )

        fragmentID = compileShader(
            type = GL_FRAGMENT_SHADER,
            source = fragmentSrc,
            shaderName = "fragment",
            game = game
        )
    }

    private fun link(game: Game) {
        shaderProgramID = createShaderProgram(
            vertexShaderID = vertexID,
            fragmentShaderID = fragmentID,
            game = game
        )
    }

    private fun compileShader(
        type: Int,
        source: String,
        shaderName: String,
        game: Game,
    ): Int {
        val shaderID = glCreateShader(type)
        glShaderSource(shaderID, source)
        glCompileShader(shaderID)

        // Check for compilation errors
        val success = glGetShaderi(shaderID, GL_COMPILE_STATUS)
        if (success == GL_FALSE) {
            val length = glGetShaderi(shaderID, GL_INFO_LOG_LENGTH)
            logger.critical("'$shaderName': ($filePath) shader compilation FAILED!")
            logger.critical(glGetShaderInfoLog(shaderID, length))
            game.terminate()
        }
        return shaderID
    }

    private fun createShaderProgram(
        vertexShaderID: Int,
        fragmentShaderID: Int,
        game: Game
    ): Int {
        val programID = glCreateProgram()
        glAttachShader(programID, vertexShaderID)
        glAttachShader(programID, fragmentShaderID)
        glLinkProgram(programID)

        // Check for linking errors
        val success = glGetProgrami(programID, GL_LINK_STATUS)
        if (success == GL_FALSE) {
            val length = glGetProgrami(programID, GL_INFO_LOG_LENGTH)
            logger.critical("($filePath): program linking FAILED!")
            logger.critical(glGetProgramInfoLog(programID, length))
            game.terminate()
        }
        return programID
    }
}