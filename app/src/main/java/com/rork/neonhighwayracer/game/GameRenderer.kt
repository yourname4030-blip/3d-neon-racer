package com.rork.neonhighwayracer.game

import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Car(var x: Float, var z: Float, val color: FloatArray) {
    
    private val vertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
        "attribute vec4 vPosition;" +
        "void main() {" +
        "  gl_Position = uMVPMatrix * vPosition;" +
        "}"

    private val fragmentShaderCode =
        "precision mediump float;" +
        "uniform vec4 vColor;" +
        "void main() {" +
        "  gl_FragColor = vColor;" +
        "}"

    private var mProgram: Int
    private var positionHandle: Int = 0
    private var colorHandle: Int = 0
    private var mvpMatrixHandle: Int = 0
    
    // Lamborghini type low poly car
    private val carCoords = floatArrayOf(
        // Body
        -0.15f, 0.05f, 0.3f,   0.15f, 0.05f, 0.3f,   0.15f, 0.05f, -0.3f,
        -0.15f, 0.05f, 0.3f,   0.15f, 0.05f, -0.3f,  -0.15f, 0.05f, -0.3f,
        
        // Roof
        -0.12f, 0.15f, 0.1f,   0.12f, 0.15f, 0.1f,   0.12f, 0.15f, -0.15f,
        -0.12f, 0.15f, 0.1f,   0.12f, 0.15f, -0.15f, -0.12f, 0.15f, -0.15f,
        
        // Front windshield
        -0.12f, 0.15f, 0.1f,   0.12f, 0.15f, 0.1f,   0.15f, 0.05f, 0.3f,
        -0.12f, 0.15f, 0.1f,   0.15f, 0.05f, 0.3f,  -0.15f, 0.05f, 0.3f
    )
    
    private val vertexBuffer: FloatBuffer

    init {
        val bb = ByteBuffer.allocateDirect(carCoords.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(carCoords)
        vertexBuffer.position(0)

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        mProgram = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    fun draw(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(mProgram)
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)

        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
        mvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")

        val modelMatrix = FloatArray(16)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, x, 0f, z)
        
        val finalMatrix = FloatArray(16)
        Matrix.multiplyMM(finalMatrix, 0, mvpMatrix, 0, modelMatrix, 0)

        // Neon glow body
        GLES20.glUniform4fv(colorHandle, 1, color, 0)
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, finalMatrix, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, carCoords.size / 3)
        
        GLES20.glDisableVertexAttribArray(positionHandle)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
}
