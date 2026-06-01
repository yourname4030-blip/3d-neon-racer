package com.yourname.neonracer

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GameRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private lateinit var road: Road
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    
    private var startTime = System.nanoTime()

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        GLES20.glClearColor(0.0f, 0.02f, 0.1f, 1.0f) // Dark neon background
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        road = Road()
    }

    override fun onDrawFrame(unused: GL10) {
        val time = (System.nanoTime() - startTime) / 1000000000.0f
        
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        
        // Camera position: 2m up, 5m back
        Matrix.setLookAtM(viewMatrix, 0, 0f, 2f, 5f, 0f, 0f, -10f, 0f, 1.0f, 0.0f)
        
        // Combine projection and view
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        
        // Draw road with time for animation
        road.draw(mvpMatrix, time)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 2f, 100f)
    }
}
