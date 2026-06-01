package com.neon.racer

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.sin
import kotlin.random.Random

class GameView(context: Context) : View(context) {
    
    private val paint = Paint().apply { isAntiAlias = true }
    private var playerCar = PlayerCar()
    private val vehicles = mutableListOf<Vehicle>()
    private var score = 0
    private var gameOver = false
    private var roadOffset = 0f
    private var spawnTimer = 0
    
    enum class VehicleType {
        LUXURY_CAR, SPORTS_CAR, TRUCK, BUS, POLICE
    }
    
    data class Vehicle(
        var x: Float, 
        var y: Float, 
        val type: VehicleType,
        val speed: Float,
        val width: Float,
        val height: Float,
        val color: Int
    )
    
    data class PlayerCar(
        var x: Float = 540f, 
        var y: Float = 1600f,
        var targetX: Float = 540f
    )
    
    init {
        post(object : Runnable {
            override fun run() {
                if (!gameOver) {
                    update()
                    invalidate()
                }
                postDelayed(this, 16)
            }
        })
    }
    
    private fun update() {
        roadOffset += 20f
        if (roadOffset > 100f) roadOffset = 0f
        
        playerCar.x += (playerCar.targetX - playerCar.x) * 0.2f
        
        spawnTimer++
        if (spawnTimer > 30) {
            spawnTimer = 0
            spawnRandomVehicle()
        }
        
        val iterator = vehicles.iterator()
        while (iterator.hasNext()) {
            val vehicle = iterator.next()
            vehicle.y += vehicle.speed
            
            if (checkCollision(vehicle)) {
                gameOver = true
            }
            
            if (vehicle.y > height + 200) {
                iterator.remove()
                score += 10
            }
        }
    }
    
    private fun spawnRandomVehicle() {
        val lanes = listOf(200f, 540f, 880f)
        val type = VehicleType.values().random()
        
        val vehicle = when(type) {
            VehicleType.LUXURY_CAR -> Vehicle(
                lanes.random(), -200f, type, 15f, 80f, 160f, Color.rgb(255, 215, 0)
            )
            VehicleType.SPORTS_CAR -> Vehicle(
                lanes.random(), -200f, type, 20f, 70f, 140f, Color.MAGENTA
            )
            VehicleType.TRUCK -> Vehicle(
                lanes.random(), -300f, type, 10f, 100f, 250f, Color.rgb(255, 100, 0)
            )
            VehicleType.BUS -> Vehicle(
                lanes.random(), -350f, type, 8f, 110f, 300f, Color.CYAN
            )
            VehicleType.POLICE -> Vehicle(
                lanes.random(), -200f, type, 18f, 80f, 160f, Color.BLUE
            )
        }
        vehicles.add(vehicle)
    }
    
    private fun checkCollision(vehicle: Vehicle): Boolean {
        val px1 = playerCar.x - 40f
        val py1 = playerCar.y - 80f
        val px2 = playerCar.x + 40f
        val py2 = playerCar.y + 80f
        
        val vx1 = vehicle.x - vehicle.width/2
        val vy1 = vehicle.y - vehicle.height/2
        val vx2 = vehicle.x + vehicle.width/2
        val vy2 = vehicle.y + vehicle.height/2
        
        return !(px2 < vx1 || px1 > vx2 || py2 < vy1 || py1 > vy2)
    }
    
    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.rgb(10, 5, 30))
        drawRoad(canvas)
        drawVehicles(canvas)
        drawPlayerCar(canvas)
        drawHUD(canvas)
        if (gameOver) drawGameOver(canvas)
    }
    
    private fun drawRoad(canvas: Canvas) {
        paint.color = Color.rgb(30, 20, 50)
        canvas.drawRect(100f, 0f, width - 100f, height.toFloat(), paint)
        
        paint.color = Color.CYAN
        paint.strokeWidth = 4f
        for (i in 0..20) {
            val y = i * 100f + roadOffset
            canvas.drawLine(380f, y, 380f, y + 50f, paint)
            canvas.drawLine(700f, y, 700f, y + 50f, paint)
        }
        
        paint.color = Color.MAGENTA
        paint.strokeWidth = 8f
        canvas.drawLine(100f, 0f, 100f, height.toFloat(), paint)
        canvas.drawLine(width - 100f, 0f, width - 100f, height.toFloat(), paint)
    }
    
    private fun drawVehicles(canvas: Canvas) {
        vehicles.forEach { vehicle ->
            when(vehicle.type) {
                VehicleType.LUXURY_CAR -> drawLuxuryCar(canvas, vehicle)
                VehicleType.SPORTS_CAR -> drawSportsCar(canvas, vehicle)
                VehicleType.TRUCK -> drawTruck(canvas, vehicle)
                VehicleType.BUS -> drawBus(canvas, vehicle)
                VehicleType.POLICE -> drawPoliceCar(canvas, vehicle)
            }
        }
    }
    
    private fun drawLuxuryCar(canvas: Canvas, v: Vehicle) {
        paint.style = Paint.Style.FILL
        paint.color = v.color
        canvas.drawRoundRect(v.x-v.width/2, v.y-v.height/2, v.x+v.width/2, v.y+v.height/2, 20f, paint)
        
        paint.color = Color.argb(150, 0, 255, 255)
        canvas.drawRoundRect(v.x-30f, v.y-60f, v.x+30f, v.y-20f, 10f, paint)
        
        paint.style = Paint.Style.STROKE
        paint.color = Color.YELLOW
        paint.strokeWidth = 6f
        canvas.drawRoundRect(v.x-v.width/2-5, v.y-v.height/2-5, v.x+v.width/2+5, v.y+v.height/2+5, 25f, paint)
    }
    
    private fun drawSportsCar(canvas: Canvas, v: Vehicle) {
        paint.style = Paint.Style.FILL
        paint.color = v.color
        val path = Path()
        path.moveTo(v.x, v.y - v.height/2)
        path.lineTo(v.x - v.width/2, v.y + v.height/2)
        path.lineTo(v.x + v.width/2, v.y + v.height/2)
        path.close()
        canvas.drawPath(path, paint)
        
        paint.color = Color.WHITE
        paint.strokeWidth = 4f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(v.x, v.y-v.height/2, v.x, v.y+v.height/2, paint)
    }
    
    private fun drawTruck(canvas: Canvas, v: Vehicle) {
        paint.style = Paint.Style.FILL
        paint.color = v.color
        canvas.drawRect(v.x-v.width/2, v.y-v.height/2, v.x+v.width/2, v.y+v.height/2-60, paint)
        
        paint.color = Color.rgb(255, 150, 0)
        canvas.drawRoundRect(v.x-v.width/2, v.y+v.height/2-60, v.x+v.width/2, v.y+v.height/2, 10f, paint)
        
        paint.style = Paint.Style.STROKE
        paint.color = Color.RED
        paint.strokeWidth = 5f
        canvas.drawRect(v.x-v.width/2, v.y-v.height/2, v.x+v.width/2, v.y+v.height/2, paint)
    }
    
    private fun drawBus(canvas: Canvas, v: Vehicle) {
        paint.style = Paint.Style.FILL
        paint.color = v.color
        canvas.drawRoundRect(v.x-v.width/2, v.y-v.height/2, v.x+v.width/2, v.y+v.height/2, 15f, paint)
        
        paint.color = Color.argb(100, 255, 255, 255)
        for (i in 0..4) {
            canvas.drawRect(
                v.x-v.width/2+10, v.y-v.height/2+20+i*50,
                v.x+v.width/2-10, v.y-v.height/2+50+i*50, paint
            )
        }
        
        paint.color = Color.CYAN
        paint.strokeWidth = 6f
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(v.x-v.width/2, v.y-v.height/2, v.x+v.width/2, v.y+v.height/2, 15f, paint)
    }
    
    private fun drawPoliceCar(canvas: Canvas, v: Vehicle) {
        paint.style = Paint.Style.FILL
        paint.color = v.color
        canvas.drawRoundRect(v.x-v.width/2, v.y-v.height/2, v.x+v.width/2, v.y+v.height/2, 20f, paint)
        
        paint.color = if (System.currentTimeMillis() % 500 < 250) Color.RED else Color.BLUE
        canvas.drawCircle(v.x-20f, v.y-v.height/2-10, 15f, paint)
        canvas.drawCircle(v.x+20f, v.y-v.height/2-10, 15f, paint)
    }
    
    private fun drawPlayerCar(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(255, 215, 0)
        canvas.drawRoundRect(
            playerCar.x-40f, playerCar.y-80f,
            playerCar.x+40f, playerCar.y+80f, 25f, paint
        )
        
        paint.color = Color.argb(180, 0, 255, 255)
        canvas.drawRoundRect(
            playerCar.x-30f, playerCar.y-60f,
            playerCar.x+30f, playerCar.y-10f, 15f, paint
        )
        
        val pulse = 5f + sin(System.currentTimeMillis() / 200f) * 3f
        paint.style = Paint.Style.STROKE
        paint.color = Color.MAGENTA
        paint.strokeWidth = pulse
        canvas.drawRoundRect(
            playerCar.x-45f, playerCar.y-85f,
            playerCar.x+45f, playerCar.y+85f, 30f, paint
        )
        
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.drawCircle(playerCar.x-25f, playerCar.y-70f, 8f, paint)
        canvas.drawCircle(playerCar.x+25f, playerCar.y-70f, 8f, paint)
    }
    
    private fun drawHUD(canvas: Canvas) {
        paint.color = Color.CYAN
        paint.textSize = 60f
        paint.style = Paint.Style.FILL
        paint.setShadowLayer(20f, 0f, 0f, Color.CYAN)
        canvas.drawText("SCORE: $score", 50f, 80f, paint)
        paint.clearShadowLayer()
    }
    
    private fun drawGameOver(canvas: Canvas) {
        paint.color = Color.argb(200, 0, 0, 0)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        
        paint.color = Color.RED
        paint.textSize = 100f
        paint.textAlign = Paint.Align.CENTER
        paint.setShadowLayer(30f, 0f, 0f, Color.RED)
        canvas.drawText("GAME OVER", width/2f, height/2f, paint)
        
        paint.color = Color.WHITE
        paint.textSize = 50f
        canvas.drawText("Score: $score", width/2f, height/2f + 100f, paint)
        canvas.drawText("Tap to restart", width/2f, height/2f + 180f, paint)
        paint.clearShadowLayer()
        paint.textAlign = Paint.Align.LEFT
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (gameOver) {
                gameOver = false
                score = 0
                vehicles.clear()
                playerCar.x = 540f
                playerCar.targetX = 540f
            } else {
                playerCar.targetX = when {
                    event.x < width / 3 -> 200f
                    event.x > width * 2 / 3 -> 880f
                    else -> 540f
                }
            }
        }
        return true
    }
}
