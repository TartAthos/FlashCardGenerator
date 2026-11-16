package com.example.falshcardgenerator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.falshcardgenerator.db.FlashcardDao;
import com.example.falshcardgenerator.model.Card;
import com.example.falshcardgenerator.model.Deck;

import java.util.List;

public class EditDeckActivity extends AppCompatActivity {

    private FlashcardDao dao;
    private long deckId = -1L; // -1L signifie "Mode Création"

    private TextView tvScreenTitle;
    private EditText etDeckTitle, etDeckDescription;
    private LinearLayout containerEditCards;
    private Button btnAddCardRow, btnSaveDeck, btnDeleteDeck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_deck);

        dao = new FlashcardDao(this);

        // Récupération des vues
        tvScreenTitle = findViewById(R.id.tvScreenTitle);
        etDeckTitle = findViewById(R.id.etDeckTitle);
        etDeckDescription = findViewById(R.id.etDeckDescription);
        containerEditCards = findViewById(R.id.containerEditCards);
        btnAddCardRow = findViewById(R.id.btnAddCardRow);
        btnSaveDeck = findViewById(R.id.btnSaveDeck);
        btnDeleteDeck = findViewById(R.id.btnDeleteDeck);

        // Vérifie si un DECK_ID a été passé par MainActivity
        deckId = getIntent().getLongExtra("DECK_ID", -1L);

        if (deckId != -1L) {
            // --- MODE MODIFICATION ---
            tvScreenTitle.setText("Modifier le Deck");
            btnDeleteDeck.setVisibility(View.VISIBLE);
            loadExistingDeckData();
        } else {
            // --- MODE CRÉATION ---
            tvScreenTitle.setText("Nouveau Deck");
            // On ajoute une première carte vide par défaut pour guider l'utilisateur
            addCardRow("", "");
        }

        // Bouton "+ Ajouter une carte"
        btnAddCardRow.setOnClickListener(v -> addCardRow("", ""));

        // Bouton "Enregistrer"
        btnSaveDeck.setOnClickListener(v -> saveDeckAndCards());

        // Bouton "Supprimer" (uniquement visible en mode modification)
        btnDeleteDeck.setOnClickListener(v -> {
            dao.deleteDeck(deckId);
            Toast.makeText(this, "Deck supprimé", Toast.LENGTH_SHORT).show();
            finish(); // Ferme l'écran et revient à MainActivity
        });
    }

    private void loadExistingDeckData() {
        Deck deck = dao.getDeckById(deckId);
        if (deck != null) {
            etDeckTitle.setText(deck.getTitle());
            etDeckDescription.setText(deck.getDescription());
        }

        // Charge les cartes existantes du deck
        List<Card> cards = dao.getCardsForDeck(deckId);
        for (Card card : cards) {
            addCardRow(card.getQuestion(), card.getAnswer());
        }
    }

    /**
     * Ajoute visuellement une ligne de carte (Question + Réponse + bouton X) dans le ScrollView.
     */
    private void addCardRow(String question, String answer) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View rowView = inflater.inflate(R.layout.item_edit_card, containerEditCards, false);

        EditText etQuestion = rowView.findViewById(R.id.CardQuestion);
        EditText etAnswer = rowView.findViewById(R.id.CardAnswer);
        Button btnDeleteRow = rowView.findViewById(R.id.btnDeleteCardRow);

        etQuestion.setText(question);
        etAnswer.setText(answer);

        // Supprime la ligne de l'écran au clic sur "X"
        btnDeleteRow.setOnClickListener(v -> containerEditCards.removeView(rowView));

        containerEditCards.addView(rowView);
    }

    private void saveDeckAndCards() {
        String title = etDeckTitle.getText().toString().trim();
        String description = etDeckDescription.getText().toString().trim();

        if (title.isEmpty()) {
            etDeckTitle.setError("Le titre est obligatoire");
            return;
        }

        if (deckId == -1L) {
            // 1. CRÉATION : on insère le nouveau deck et on récupère son ID généré
            deckId = dao.insertDeck(new Deck(title, description));
        } else {
            // 2. MODIFICATION : on met à jour le deck et on nettoie ses anciennes cartes
            dao.updateDeck(new Deck(deckId, title, description));
            List<Card> oldCards = dao.getCardsForDeck(deckId);
            for (Card old : oldCards) {
                dao.deleteCard(old.getId());
            }
        }

        // 3. Parcourt toutes les lignes visibles dans containerEditCards pour les enregistrer
        int rowCount = containerEditCards.getChildCount();
        for (int i = 0; i < rowCount; i++) {
            View rowView = containerEditCards.getChildAt(i);
            EditText etQuestion = rowView.findViewById(R.id.CardQuestion);
            EditText etAnswer = rowView.findViewById(R.id.CardAnswer);

            String q = etQuestion.getText().toString().trim();
            String a = etAnswer.getText().toString().trim();

            // On n'enregistre la carte que si la question et la réponse sont remplies
            if (!q.isEmpty() && !a.isEmpty()) {
                dao.insertCard(new Card(deckId, q, a));
            }
        }

        Toast.makeText(this, "Deck enregistré !", Toast.LENGTH_SHORT).show();
        finish(); // Retour automatique vers MainActivity (qui déclenchera onResume())
    }
}