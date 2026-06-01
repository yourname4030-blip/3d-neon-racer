package com.yourname.neonracer

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Road {
    private val vertexShaderCode = """
        attribute vec4 vPosition;
        attribute vec2 aTexCoord;
        uniform mat4 uMVPMatrix;
        varying vec2 vTexCoord;
        varying float vFog;
        void main() {
            gl_Position = uMVPMatrix * vPosition;
            vTexCoord = aTexCoord;
            vFog = clamp((gl_Position.z + 15.0) / 20.0, 0.0, 1.0);
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;
        varying vec2 vTexCoord;
        varying float vFog;
        uniform float uTime;
        vec3 neonColor = vec3(0.0, 1.0, 1.0);
        void main() {
            float x = vTexCoord.x;
            float z = vTexCoord.y;
            float centerLine = step(0.48, x) * step(x, 0.52);
            float sideLineL = step(0.05, x) * step(x, 0.1);
            float sideLineR = step(0.9, x) * step(x, 0.95);
            float lines = centerLine + sideLineL + sideLineR;
            float scroll = fract(z * 8.0 - uTime * 3.0);
            float glow = smoothstep(0.0, 0.1, scroll) * smoothstep(0.2, 0.1, scroll);
            vec3 roadColor = vec3(0.05, 0.05, 0.15);
            vec3 finalColor = mix(roadColor, neonColor * 2.0, lines + glow * 0.6);
            finalColor = mix(finalColor, vec3(0.0, 0.02, 0.1), vFog);
            gl_FragColor = vec4(finalColor, 1.0);
        }
    """.trimIndent()

    private val program: Int
    private var positionHandle: Int = 0
    private var texCoordHandle: Int = 0
    private var mvpMatrixHandle: Int = 0
    private var timeHandle: Int = 0
    private val vertexBuffer: FloatBuffer
    private val texCoordBuffer: FloatBuffer
    private val roadWidth = 4f
    private val roadLength = 100f
    private val roadCoords = floatArrayOf(-roadWidth,0f,0f, roadWidth,0f,0f, -roadWidth,0f,-roadLength, roadWidth,0f,-roadLength)
    private val texCoords = floatArrayOf(0f,0f, 1f,0f, 0f,10f, 1f,10f)

    init {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
        vertexBuffer = ByteBuffer.allocateDirect(roadCoords.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(roadCoords); position(0) }
        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(texCoords); position(0) }
    }

    fun draw(mvpMatrix: FloatArray, time: Float) {
        GLES20.glUseProgram(program)
        positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer)
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        timeHandle = GLES20.glGetUniformLocation(program, "uTime")
        GLES20.glUniform1f(timeHandle, time)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
}
