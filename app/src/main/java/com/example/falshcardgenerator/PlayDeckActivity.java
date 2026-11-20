package com.example.falshcardgenerator;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.falshcardgenerator.db.FlashcardDao;
import com.example.falshcardgenerator.model.Card;
import com.example.falshcardgenerator.model.Deck;

import java.util.Collections;
import java.util.List;

public class PlayDeckActivity extends AppCompatActivity {

    private FlashcardDao dao;
    private List<Card> cardList;
    private int currentIndex = 0;
    private boolean isShowingAnswer = false;

    private TextView PlayDeckTitle, CardProgress, CardSideLabel, CardContent;
    private CardView cardFlashcard;
    private ConstraintLayout cardInnerContainer;
    private Button btnFlipCard, btnNextCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_deck);

        dao = new FlashcardDao(this);

        // 1. Récupération des vues
        PlayDeckTitle = findViewById(R.id.PlayDeckTitle);
        CardProgress = findViewById(R.id.CardProgress);
        CardSideLabel = findViewById(R.id.CardSideLabel);
        CardContent = findViewById(R.id.CardContent);
        cardFlashcard = findViewById(R.id.cardFlashcard);
        cardInnerContainer = findViewById(R.id.cardInnerContainer);
        btnNextCard = findViewById(R.id.btnNextCard);

        // 2. Récupération de l'ID du deck transmis par MainActivity
        long deckId = getIntent().getLongExtra("DECK_ID", -1L);
        Deck deck = dao.getDeckById(deckId);
        cardList = dao.getCardsForDeck(deckId);

        // Sécurité : si le deck n'existe pas ou n'a aucune carte
        if (deck == null || cardList.isEmpty()) {
            Toast.makeText(this, "Ce deck ne contient aucune carte !", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        PlayDeckTitle.setText(deck.getTitle());

        // Optionnel : mélanger l'ordre des cartes à chaque partie
        Collections.shuffle(cardList);

        // 3. Affichage de la première carte
        showCurrentCard();

        // 4. Retourner la carte (au clic sur la carte ou sur le bouton)
        View.OnClickListener flipListener = v -> flipCard();
        cardFlashcard.setOnClickListener(flipListener);

        // 5. Passer à la carte suivante
        btnNextCard.setOnClickListener(v -> nextCard());
    }

    private void showCurrentCard() {
        Card currentCard = cardList.get(currentIndex);
        isShowingAnswer = false;

        // Mise à jour du compteur (ex: "1 / 4")
        CardProgress.setText((currentIndex + 1) + " / " + cardList.size());

        // Affichage de la face Question
        CardSideLabel.setText("QUESTION");
        CardSideLabel.setTextColor(Color.parseColor("#3F51B5"));
        CardContent.setText(currentCard.getQuestion());
        cardInnerContainer.setBackgroundColor(Color.WHITE);

        // Changer le texte du bouton sur la dernière carte
        if (currentIndex == cardList.size() - 1) {
            btnNextCard.setText("Terminer");
        } else {
            btnNextCard.setText("Suivante");
        }
    }

    private void flipCard() {
        Card currentCard = cardList.get(currentIndex);
        isShowingAnswer = !isShowingAnswer;

        cardFlashcard.animate()
                .rotationY(90f)
                .setDuration(150)
                .withEndAction(() -> {
                    if (isShowingAnswer) {
                        CardSideLabel.setText("RÉPONSE");
                        CardSideLabel.setTextColor(Color.parseColor("#2E7D32"));
                        CardContent.setText(currentCard.getAnswer());
                        cardInnerContainer.setBackgroundColor(Color.parseColor("#E8F5E9"));
                    } else {
                        CardSideLabel.setText("QUESTION");
                        CardSideLabel.setTextColor(Color.parseColor("#3F51B5"));
                        CardContent.setText(currentCard.getQuestion());
                        cardInnerContainer.setBackgroundColor(Color.WHITE);
                    }
                    cardFlashcard.setRotationY(-90f);
                    cardFlashcard.animate()
                            .rotationY(0f)
                            .setDuration(150)
                            .start();
                }).start();
    }

    private void nextCard() {
        if (currentIndex < cardList.size() - 1) {
            currentIndex++;
            showCurrentCard();
        } else {
            // Fin du paquet : boîte de dialogue pour rejouer ou quitter
            new AlertDialog.Builder(this)
                    .setTitle("Session terminée !")
                    .setMessage("Vous avez révisé les " + cardList.size() + " cartes de ce deck.")
                    .setPositiveButton("Rejouer", (dialog, which) -> {
                        Collections.shuffle(cardList);
                        currentIndex = 0;
                        showCurrentCard();
                    })
                    .setNegativeButton("Retour aux decks", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        }
    }
}