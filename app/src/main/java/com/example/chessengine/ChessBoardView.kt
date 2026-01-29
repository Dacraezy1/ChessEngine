package com.example.chessengine

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class ChessBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val lightSquareColor = Color.parseColor("#FFFFFF")
    private val darkSquareColor = Color.parseColor("#B58863")
    private val selectedSquareColor = Color.parseColor("#FFD700")
    private val moveIndicatorColor = Color.parseColor("#00FF00")
    private val blackColor = Color.BLACK
    private val whiteColor = Color.WHITE

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = 40f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val arrowPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 8f
        color = moveIndicatorColor
    }

    private val arrowHeadPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = moveIndicatorColor
    }

    private var squareSize = 0f
    private var boardOffsetX = 0f
    private var boardOffsetY = 0f

    private var isFlipped = false
    private var showBestMove = true
    private var bestMove: String? = null

    // Standard starting position (FEN: rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR)
    private val board = Array(8) { IntArray(8) }

    init {
        initializeBoard()
    }

    fun initializeBoard() {
        // Initialize standard chess starting position
        // Format: PieceType | (Color << 3)
        // Piece types: EMPTY=0, PAWN=1, KNIGHT=2, BISHOP=3, ROOK=4, QUEEN=5, KING=6
        // Colors: WHITE=0, BLACK=1

        // Clear board
        for (i in 0..7) {
            for (j in 0..7) {
                board[i][j] = 0
            }
        }

        // Black pieces
        board[0][0] = 4 or (1 shl 3) // Rook
        board[0][1] = 2 or (1 shl 3) // Knight
        board[0][2] = 3 or (1 shl 3) // Bishop
        board[0][3] = 5 or (1 shl 3) // Queen
        board[0][4] = 6 or (1 shl 3) // King
        board[0][5] = 3 or (1 shl 3) // Bishop
        board[0][6] = 2 or (1 shl 3) // Knight
        board[0][7] = 4 or (1 shl 3) // Rook

        for (i in 0..7) {
            board[1][i] = 1 or (1 shl 3) // Pawns
        }

        // White pieces
        for (i in 0..7) {
            board[6][i] = 1 // Pawns
        }

        board[7][0] = 4 // Rook
        board[7][1] = 2 // Knight
        board[7][2] = 3 // Bishop
        board[7][3] = 5 // Queen
        board[7][4] = 6 // King
        board[7][5] = 3 // Bishop
        board[7][6] = 2 // Knight
        board[7][7] = 4 // Rook

        invalidate()
    }

    fun setFlipped(flipped: Boolean) {
        isFlipped = flipped
        invalidate()
    }

    fun setShowBestMove(show: Boolean) {
        showBestMove = show
        invalidate()
    }

    fun setBestMove(move: String) {
        bestMove = move
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val boardSize = min(w, h).toFloat()
        squareSize = boardSize / 8
        boardOffsetX = (w - boardSize) / 2
        boardOffsetY = (h - boardSize) / 2
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw board squares
        for (row in 0..7) {
            for (col in 0..7) {
                val displayRow = if (isFlipped) 7 - row else row
                val displayCol = if (isFlipped) 7 - col else col
                
                val left = boardOffsetX + displayCol * squareSize
                val top = boardOffsetY + displayRow * squareSize
                val right = left + squareSize
                val bottom = top + squareSize
                
                // Alternate square colors
                paint.color = if ((row + col) % 2 == 0) lightSquareColor else darkSquareColor
                canvas.drawRect(left, top, right, bottom, paint)
                
                // Draw piece
                val piece = board[row][col]
                if (piece != 0) {
                    drawPiece(canvas, piece, left, top, squareSize)
                }
            }
        }
        
        // Draw coordinates
        drawCoordinates(canvas)
        
        // Draw best move arrow if available
        if (showBestMove && bestMove != null) {
            drawBestMoveArrow(canvas)
        }
    }

    private fun drawPiece(canvas: Canvas, piece: Int, left: Float, top: Float, size: Float) {
        val pieceType = piece and 7
        val pieceColor = (piece shr 3) and 1
        
        val centerX = left + size / 2
        val centerY = top + size / 2
        val radius = size * 0.4f
        
        when (pieceType) {
            1 -> drawPawn(canvas, centerX, centerY, radius, pieceColor)
            2 -> drawKnight(canvas, centerX, centerY, radius, pieceColor)
            3 -> drawBishop(canvas, centerX, centerY, radius, pieceColor)
            4 -> drawRook(canvas, centerX, centerY, radius, pieceColor)
            5 -> drawQueen(canvas, centerX, centerY, radius, pieceColor)
            6 -> drawKing(canvas, centerX, centerY, radius, pieceColor)
        }
    }

    private fun drawPawn(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int) {
        paint.color = if (color == 0) whiteColor else blackColor
        canvas.drawCircle(cx, cy - radius * 0.2f, radius * 0.7f, paint)
        
        paint.strokeWidth = radius * 0.2f
        paint.style = Paint.Style.STROKE
        canvas.drawCircle(cx, cy - radius * 0.2f, radius * 0.7f, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawKnight(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int) {
        paint.color = if (color == 0) whiteColor else blackColor
        val path = Path()
        path.moveTo(cx - radius * 0.8f, cy + radius * 0.4f)
        path.lineTo(cx - radius * 0.4f, cy - radius * 0.6f)
        path.lineTo(cx + radius * 0.2f, cy - radius * 0.8f)
        path.lineTo(cx + radius * 0.6f, cy - radius * 0.2f)
        path.lineTo(cx + radius * 0.4f, cy + radius * 0.4f)
        path.close()
        canvas.drawPath(path, paint)
        
        paint.strokeWidth = radius * 0.15f
        paint.style = Paint.Style.STROKE
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawBishop(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int) {
        paint.color = if (color == 0) whiteColor else blackColor
        val path = Path()
        path.moveTo(cx, cy - radius * 0.8f)
        path.lineTo(cx - radius * 0.6f, cy + radius * 0.4f)
        path.lineTo(cx + radius * 0.6f, cy + radius * 0.4f)
        path.close()
        canvas.drawPath(path, paint)
        
        // Draw cross lines
        paint.strokeWidth = radius * 0.15f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(cx - radius * 0.4f, cy, cx + radius * 0.4f, cy, paint)
        canvas.drawLine(cx, cy - radius * 0.4f, cx, cy + radius * 0.4f, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawRook(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int) {
        paint.color = if (color == 0) whiteColor else blackColor
        canvas.drawRect(
            cx - radius * 0.7f,
            cy - radius * 0.7f,
            cx + radius * 0.7f,
            cy + radius * 0.5f,
            paint
        )
        
        paint.strokeWidth = radius * 0.15f
        paint.style = Paint.Style.STROKE
        canvas.drawRect(
            cx - radius * 0.7f,
            cy - radius * 0.7f,
            cx + radius * 0.7f,
            cy + radius * 0.5f,
            paint
        )
        paint.style = Paint.Style.FILL
    }

    private fun drawQueen(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int) {
        paint.color = if (color == 0) whiteColor else blackColor
        canvas.drawCircle(cx, cy, radius * 0.8f, paint)
        
        // Draw crown points
        for (i in 0..2) {
            val angle = Math.toRadians((i * 120 - 30).toDouble()).toFloat()
            val x1 = cx + cos(angle) * radius * 0.8f
            val y1 = cy + sin(angle) * radius * 0.8f
            val x2 = cx + cos(angle) * radius * 1.2f
            val y2 = cy + sin(angle) * radius * 1.2f
            canvas.drawLine(x1, y1, x2, y2, paint)
        }
        
        paint.strokeWidth = radius * 0.15f
        paint.style = Paint.Style.STROKE
        canvas.drawCircle(cx, cy, radius * 0.8f, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawKing(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int) {
        paint.color = if (color == 0) whiteColor else blackColor
        canvas.drawCircle(cx, cy, radius * 0.7f, paint)
        
        // Draw cross
        paint.strokeWidth = radius * 0.2f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(cx - radius * 0.5f, cy, cx + radius * 0.5f, cy, paint)
        canvas.drawLine(cx, cy - radius * 0.5f, cx, cy + radius * 0.5f, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawCoordinates(canvas: Canvas) {
        textPaint.textSize = squareSize * 0.3f
        textPaint.color = blackColor
        
        for (i in 0..7) {
            val displayI = if (isFlipped) 7 - i else i
            
            // File letters (a-h)
            val fileChar = if (isFlipped) 'h' - i else 'a' + i
            val fileX = boardOffsetX + displayI * squareSize + squareSize / 2
            val fileY = boardOffsetY + 8 * squareSize - 5
            canvas.drawText(fileChar.toString(), fileX, fileY, textPaint)
            
            // Rank numbers (1-8)
            val rankNum = if (isFlipped) i + 1 else 8 - i
            val rankX = boardOffsetX + 5
            val rankY = boardOffsetY + displayI * squareSize + squareSize / 2 + textPaint.textSize / 3
            canvas.drawText(rankNum.toString(), rankX, rankY, textPaint)
        }
    }

    private fun drawBestMoveArrow(canvas: Canvas) {
        bestMove?.let { move ->
            if (move.length != 4) return@let
            
            try {
                val fromFile = move[0] - 'a'
                val fromRank = move[1] - '1'
                val toFile = move[2] - 'a'
                val toRank = move[3] - '1'
                
                if (fromFile !in 0..7 || fromRank !in 0..7 || 
                    toFile !in 0..7 || toRank !in 0..7) return@let
                
                val startRow = if (isFlipped) 7 - fromRank else fromRank
                val startCol = if (isFlipped) 7 - fromFile else fromFile
                val endRow = if (isFlipped) 7 - toRank else toRank
                val endCol = if (isFlipped) 7 - toFile else toFile
                
                val startX = boardOffsetX + startCol * squareSize + squareSize / 2
                val startY = boardOffsetY + startRow * squareSize + squareSize / 2
                val endX = boardOffsetX + endCol * squareSize + squareSize / 2
                val endY = boardOffsetY + endRow * squareSize + squareSize / 2
                
                // Draw arrow shaft
                canvas.drawLine(startX, startY, endX, endY, arrowPaint)
                
                // Draw arrowhead
                val angle = atan2((endY - startY).toDouble(), (endX - startX).toDouble())
                val arrowLength = squareSize * 0.3f
                
                val x1 = endX - arrowLength * cos(angle - Math.PI / 6).toFloat()
                val y1 = endY - arrowLength * sin(angle - Math.PI / 6).toFloat()
                val x2 = endX - arrowLength * cos(angle + Math.PI / 6).toFloat()
                val y2 = endY - arrowLength * sin(angle + Math.PI / 6).toFloat()
                
                val path = Path()
                path.moveTo(endX, endY)
                path.lineTo(x1, y1)
                path.lineTo(x2, y2)
                path.close()
                canvas.drawPath(path, arrowHeadPaint)
            } catch (e: Exception) {
                // Ignore invalid moves
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
