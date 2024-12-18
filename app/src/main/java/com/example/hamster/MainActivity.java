package com.example.hamster;

import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import android.content.SharedPreferences;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_IS_DARK_MODE = "is_dark_mode";
    private static final String KEY_WINS = "wins";
    private static final String KEY_LOSSES = "losses";
    private static final String KEY_DRAWS = "draws";

    private int currentPlayer = 1; // 1 - игрок, 2 - бот или 2 - второй игрок
    private int[][] board = new int[3][3];
    private Button[][] buttons = new Button[3][3];
    private Switch darkModeSwitch;

    private int wins = 0;
    private int losses = 0;
    private int draws = 0;
    private boolean isGameFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Применяем тему перед загрузкой интерфейса
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        darkModeSwitch.setChecked(isDarkModeEnabled());

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveThemePreference(isChecked);
            recreate(); // Перезагружаем активность для применения новой темы
        });

        // Выбор режима игры
        showGameModeSelectionDialog();

        Button resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(v -> resetGame());

        loadStatistics();
        updateStatisticsTextView(); // Обновляем статистику при загрузке
    }

    private void showGameModeSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите режим игры")
                .setItems(new CharSequence[]{"С ботом", "С другом"}, (dialog, which) -> {
                    if (which == 0) {
                        currentPlayer = 1; // Игрок против бота
                        startGameWithBot();
                    } else {
                        currentPlayer = 1; // Игрок против игрока
                        startGameWithFriend();
                    }
                })
                .show();
    }

    private void startGameWithBot() {
        initializeGame();
        isGameFinished = false; // Сбрасываем флаг завершения игры
        // Логика для игры с ботом
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                final int row = i;
                final int col = j;
                buttons[i][j].setOnClickListener(v -> {
                    if (currentPlayer == 1) {
                        makeMove(row, col);
                        if (!isGameFinished) { // Проверяем, завершена ли игра
                            botMove();
                        }
                    }
                });
            }
        }
    }

    private void startGameWithFriend() {
        initializeGame();
        isGameFinished = false; // Сбрасываем флаг завершения игры
        // Логика для игры с другом
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                final int row = i;
                final int col = j;
                buttons[i][j].setOnClickListener(v -> {
                    makeMove(row, col);
                    if (isGameFinished) { // Если игра завершена, перезапускаем
                        restartGame();
                    }
                });
            }
        }
    }

    private void initializeGame() {
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j] = (Button) gridLayout.getChildAt(i * 3 + j);
                buttons[i][j].setText("");
                buttons[i][j].setEnabled(true);
            }
        }
        board = new int[3][3]; // Сбросить игровое поле
    }

    private void makeMove(int row, int col) {
        if (board[row][col] != 0 || isGameFinished) { // Проверяем, завершена ли игра
            return;
        }
        board[row][col] = currentPlayer;
        buttons[row][col].setText(currentPlayer == 1 ? "X" : "O");

        if (checkWin()) {
            isGameFinished = true; // Устанавливаем флаг завершения игры
            if (currentPlayer == 1) {
                wins++;
                Toast.makeText(this, "Вы победили!", Toast.LENGTH_SHORT).show();
            } else {
                losses++;
                Toast.makeText(this, "Второй игрок победил!", Toast.LENGTH_SHORT).show();
            }
            saveStatistics();
            updateStatisticsTextView(); // Обновляем статистику после игры
            disableButtons();
            showRestartDialog(); // Показываем диалог для перезапуска игры
        } else if (isBoardFull()) {
            isGameFinished = true; // Устанавливаем флаг завершения игры
            draws++;
            Toast.makeText(this, "Ничья!", Toast.LENGTH_SHORT).show();
            saveStatistics();
            updateStatisticsTextView(); // Обновляем статистику после игры
            disableButtons();
            showRestartDialog(); // Показываем диалог для перезапуска игры
        } else {
            currentPlayer = (currentPlayer == 1) ? 2 : 1; // Переключаем игрока
        }
    }

    private void showRestartDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Игра завершена")
                .setMessage("Хотите сыграть заново?")
                .setPositiveButton("Да", (dialog, which) -> restartGame())
                .setNegativeButton("Нет", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.black)); // Установите нужный цвет
            negativeButton.setTextColor(getResources().getColor(R.color.black)); // Установите нужный цвет
        });
        dialog.show();
    }
    private void restartGame() {
        initializeGame();
        isGameFinished = false; // Сбрасываем флаг завершения игры
    }

    private void botMove() {
        Random random = new Random();
        int row, col;
        do {
            row = random.nextInt(3);
            col = random.nextInt(3);
        } while (board[row][col] != 0);
        makeMove(row, col);
    }

    private boolean checkWin() {
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == currentPlayer && board[i][1] == currentPlayer && board[i][2] == currentPlayer) return true;
            if (board[0][i] == currentPlayer && board[1][i] == currentPlayer && board[2][i] == currentPlayer) return true;
        }
        return (board[0][0] == currentPlayer && board[1][1] == currentPlayer && board[2][2] == currentPlayer) ||
                (board[0][2] == currentPlayer && board[1][1] == currentPlayer && board[2][0] == currentPlayer);
    }

    private boolean isBoardFull() {
        for (int[] row : board) {
            for (int cell : row) {
                if (cell == 0) return false;
            }
        }
        return true;
    }

    private void disableButtons() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setEnabled(false);
            }
        }
    }

    private void resetGame() {
        initializeGame();
        wins = 0;
        losses = 0;
        draws = 0;
        updateStatisticsTextView(); // Обновляем статистику после сброса
    }

    private void loadStatistics() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        wins = prefs.getInt(KEY_WINS, 0);
        losses = prefs.getInt(KEY_LOSSES, 0);
        draws = prefs.getInt(KEY_DRAWS, 0);
    }

    private void saveStatistics() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_WINS, wins);
        editor.putInt(KEY_LOSSES, losses);
        editor.putInt(KEY_DRAWS, draws);
        editor.apply();
    }

    private void updateStatisticsTextView() {
        TextView statsTextView = findViewById(R.id.statisticsTextView);
        statsTextView.setText("Победы: " + wins + "\nПоражения: " + losses + "\nНичьи: " + draws);
    }

    private void applyTheme() {
        if (isDarkModeEnabled()) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme_Light);
        }
    }

    private boolean isDarkModeEnabled() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_DARK_MODE, false);
    }
    private void saveThemePreference(boolean isDarkMode) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_DARK_MODE, isDarkMode);
        editor.apply();
    }
}