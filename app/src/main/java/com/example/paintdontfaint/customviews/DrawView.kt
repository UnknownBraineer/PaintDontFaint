package com.example.paintdontfaint.customviews

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    companion object {
        private const val TOUCH_TOLERANCE = 4
        private const val DEFAULT_CURRENT_COLOR = Color.GREEN
        private const val DEFAULT_STROKE_WIDTH = 20
        const val backgroundColor = Color.WHITE
    }

    private var mX = 0F
    private var mY = 0F
    private lateinit var mPath: Path
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        isDither = true
        color = DEFAULT_CURRENT_COLOR
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        alpha = 0xff
    }

    private val paths = ArrayList<Stroke>()
    private var currentColor = DEFAULT_CURRENT_COLOR
    private var strokeWidth = DEFAULT_STROKE_WIDTH
    private lateinit var mBitmap: Bitmap
    private var mCanvas: Canvas? = null
    private val mBitmapPaint = Paint(Paint.DITHER_FLAG)

    fun init(height: Int, width: Int) {
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap)
    }

    fun setColor(color: Int) {
        currentColor = color
    }

    fun setStrokeWidth(width: Int) {
        strokeWidth = width
    }

    fun undo() {
        if (paths.size != 0) {
            paths.removeAt(paths.size - 1)
            invalidate()
        }
    }

    fun save() = mBitmap

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!.save()
        mCanvas?.drawColor(backgroundColor)
        for (stroke in paths) {
            mPaint.setColor(stroke.color)
            mPaint.strokeWidth = stroke.strokeWidth.toFloat()
            mCanvas?.drawPath(stroke.path, mPaint)
        }
        canvas.drawBitmap(mBitmap, 0f, 0f, mBitmapPaint)
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x = event!!.getX()
        val y = event.getY()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x,y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
        }

        return true
    }


    private fun touchStart(x: Float, y: Float) {
        mPath = Path()
        val fp = Stroke(currentColor, strokeWidth, mPath)
        paths.add(fp)
        mPath.reset()
        mPath.moveTo(x, y)
        mX = x
        mY = y
    }

    private fun touchMove(x: Float, y: Float) {
        val dx = Math.abs(x - mX)
        val dy = Math.abs(y - mY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
        }
    }

    private fun touchUp() {
        mPath.lineTo(mX, mY)
    }




}