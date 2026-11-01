
package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class MazeView extends View {

    private final int SIZE = 10;

    private int[][] maze = new int[SIZE][SIZE];
    private boolean[][] explored = new boolean[SIZE][SIZE];
    private boolean[][] path = new boolean[SIZE][SIZE];
    private boolean[][] userPath = new boolean[SIZE][SIZE];
    private int playerRow = 0;
    private int playerColumn = 0;
    private boolean playerVisible = false;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF cellRect = new RectF();

    // Colors
    private static final int COLOR_WALL = Color.rgb(30, 41, 59);
    private static final int COLOR_OPEN = Color.rgb(241, 245, 249);
    private static final int COLOR_START = Color.rgb(34, 197, 94);
    private static final int COLOR_END = Color.rgb(239, 68, 68);
    private static final int COLOR_PATH = Color.rgb(59, 130, 246);
    private static final int COLOR_EXPLORED = Color.rgb(251, 191, 36);
    private static final int COLOR_PLAYER = Color.rgb(249, 115, 22);
    private static final int COLOR_USER_PATH = Color.rgb(168, 85, 247);
    private static final int COLOR_GRID = Color.rgb(15, 25, 35);

    private static final float CORNER_RADIUS = 5f;
    private static final float CELL_GAP = 2.5f;

    public MazeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        glowPaint.setAntiAlias(true);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(3f);
    }

    public void setMaze(int[][] data) {
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(data[i], 0, maze[i], 0, SIZE);
        }
        invalidate();
    }

    public void clearMaze() {
        explored = new boolean[SIZE][SIZE];
        path = new boolean[SIZE][SIZE];
        userPath = new boolean[SIZE][SIZE];
        invalidate();
    }

    public void setExploredCell(int r, int c) {
        explored[r][c] = true;
        invalidate();
    }

    public void setPath(int r, int c) {
        path[r][c] = true;
        invalidate();
    }

    public void setPlayer(int r, int c) {
        playerRow = r;
        playerColumn = c;
        playerVisible = true;
        invalidate();
    }

    public void clearPlayer() {
        playerVisible = false;
        userPath = new boolean[SIZE][SIZE];
        invalidate();
    }

    public void setUserPath(int r, int c) {
        userPath[r][c] = true;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cellW = getWidth() / (float) SIZE;
        float cellH = getHeight() / (float) SIZE;
        float cell = Math.min(cellW, cellH);

        // Center the maze
        float offsetX = (getWidth() - cell * SIZE) / 2f;
        float offsetY = (getHeight() - cell * SIZE) / 2f;

        // Draw background
        canvas.drawColor(COLOR_GRID);

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {

                float left = offsetX + c * cell + CELL_GAP;
                float top = offsetY + r * cell + CELL_GAP;
                float right = offsetX + (c + 1) * cell - CELL_GAP;
                float bottom = offsetY + (r + 1) * cell - CELL_GAP;

                cellRect.set(left, top, right, bottom);

                // Determine cell color
                int color;
                boolean isSpecial = false;

                if (maze[r][c] == 1) {
                    color = COLOR_WALL;
                } else if (playerVisible && r == playerRow && c == playerColumn) {
                    color = COLOR_PLAYER;
                    isSpecial = true;
                } else if (r == 0 && c == 0) {
                    color = COLOR_START;
                    isSpecial = true;
                } else if (r == SIZE - 1 && c == SIZE - 1) {
                    color = COLOR_END;
                    isSpecial = true;
                } else if (userPath[r][c]) {
                    color = COLOR_USER_PATH;
                } else if (path[r][c]) {
                    color = COLOR_PATH;
                } else if (explored[r][c]) {
                    color = COLOR_EXPLORED;
                } else {
                    color = COLOR_OPEN;
                }

                paint.setColor(color);
                canvas.drawRoundRect(cellRect, CORNER_RADIUS, CORNER_RADIUS, paint);

                // Draw glow effect for special cells
                if (isSpecial && maze[r][c] == 0) {
                    glowPaint.setColor(color);
                    glowPaint.setAlpha(80);
                    RectF glowRect = new RectF(left - 2, top - 2, right + 2, bottom + 2);
                    canvas.drawRoundRect(glowRect, CORNER_RADIUS + 2, CORNER_RADIUS + 2, glowPaint);
                }

                // Draw labels
                if (r == 0 && c == 0 && !(playerVisible && playerRow == 0 && playerColumn == 0)) {
                    textPaint.setTextSize(cell * 0.38f);
                    canvas.drawText("S", left + (right - left) / 2,
                            top + (bottom - top) * 0.62f, textPaint);
                } else if (r == SIZE - 1 && c == SIZE - 1
                        && !(playerVisible && playerRow == SIZE - 1 && playerColumn == SIZE - 1)) {
                    textPaint.setTextSize(cell * 0.38f);
                    canvas.drawText("G", left + (right - left) / 2,
                            top + (bottom - top) * 0.62f, textPaint);
                } else if (playerVisible && r == playerRow && c == playerColumn) {
                    // Draw runner emoji-like indicator
                    textPaint.setTextSize(cell * 0.42f);
                    canvas.drawText("●", left + (right - left) / 2,
                            top + (bottom - top) * 0.65f, textPaint);
                }
            }
        }
    }
}
