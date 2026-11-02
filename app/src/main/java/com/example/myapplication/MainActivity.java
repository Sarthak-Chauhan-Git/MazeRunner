
package com.example.myapplication;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private MazeView mazeView;
    private TextView tvStatus, tvLevel, tvAlgorithm;
    private ProgressBar progressAnimation;

    // Comparison table cells
    private TextView tvBruteLength, tvHeuristicLength, tvUserLength;
    private TextView tvBruteNodes, tvHeuristicNodes;
    private TextView tvBruteTime, tvHeuristicTime;
    private TextView tvBrutePaths;
    private TextView tvBruteOptimal, tvHeuristicOptimal, tvUserOptimal;

    // Verdict
    private MaterialCardView cardVerdict, cardHeuristic;
    private TextView tvVerdict, tvHeuristicTrace;

    private Button btnBrute, btnHeuristic, btnReset, btnNext;
    private Button btnNextPath, btnPrevPath;
    private Button btnPlay, btnClearRun;

    private final int N = 10;
    private int level = 1;
    private int[][] maze = new int[N][N];

    private final int[] dr = {1, 0, -1, 0};
    private final int[] dc = {0, 1, 0, -1};

    private final Random random = new Random();

    private boolean[][] visited;
    private ArrayList<int[]> explored = new ArrayList<>();
    private ArrayList<int[]> bestPath = new ArrayList<>();
    private ArrayList<int[]> bruteBestPath = new ArrayList<>();

    private ArrayList<ArrayList<int[]>> allPaths = new ArrayList<>();

    private int pathsFound = 0;
    private int nodesVisited = 0;
    private int currentPathIndex = 0;

    private static final int MAX_PATHS = 5;

    private double bruteTime = -1;
    private double greedyTime = -1;

    private int bruteLength = -1;
    private int greedyLength = -1;

    private String heuristicTrace = "";
    private final Handler animationHandler = new Handler(Looper.getMainLooper());
    private boolean isAnimating = false;
    private boolean isPlaying = false;
    private int playerRow = 0;
    private int playerColumn = 0;
    private int userLength = -1;
    private ArrayList<int[]> userPath = new ArrayList<>();

    private static class Node {
        int r, c;

        Node(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full-screen immersive
        getWindow().setStatusBarColor(Color.parseColor("#0F1923"));
        getWindow().setNavigationBarColor(Color.parseColor("#0F1923"));

        setContentView(R.layout.activity_main);

        // Bind views
        mazeView = findViewById(R.id.mazeView);
        tvStatus = findViewById(R.id.tvStatus);
        tvLevel = findViewById(R.id.tvLevel);
        tvAlgorithm = findViewById(R.id.tvAlgorithm);
        progressAnimation = findViewById(R.id.progressAnimation);

        // Comparison table
        tvBruteLength = findViewById(R.id.tvBruteLength);
        tvHeuristicLength = findViewById(R.id.tvHeuristicLength);
        tvUserLength = findViewById(R.id.tvUserLength);
        tvBruteNodes = findViewById(R.id.tvBruteNodes);
        tvHeuristicNodes = findViewById(R.id.tvHeuristicNodes);
        tvBruteTime = findViewById(R.id.tvBruteTime);
        tvHeuristicTime = findViewById(R.id.tvHeuristicTime);
        tvBrutePaths = findViewById(R.id.tvBrutePaths);
        tvBruteOptimal = findViewById(R.id.tvBruteOptimal);
        tvHeuristicOptimal = findViewById(R.id.tvHeuristicOptimal);
        tvUserOptimal = findViewById(R.id.tvUserOptimal);

        // Cards
        cardVerdict = findViewById(R.id.cardVerdict);
        cardHeuristic = findViewById(R.id.cardHeuristic);
        tvVerdict = findViewById(R.id.tvVerdict);
        tvHeuristicTrace = findViewById(R.id.tvHeuristicTrace);

        // Buttons
        btnBrute = findViewById(R.id.btnBrute);
        btnHeuristic = findViewById(R.id.btnHeuristic);
        btnReset = findViewById(R.id.btnReset);
        btnNext = findViewById(R.id.btnNext);
        btnNextPath = findViewById(R.id.btnNextPath);
        btnPrevPath = findViewById(R.id.btnPrevPath);
        btnPlay = findViewById(R.id.btnPlay);
        btnClearRun = findViewById(R.id.btnClearRun);

        // Button listeners
        btnBrute.setOnClickListener(v -> runBruteForce());
        btnHeuristic.setOnClickListener(v -> runGreedy());
        btnReset.setOnClickListener(v -> generateMaze());

        btnNext.setOnClickListener(v -> {
            level = level == 2 ? 1 : level + 1;
            generateMaze();
        });

        btnNextPath.setOnClickListener(
                v -> showBrutePath(currentPathIndex + 1));

        btnPrevPath.setOnClickListener(
                v -> showBrutePath(currentPathIndex - 1));
        btnPlay.setOnClickListener(v -> beginPlay());
        btnClearRun.setOnClickListener(v -> clearPlayerRun());
        mazeView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                handleMazeTap(event.getX(), event.getY());
            }
            if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return true;
        });

        btnNextPath.setEnabled(false);
        btnPrevPath.setEnabled(false);

        generateMaze();
        mazeView.post(this::showStoryDialog);
    }

    // ────────────────────────────────────────────────────────────
    //  STORY DIALOG (polished custom layout)
    // ────────────────────────────────────────────────────────────

    private void showStoryDialog() {
        Dialog dialog = new Dialog(this, R.style.StoryDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_story);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.88);
            dialog.getWindow().setAttributes(params);
            dialog.getWindow().setDimAmount(0.75f);
        }

        Button btnStart = dialog.findViewById(R.id.btnStartGame);
        btnStart.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // ────────────────────────────────────────────────────────────
    //  MAZE GENERATION
    // ────────────────────────────────────────────────────────────

    private void generateMaze() {

        cancelAnimation();
        maze = new int[N][N];
        boolean[][] safe = new boolean[N][N];

        if (level == 2) {
            // Fixed trap: greedy best-first search takes 24 moves, while
            // brute force finds the 18-move optimum.
            String[] hardMaze = {
                    "..###.#.#.",
                    "..#.####.#",
                    ".....#####",
                    ".#.#.#.#..",
                    "##........",
                    ".#...#.#.#",
                    ".##....#..",
                    "..#...###.",
                    "##..#..#..",
                    ".########."
            };

            for (int r = 0; r < N; r++) {
                for (int c = 0; c < N; c++) {
                    maze[r][c] = hardMaze[r].charAt(c) == '#' ? 1 : 0;
                }
            }
        } else {
            // Five guaranteed routes
            int[] rows = {1, 3, 5, 7, 8};

            // Initially all cells are walls
            for (int r = 0; r < N; r++) {
                for (int c = 0; c < N; c++) {
                    maze[r][c] = 1;
                }
            }

            // Create five guaranteed paths
            for (int row : rows) {

                for (int r = 0; r <= row; r++) {
                    safe[r][0] = true;
                }

                for (int c = 0; c < N; c++) {
                    safe[row][c] = true;
                }

                for (int r = row; r < N; r++) {
                    safe[r][N - 1] = true;
                }
            }

            // Open guaranteed paths
            for (int r = 0; r < N; r++) {
                for (int c = 0; c < N; c++) {
                    if (safe[r][c]) {
                        maze[r][c] = 0;
                    }
                }
            }

            // Keep the easy maze deterministic so both scenarios are repeatable.
            Random levelRandom = new Random(100);
            double openingChance = 0.08;

            for (int r = 0; r < N; r++) {
                for (int c = 1; c < N - 1; c++) {

                    if (!safe[r][c] &&
                            levelRandom.nextDouble() < openingChance) {
                        maze[r][c] = 0;
                    }
                }
            }
        }

        resetDisplay();
        mazeView.setMaze(maze);

        String levelLabel = level == 1 ? "EASY" : "HARD";
        tvLevel.setText("LVL " + level + " · " + levelLabel);

        tvStatus.setText("Tap PLAY to start, or run a simulation");
        tvAlgorithm.setText("READY");
    }

    private void resetDisplay() {

        explored.clear();
        bestPath.clear();
        bruteBestPath.clear();
        allPaths.clear();

        visited = new boolean[N][N];

        pathsFound = 0;
        nodesVisited = 0;
        currentPathIndex = 0;

        bruteTime = -1;
        greedyTime = -1;

        bruteLength = -1;
        greedyLength = -1;
        userLength = -1;
        isPlaying = false;
        userPath.clear();
        btnPlay.setText("▶  PLAY");

        heuristicTrace = "";

        btnNextPath.setEnabled(false);
        btnPrevPath.setEnabled(false);

        mazeView.clearMaze();
        mazeView.setPlayer(0, 0);

        // Reset table
        resetTableCells();
    }

    private void resetTableCells() {
        String dash = "—";
        tvBruteLength.setText(dash);
        tvHeuristicLength.setText(dash);
        tvUserLength.setText(dash);
        tvBruteNodes.setText(dash);
        tvHeuristicNodes.setText(dash);
        tvBruteTime.setText(dash);
        tvHeuristicTime.setText(dash);
        tvBrutePaths.setText(dash);
        tvBruteOptimal.setText(dash);
        tvHeuristicOptimal.setText(dash);
        tvUserOptimal.setText(dash);

        // Reset colors
        int muted = Color.parseColor("#8899AA");
        tvBruteLength.setTextColor(muted);
        tvHeuristicLength.setTextColor(muted);
        tvUserLength.setTextColor(muted);
        tvBruteOptimal.setTextColor(muted);
        tvHeuristicOptimal.setTextColor(muted);
        tvUserOptimal.setTextColor(muted);

        cardVerdict.setVisibility(View.GONE);
        cardHeuristic.setVisibility(View.GONE);
    }

    // ────────────────────────────────────────────────────────────
    //  BRUTE FORCE DFS
    // ────────────────────────────────────────────────────────────

    private void runBruteForce() {

        if (isAnimating) {
            return;
        }

        tvStatus.setText("⚡ Brute force exploring all routes...");
        tvAlgorithm.setText("BRUTE FORCE");

        // Reset Brute Force results
        allPaths.clear();
        pathsFound = 0;
        nodesVisited = 0;
        currentPathIndex = 0;

        visited = new boolean[N][N];

        long startTime = System.nanoTime();

        bruteDFS(0, 0, new ArrayList<>());

        long endTime = System.nanoTime();

        bruteTime = (endTime - startTime) / 1_000_000.0;

        if (allPaths.isEmpty()) {
            tvStatus.setText("No path found!");
            setBusy(false);
            return;
        }

        bruteLength = bruteBestPath.size() - 1;

        updateComparisonTable();
        animateBrutePath(0);
    }

    private void bruteDFS(int r, int c, ArrayList<int[]> current) {

        if (!bruteBestPath.isEmpty() && current.size() >= bruteBestPath.size()) {
            return;
        }

        if (r < 0 || c < 0 || r >= N || c >= N) {
            return;
        }

        if (maze[r][c] == 1 || visited[r][c]) {
            return;
        }

        visited[r][c] = true;
        nodesVisited++;

        current.add(new int[]{r, c});

        if (r == N - 1 && c == N - 1) {

            ArrayList<int[]> copy = new ArrayList<>();

            for (int[] cell : current) {
                copy.add(new int[]{cell[0], cell[1]});
            }

            if (bruteBestPath.isEmpty() || copy.size() < bruteBestPath.size()) {
                bruteBestPath = copy;
            }
            if (allPaths.size() < MAX_PATHS) {
                allPaths.add(copy);
            }
            pathsFound = allPaths.size();

        } else {

            for (int i = 0; i < 4; i++) {

                bruteDFS(
                        r + dr[i],
                        c + dc[i],
                        current
                );
            }
        }

        current.remove(current.size() - 1);
        visited[r][c] = false;
    }

    private void animateBrutePath(int index) {
        if (index < 0 || index >= allPaths.size()) {
            setBusy(false);
            return;
        }

        setBusy(true);
        currentPathIndex = index;
        mazeView.clearMaze();
        mazeView.setMaze(maze);

        ArrayList<int[]> selectedPath = allPaths.get(index);
        final int[] cellIndex = {0};
        tvStatus.setText("⚡ Replaying brute force path " + (index + 1) + "...");

        Runnable revealPath = new Runnable() {
            @Override
            public void run() {
                if (cellIndex[0] < selectedPath.size()) {
                    int[] cell = selectedPath.get(cellIndex[0]++);
                    mazeView.setPath(cell[0], cell[1]);
                    animationHandler.postDelayed(this, 24);
                    return;
                }

                setBusy(false);
                showBrutePath(index);
                updateOutcomeStatus();
            }
        };
        animationHandler.post(revealPath);
    }

    // Display one complete Brute Force path.
    private void showBrutePath(int index) {

        if (index < 0 || index >= allPaths.size()) {
            return;
        }

        currentPathIndex = index;

        ArrayList<int[]> selectedPath = allPaths.get(index);

        mazeView.clearMaze();
        mazeView.setMaze(maze);

        for (int[] cell : selectedPath) {
            mazeView.setPath(cell[0], cell[1]);
        }

        String shortestText = "";

        if (bruteBestPath != null && !bruteBestPath.isEmpty()
                && selectedPath.size() == bruteBestPath.size()) {
            shortestText = " ★";
        }

        tvStatus.setText(
                "⚡ Path " + (index + 1) + " of " + allPaths.size()
                        + shortestText
        );
        tvAlgorithm.setText("BRUTE · PATH " + (index + 1));

        btnPrevPath.setEnabled(index > 0);
        btnNextPath.setEnabled(index < allPaths.size() - 1);
    }

    // ────────────────────────────────────────────────────────────
    //  GREEDY BEST-FIRST SEARCH (Heuristic)
    // ────────────────────────────────────────────────────────────

    private int heuristic(int r, int c) {

        return Math.abs(9 - r) + Math.abs(9 - c);
    }

    private void runGreedy() {
        if (isAnimating) {
            return;
        }

        explored.clear();
        bestPath.clear();

        greedyTime = -1;
        greedyLength = -1;

        resetAlgorithmDisplay();

        long start = System.nanoTime();

        PriorityQueue<Node> pq = new PriorityQueue<>(
                (a, b) -> Integer.compare(
                        heuristic(a.r, a.c),
                        heuristic(b.r, b.c))
        );

        boolean[][] closed = new boolean[N][N];

        int[][] parentR = new int[N][N];
        int[][] parentC = new int[N][N];

        pq.add(new Node(0, 0));
        closed[0][0] = true;

        boolean found = false;

        StringBuilder trace = new StringBuilder();

        int count = 0;

        while (!pq.isEmpty()) {

            Node current = pq.poll();

            int r = current.r;
            int c = current.c;

            explored.add(new int[]{r, c});

            if (count < 12) {
                trace.append("(").append(r).append(",")
                        .append(c).append(") h=")
                        .append(heuristic(r, c)).append("\n");
                count++;
            }

            if (r == 9 && c == 9) {
                found = true;
                break;
            }

            for (int i = 0; i < 4; i++) {

                int nr = r + dr[i];
                int nc = c + dc[i];

                if (nr < 0 || nc < 0 ||
                        nr >= N || nc >= N) {
                    continue;
                }

                if (maze[nr][nc] == 1 || closed[nr][nc]) {
                    continue;
                }

                closed[nr][nc] = true;

                parentR[nr][nc] = r;
                parentC[nr][nc] = c;

                pq.add(new Node(nr, nc));
            }
        }

        if (found) {

            int r = 9;
            int c = 9;

            while (r != 0 || c != 0) {

                bestPath.add(new int[]{r, c});

                int pr = parentR[r][c];
                int pc = parentC[r][c];

                r = pr;
                c = pc;
            }

            bestPath.add(new int[]{0, 0});

            Collections.reverse(bestPath);
        }

        greedyTime = (System.nanoTime() - start) / 1000000.0;

        heuristicTrace = trace.toString();

        if (found) {
            greedyLength = bestPath.size() - 1;
        }

        updateComparisonTable();
        animateGreedyResult(found);
    }

    private void animateGreedyResult(boolean found) {
        setBusy(true);
        tvAlgorithm.setText("HEURISTIC");
        tvStatus.setText("🧠 Heuristic choosing nearest-looking cells...");
        mazeView.clearMaze();
        mazeView.setMaze(maze);

        final int[] exploredIndex = {0};
        final int[] pathIndex = {0};

        Runnable reveal = new Runnable() {
            @Override
            public void run() {
                if (exploredIndex[0] < explored.size()) {
                    int[] cell = explored.get(exploredIndex[0]++);
                    mazeView.setExploredCell(cell[0], cell[1]);
                    animationHandler.postDelayed(this, 18);
                    return;
                }

                if (pathIndex[0] < bestPath.size()) {
                    int[] cell = bestPath.get(pathIndex[0]++);
                    mazeView.setPath(cell[0], cell[1]);
                    animationHandler.postDelayed(this, 28);
                    return;
                }

                setBusy(false);
                tvStatus.setText(found
                        ? "🧠 Heuristic done — compare with brute force below"
                        : "No path found!");
                updateOutcomeStatus();
            }
        };
        animationHandler.post(reveal);
    }

    // ────────────────────────────────────────────────────────────
    //  PLAYER MODE
    // ────────────────────────────────────────────────────────────

    private void beginPlay() {
        if (isAnimating) {
            return;
        }

        clearPlayerRun();
        isPlaying = true;
        tvAlgorithm.setText("PLAYING");
        tvStatus.setText("🎮 Tap adjacent tiles to guide the runner to the cure!");
        btnPlay.setText("PLAYING...");
    }

    private void clearPlayerRun() {
        isPlaying = false;
        playerRow = 0;
        playerColumn = 0;
        userLength = -1;
        userPath.clear();
        userPath.add(new int[]{0, 0});
        mazeView.clearPlayer();
        mazeView.setPlayer(0, 0);
        mazeView.setUserPath(0, 0);
        btnPlay.setText("▶  PLAY");
        if (!isAnimating) {
            tvStatus.setText("Route cleared. Ready to go again.");
            tvAlgorithm.setText("READY");
        }
        updateComparisonTable();
    }

    private void handleMazeTap(float x, float y) {
        if (!isPlaying || isAnimating) {
            return;
        }

        float cell = Math.min(
                mazeView.getWidth() / (float) N,
                mazeView.getHeight() / (float) N
        );

        // Account for maze centering offset
        float offsetX = (mazeView.getWidth() - cell * N) / 2f;
        float offsetY = (mazeView.getHeight() - cell * N) / 2f;

        int column = (int) ((x - offsetX) / cell);
        int row = (int) ((y - offsetY) / cell);

        if (row < 0 || column < 0 || row >= N || column >= N) {
            return;
        }

        int distance = Math.abs(row - playerRow)
                + Math.abs(column - playerColumn);
        if (distance != 1) {
            tvStatus.setText("Choose a tile directly next to the runner.");
            return;
        }
        if (maze[row][column] == 1) {
            tvStatus.setText("That route is blocked. Try another tile.");
            return;
        }

        playerRow = row;
        playerColumn = column;
        userLength++;
        userPath.add(new int[]{row, column});
        mazeView.setUserPath(row, column);
        mazeView.setPlayer(row, column);

        if (row == N - 1 && column == N - 1) {
            isPlaying = false;
            btnPlay.setText("▶  AGAIN");
            tvAlgorithm.setText("WINNER!");
            tvStatus.setText("🎉 You reached the cure in " + userLength + " moves!");
            updateComparisonTable();
        } else {
            tvStatus.setText("🎮 " + userLength
                    + " move" + (userLength == 1 ? "" : "s") + " so far");
        }
    }

    // ────────────────────────────────────────────────────────────
    //  COMPARISON TABLE & VERDICT
    // ────────────────────────────────────────────────────────────

    private void updateComparisonTable() {
        int colorGreen = Color.parseColor("#22C55E");
        int colorRed = Color.parseColor("#EF4444");
        int colorBlue = Color.parseColor("#3B82F6");
        int colorPurple = Color.parseColor("#A855F7");
        int colorMuted = Color.parseColor("#8899AA");

        // ─── Brute Force column ───
        if (bruteTime >= 0) {
            tvBruteLength.setText(String.valueOf(bruteLength));
            tvBruteLength.setTextColor(colorBlue);
            tvBruteNodes.setText(String.valueOf(nodesVisited));
            tvBruteTime.setText(String.format(Locale.US, "%.2f", bruteTime));
            tvBrutePaths.setText(String.valueOf(pathsFound));
            tvBruteOptimal.setText("✓ YES");
            tvBruteOptimal.setTextColor(colorGreen);
        }

        // ─── Heuristic column ───
        if (greedyTime >= 0) {
            tvHeuristicLength.setText(String.valueOf(greedyLength));
            tvHeuristicNodes.setText(String.valueOf(explored.size()));
            tvHeuristicTime.setText(String.format(Locale.US, "%.2f", greedyTime));

            if (bruteLength >= 0) {
                if (greedyLength == bruteLength) {
                    tvHeuristicOptimal.setText("✓ YES");
                    tvHeuristicOptimal.setTextColor(colorGreen);
                    tvHeuristicLength.setTextColor(colorGreen);
                } else {
                    tvHeuristicOptimal.setText("✗ NO");
                    tvHeuristicOptimal.setTextColor(colorRed);
                    tvHeuristicLength.setTextColor(colorRed);
                }
            } else {
                tvHeuristicOptimal.setText("?");
                tvHeuristicOptimal.setTextColor(colorMuted);
                tvHeuristicLength.setTextColor(colorPurple);
            }

            // Show heuristic trace card
            if (!heuristicTrace.isEmpty()) {
                cardHeuristic.setVisibility(View.VISIBLE);
                tvHeuristicTrace.setText(heuristicTrace);
            }
        }

        // ─── User column ───
        if (userLength >= 0) {
            tvUserLength.setText(String.valueOf(userLength));

            if (bruteLength >= 0) {
                if (userLength == bruteLength) {
                    tvUserOptimal.setText("✓ YES");
                    tvUserOptimal.setTextColor(colorGreen);
                    tvUserLength.setTextColor(colorGreen);
                } else {
                    int extra = userLength - bruteLength;
                    tvUserOptimal.setText("+" + extra);
                    tvUserOptimal.setTextColor(colorRed);
                    tvUserLength.setTextColor(colorRed);
                }
            } else {
                tvUserOptimal.setText("?");
                tvUserOptimal.setTextColor(colorMuted);
                tvUserLength.setTextColor(Color.parseColor("#22C55E"));
            }
        }

        // ─── Build verdict ───
        updateVerdict();
    }

    private void updateVerdict() {
        StringBuilder verdict = new StringBuilder();
        boolean hasAny = false;

        if (bruteTime >= 0 && greedyTime >= 0) {
            hasAny = true;

            // Speed comparison
            if (greedyTime < bruteTime) {
                verdict.append("⏱  Heuristic was ")
                        .append(String.format(Locale.US, "%.1f", bruteTime / Math.max(greedyTime, 0.001)))
                        .append("× faster\n");
            } else if (bruteTime < greedyTime) {
                verdict.append("⏱  Brute Force was faster (")
                        .append(String.format(Locale.US, "%.2f", bruteTime))
                        .append(" ms vs ")
                        .append(String.format(Locale.US, "%.2f", greedyTime))
                        .append(" ms)\n");
            } else {
                verdict.append("⏱  Both took the same time\n");
            }

            // Path quality
            if (greedyLength > bruteLength) {
                verdict.append("📏  Brute Force found a shorter path (")
                        .append(bruteLength).append(" vs ").append(greedyLength).append(" moves)\n");
            } else if (greedyLength == bruteLength) {
                verdict.append("📏  Both found paths of the same length (").append(bruteLength).append(" moves)\n");
            }

            // Efficiency
            verdict.append("🔍  Brute Force explored ").append(nodesVisited)
                    .append(" nodes vs Heuristic's ").append(explored.size()).append("\n");

            // Key insight
            verdict.append("\n💡 Brute Force guarantees the optimal path but explores exhaustively. Heuristic is guided by h(n) and usually faster, but may miss the shortest route.");
        }

        if (userLength >= 0 && bruteLength >= 0) {
            hasAny = true;
            int diff = userLength - bruteLength;
            if (diff == 0) {
                verdict.append("\n\n🏆 You matched the optimal path!");
            } else {
                verdict.append("\n\n🎮 Your path was ").append(diff)
                        .append(" move").append(diff == 1 ? "" : "s")
                        .append(" longer than optimal.");
            }
        }

        if (hasAny) {
            cardVerdict.setVisibility(View.VISIBLE);
            tvVerdict.setText(verdict.toString().trim());
        }
    }

    private void updateOutcomeStatus() {
        if (bruteLength >= 0 && greedyLength >= 0) {
            if (greedyLength > bruteLength) {
                tvStatus.setText("Heuristic faster, but brute force found optimal");
            } else {
                tvStatus.setText("Both found same-length paths ✓");
            }
        }
    }

    // ────────────────────────────────────────────────────────────
    //  UTILITY
    // ────────────────────────────────────────────────────────────

    private void resetAlgorithmDisplay() {

        explored.clear();
        bestPath.clear();

        visited = new boolean[N][N];
        allPaths.clear();

        pathsFound = 0;
        nodesVisited = 0;
        currentPathIndex = 0;

        mazeView.clearMaze();

        btnNextPath.setEnabled(false);
        btnPrevPath.setEnabled(false);
    }

    private void setBusy(boolean busy) {
        isAnimating = busy;
        btnBrute.setEnabled(!busy);
        btnHeuristic.setEnabled(!busy);
        btnReset.setEnabled(!busy);
        btnNext.setEnabled(!busy);
        btnPlay.setEnabled(!busy);
        btnClearRun.setEnabled(!busy);
        progressAnimation.setVisibility(busy ? View.VISIBLE : View.GONE);
    }

    private void cancelAnimation() {
        animationHandler.removeCallbacksAndMessages(null);
        setBusy(false);
    }

    @Override
    protected void onDestroy() {
        cancelAnimation();
        super.onDestroy();
    }
}
