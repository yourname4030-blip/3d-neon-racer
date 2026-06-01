package com.neon.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

class GameView(context: Context) : View(context) {

    private var playerX = 0f
    private var playerY = 0f
    private var score = 0
    private var gameOver = false
    
    private val playerPaint = Paint().apply {
        color = Color.CYAN
        style = Paint.Style.FILL
    }
    
    private val enemyPaint = Paint().apply {
        style = Paint.Style.FILL
    }
    
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 60f
        isAntiAlias = true
    }
    
    private val roadPaint = Paint().apply {
        color = Color.DKGRAY
        style = Paint.Style.FILL
    }
    
    private val linePaint = Paint().apply {
        color = Color.YELLOW
        strokeWidth = 10f
    }
    
    private data class Enemy(var x: Float, var y: Float, var type: Int)
    private val enemies = mutableListOf<Enemy>()
    private var enemySpeed = 8f
    private var spawnTimer = 0
    
    init {
        post { gameLoop() }
    }
    
    private fun gameLoop() {
        if (!gameOver) {
            update()
            invalidate()
            postDelayed({ gameLoop() }, 16)
        }
    }
    
    private fun update() {
        if (playerY == 0f) {
            playerX = width / 2f
            playerY = height - 200f
        }
        
        spawnTimer++
        if (spawnTimer > 30) {
            spawnTimer = 0
            enemies.add(Enemy(
                Random.nextInt(width - 100).toFloat(),
                -100f,
                Random.nextInt(5)
            ))
        }
        
        val iterator = enemies.iterator()
        while (iterator.hasNext()) {
            val enemy = iterator.next()
            enemy.y += enemySpeed
            
            if (enemy.y > height) {
                iterator.remove()
                score += 10
                if (score % 100 == 0) enemySpeed += 0.5f
            }
            
            if (checkCollision(enemy)) {
                gameOver = true
            }
        }
    }
    
    private fun checkCollision(enemy: Enemy): Boolean {
        val carWidth = 80f
        val carHeight = 120f
        return playerX < enemy.x + carWidth &&
               playerX + carWidth > enemy.x &&
               playerY < enemy.y + carHeight &&
               playerY + carHeight > enemy.y
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLACK)
        
        // Road
        canvas.drawRect(width * 0.1f, 0f, width * 0.9f, height.toFloat(), roadPaint)
        
        // Road lines
        var lineY = (System.currentTimeMillis() / 10) % 100
        while (lineY < height) {
            canvas.drawLine(width / 2f, lineY, width / 2f, lineY + 50, linePaint)
            lineY += 100
        }
        
        // Player - Luxury Car
        drawLuxuryCar(canvas, playerX, playerY, Color.CYAN)
        
        // Enemies
        for (enemy in enemies) {
            when (enemy.type) {
                0 -> drawSportsCar(canvas, enemy.x, enemy.y, Color.RED)
                1 -> drawTruck(canvas, enemy.x, enemy.y, Color.GREEN)
                2 -> drawBus(canvas, enemy.x, enemy.y, Color.YELLOW)
                3 -> drawPoliceCar(canvas, enemy.x, enemy.y, Color.BLUE)
                else -> drawLuxuryCar(canvas, enemy.x, enemy.y, Color.MAGENTA)
            }
        }
        
        // Score
        canvas.drawText("Score: $score", 50f, 80f, textPaint)
        
        if (gameOver) {
            textPaint.textSize = 100f
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("GAME OVER", width / 2f, height / 2f, textPaint)
            canvas.drawText("Score: $score", width / 2f, height / 2f + 120f, textPaint)
            canvas.drawText("Tap to Restart", width / 2f, height / 2f + 240f, textPaint)
        }
    }
    
    private fun drawLuxuryCar(canvas: Canvas, x: Float, y: Float, color: Int) {
        enemyPaint.color = color
        canvas.drawRect(x + 10, y, x + 70, y + 120, enemyPaint)
        canvas.drawRect(x, y + 20, x + 80, y + 100, enemyPaint)
        enemyPaint.color = Color.BLACK
        canvas.drawRect(x + 5, y + 90, x + 25, y + 110, enemyPaint)
        canvas.drawRect(x + 55, y + 90, x + 75, y + 110, enemyPaint)
    }
    
    private fun drawSportsCar(canvas: Canvas, x: Float, y: Float, color: Int) {
        enemyPaint.color = color
        canvas.drawRect(x + 15, y + 10, x + 65, y + 110, enemyPaint)
        canvas.drawRect(x, y + 30, x + 80, y + 90, enemyPaint)
        enemyPaint.color = Color.BLACK
        canvas.drawRect(x + 5, y + 85, x + 25, y + 105, enemyPaint)
        canvas.drawRect(x + 55, y + 85, x + 75, y + 105, enemyPaint)
    }
    
    private fun drawTruck(canvas: Canvas, x: Float, y: Float, color: Int) {
        enemyPaint.color = color
        canvas.drawRect(x + 5, y, x + 75, y + 60, enemyPaint)
        canvas.drawRect(x, y + 60, x + 80, y + 120, enemyPaint)
        enemyPaint.color = Color.BLACK
        canvas.drawRect(x + 10, y + 95, x + 30, y + 115, enemyPaint)
        canvas.drawRect(x + 50, y + 95, x + 70, y + 115, enemyPaint)
    }
    
    private fun drawBus(canvas: Canvas, x: Float, y: Float, color: Int) {
        enemyPaint.color = color
        canvas.drawRect(x, y, x + 80, y + 140, enemyPaint)
        enemyPaint.color = Color.CYAN
        for (i in 0..2) {
            canvas.drawRect(x + 10, y + 20 + i * 35, x + 70, y + 40 + i * 35, enemyPaint)
        }
        enemyPaint.color = Color.BLACK
        canvas.drawRect(x + 5, y + 115, x + 25, y + 135, enemyPaint)
        canvas.drawRect(x + 55, y + 115, x + 75, y + 135, enemyPaint)
    }
    
    private fun drawPoliceCar(canvas: Canvas, x: Float, y: Float, color: Int) {
        enemyPaint.color = color
        canvas.drawRect(x + 10, y, x + 70, y + 120, enemyPaint)
        canvas.drawRect(x, y + 20, x + 80, y + 100, enemyPaint)
        enemyPaint.color = Color.RED
        canvas.drawRect(x + 20, y - 5, x + 35, y + 5, enemyPaint)
        enemyPaint.color = Color.BLUE
        canvas.drawRect(x + 45, y - 5, x + 60, y + 5, enemyPaint)
        enemyPaint.color = Color.BLACK
        canvas.drawRect(x + 5, y + 90, x + 25, y + 110, enemyPaint)
        canvas.drawRect(x + 55, y + 90, x + 75, y + 110, enemyPaint)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gameOver && event.action == MotionEvent.ACTION_DOWN) {
            gameOver = false
            score = 0
            enemies.clear()
            enemySpeed = 8f
            gameLoop()
            return true
        }
        
        if (event.action == MotionEvent.ACTION_MOVE) {
            playerX = event.x - 40
            if (playerX < width * 0.1f) playerX = width * 0.1f
            if (playerX > width * 0.9f - 80) playerX = width * 0.9f - 80
        }
        return true
    }
}
