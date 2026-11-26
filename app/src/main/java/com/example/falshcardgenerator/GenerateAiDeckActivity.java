package com.example.falshcardgenerator;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.falshcardgenerator.ai.AiDeckGenerator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GenerateAiDeckActivity extends AppCompatActivity {

    private static final int MIN_CARDS = 3;

    private EditText etTheme;
    private EditText etSubTheme;
    private TextView tvCardCountLabel;
    private SeekBar seekBarCardCount;
    private ProgressBar progressBar;
    private TextView tvAiStatus;
    private Button btnGenerateDeck;

    private int selectedCardCount = 5;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_ai_deck);

        etTheme = findViewById(R.id.etTheme);
        etSubTheme = findViewById(R.id.etSubTheme);
        tvCardCountLabel = findViewById(R.id.tvCardCountLabel);
        seekBarCardCount = findViewById(R.id.seekBarCardCount);
        progressBar = findViewById(R.id.progressAiGeneration);
        tvAiStatus = findViewById(R.id.tvAiStatus);
        btnGenerateDeck = findViewById(R.id.btnGenerateDeck);

        // Initialisation du curseur (progress = 2 -> 3 + 2 = 5 cartes par défaut)
        seekBarCardCount.setProgress(selectedCardCount - MIN_CARDS);
        tvCardCountLabel.setText("Nombre de cartes à générer : " + selectedCardCount);

        seekBarCardCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedCardCount = MIN_CARDS + progress;
                tvCardCountLabel.setText("Nombre de cartes à générer : " + selectedCardCount);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnGenerateDeck.setOnClickListener(v -> startDeckGeneration());
    }

    private void startDeckGeneration() {
        String theme = etTheme.getText().toString().trim();
        String subTheme = etSubTheme.getText().toString().trim();

        if (theme.isEmpty()) {
            etTheme.setError("Veuillez saisir un thème");
            etTheme.requestFocus();
            return;
        }

        final int cardCountToGenerate = selectedCardCount;
        setLoadingState(true);

        executor.execute(() -> {
            AiDeckGenerator generator = new AiDeckGenerator(this);
            long newDeckId = generator.generateAndSaveDeck(theme, subTheme, cardCountToGenerate);

            mainHandler.post(() -> {
                setLoadingState(false);

                if (newDeckId != -1L) {
                    Toast.makeText(this, cardCountToGenerate + " cartes générées ! Vérifiez et validez votre deck.", Toast.LENGTH_SHORT).show();

                    Intent editIntent = new Intent(GenerateAiDeckActivity.this, EditDeckActivity.class);
                    editIntent.putExtra("DECK_ID", newDeckId);
                    startActivity(editIntent);
                    finish();
                } else {
                    Toast.makeText(this, "Erreur lors de la génération (vérifiez votre connexion ou Logcat)", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void setLoadingState(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        tvAiStatus.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnGenerateDeck.setEnabled(!isLoading);
        etTheme.setEnabled(!isLoading);
        etSubTheme.setEnabled(!isLoading);
        seekBarCardCount.setEnabled(!isLoading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}