package com.example.falshcardgenerator;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import com.example.falshcardgenerator.model.Card;
import com.example.falshcardgenerator.model.Deck;
import com.example.falshcardgenerator.db.FlashcardDao;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DB_TEST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Test du DAO : affichage des decks et des cartes
        FlashcardDao dao = new FlashcardDao(this);

        List<Deck> decks = dao.getAllDecks();
        Log.d(TAG, "Nombre de decks trouvés : " + decks.size());
        for (Deck deck : decks) {
            Log.d(TAG, "Deck [" + deck.getId() + "] : " + deck.getTitle() + " - " + deck.getDescription());
            List<Card> cards = dao.getCardsForDeck(deck.getId());
            for (Card card : cards) {
                Log.d(TAG, "   -> Carte [" + card.getId() + "] : " + card.getQuestion() + " = " + card.getAnswer());
            }
        }
    }
}