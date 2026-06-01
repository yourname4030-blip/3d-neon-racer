package com.racergame.neonracer

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*
import kotlin.random.Random

class GameView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var playerX = 0f
    private var roadOffset = 0f
    private var score = 0
    private var speed = 8f
    private var gameOver = false
    private val enemies = mutableListOf<Enemy>()
    private var lastSpawn = 0L
    
    data class Enemy(var x: Float, var y: Float, var type: Int, var w: Float, var h: Float)
    
    init {
        playerX = 300f
    }
    
    private fun spawnEnemy() {
        val now = System.currentTimeMillis()
        if (now - lastSpawn > 1200) {
            val lanes = floatArrayOf(200f, 400f, 600f)
            val x = lanes.random()
            val type = Random.nextInt(5)
            val w = when(type) { 0->60f; 1->70f; 2->90f; 3->65f; else->75f }
            val h = when(type) { 0->100f; 1->120f; 2->140f; 3->110f; else->105f }
            enemies.add(Enemy(x, -h, type, w, h))
            lastSpawn = now
        }
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        
        // Background
        paint.color = Color.rgb(5, 5, 20)
        canvas.drawRect(0f, 0f, w, h, paint)
        
        // Road
        paint.color = Color.rgb(20, 20, 40)
        canvas.drawRect(w*0.1f, 0f, w*0.9f, h, paint)
        
        // Road lines
        paint.color = Color.CYAN
        paint.strokeWidth = 8f
        for(i in 0..20) {
            val y = (i * 100 + roadOffset) % h
            canvas.drawLine(w*0.5f, y, w*0.5f, y+50f, paint)
        }
        roadOffset += speed
        if(roadOffset > 100) roadOffset = 0f
        
        // Spawn enemies
        spawnEnemy()
        
        // Draw enemies
        enemies.removeAll { enemy ->
            enemy.y += speed
            drawCar(canvas, enemy.x, enemy.y, enemy.w, enemy.h, enemy.type, false)
            
            // Collision
            val px = playerX
            val py = h - 150f
            if (abs(px - enemy.x) < 60 && abs(py - enemy.y) < 80) {
                gameOver = true
            }
            enemy.y > h
        }
        
        // Draw player luxury car
        drawCar(canvas, playerX, h - 150f, 80f, 110f, 4, true)
        
        // Score
        if (!gameOver) score++
        paint.color = Color.CYAN
        paint.textSize = 50f
        canvas.drawText("Score: ${score/10}", 30f, 60f, paint)
        
        if (gameOver) {
            paint.textSize = 80f
            canvas.drawText("GAME OVER", w/2 - 200f, h/2, paint)
            paint.textSize = 40f
            canvas.drawText("Tap to restart", w/2 - 120f, h/2 + 60f, paint)
        } else {
            speed += 0.002f
            invalidate()
        }
    }
    
    private fun drawCar(canvas: Canvas, x: Float, y: Float, w: Float, h: Float, type: Int, isPlayer: Boolean) {
        val bodyColor = when(type) {
            0 -> Color.rgb(255, 50, 50)   // Sports red
            1 -> Color.rgb(255, 165, 0)   // Truck orange  
            2 -> Color.rgb(255, 255, 0)   // Bus yellow
            3 -> Color.rgb(0, 100, 255)   // Police blue
            else -> Color.rgb(0, 255, 255) // Luxury cyan
        }
        
        paint.style = Paint.Style.FILL
        paint.color = bodyColor
        
        // Glow for player
        if (isPlayer) {
            paint.setShadowLayer(30f, 0f, 0f, Color.CYAN)
        }
        
        // Body
        canvas.drawRoundRect(x-w/2, y-h/2, x+w/2, y+h/2, 15f, 15f, paint)
        paint.clearShadowLayer()
        
        // Windshield
        paint.color = Color.rgb(100, 150, 255)
        canvas.drawRoundRect(x-w/2+10, y-h/2+10, x+w/2-10, y-10, 10f, 10f, paint)
        
        // Wheels
        paint.color = Color.DKGRAY
        canvas.drawCircle(x-w/2+15, y+h/2-15, 12f, paint)
        canvas.drawCircle(x+w/2-15, y+h/2-15, 12f, paint)
        
        // Spoiler for luxury
        if (type == 4) {
            paint.color = Color.rgb(0, 200, 200)
            canvas.drawRect(x-w/2+5, y-h/2-8, x+w/2-5, y-h/2, paint)
        }
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gameOver && event.action == MotionEvent.ACTION_DOWN) {
            gameOver = false
            score = 0
            speed = 8f
            enemies.clear()
            invalidate()
            return true
        }
        
        if (event.action == MotionEvent.ACTION_MOVE) {
            playerX = event.x.coerceIn(width*0.15f, width*0.85f)
        }
        return true
    }
}
