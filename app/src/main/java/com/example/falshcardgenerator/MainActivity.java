package com.example.falshcardgenerator;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import com.example.falshcardgenerator.model.Card;
import com.example.falshcardgenerator.model.Deck;
import com.example.falshcardgenerator.db.FlashcardDao;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DB_TEST";

    private FlashcardDao dao;
    private LinearLayout containerDecks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dao = new FlashcardDao(this);
        containerDecks = findViewById(R.id.containerDecks);

        displayDecks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayDecks();
    }

    private void displayDecks() {
        containerDecks.removeAllViews();

        List<Deck> decks = dao.getAllDecks();

        // LayoutInflater permet de transformer un fichier XML (item_deck.xml) en objet View Java
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Deck deck : decks) {
            // 1. Création d'une nouvelle carte à partir du modèle XML
            View cardView = inflater.inflate(R.layout.item_deck, containerDecks, false);

            // 2. Récupération des composants à l'intérieur de cette carte précise
            TextView tvTitle = cardView.findViewById(R.id.tvDeckTitle);
            TextView tvDescription = cardView.findViewById(R.id.tvDeckDescription);
            Button btnEdit = cardView.findViewById(R.id.btnEditDeck);
            Button btnPlay = cardView.findViewById(R.id.btnPlayDeck);

            // 3. Remplissage avec les données du deck SQLite
            tvTitle.setText(deck.getTitle());
            tvDescription.setText(deck.getDescription());

            // 4. Gestion du clic sur "Jouer"
            btnPlay.setOnClickListener(v -> {
                Toast.makeText(this, "Lancement du jeu : " + deck.getTitle(), Toast.LENGTH_SHORT).show();
                // Plus tard : ouvrir l'activité de révision en lui passant l'ID du deck
                // Intent intent = new Intent(MainActivity.this, PlayDeckActivity.class);
                // intent.putExtra("DECK_ID", deck.getId());
                // startActivity(intent);
            });

            // 5. Gestion du clic sur "Modifier"
            btnEdit.setOnClickListener(v -> {
                Toast.makeText(this, "Modification du deck : " + deck.getTitle(), Toast.LENGTH_SHORT).show();
                // Plus tard : ouvrir l'activité d'édition en lui passant l'ID du deck
                // Intent intent = new Intent(MainActivity.this, EditDeckActivity.class);
                // intent.putExtra("DECK_ID", deck.getId());
                // startActivity(intent);
            });

            // 6. Ajout de la carte remplie dans le LinearLayout du ScrollView
            containerDecks.addView(cardView);
        }
    }
}