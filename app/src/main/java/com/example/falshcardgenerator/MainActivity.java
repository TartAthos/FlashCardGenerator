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
import com.google.firebase.auth.FirebaseAuth;

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

        Button btnCreateDeck = findViewById(R.id.btnCreateDeck);
        btnCreateDeck.setOnClickListener(v -> {
            // On ouvre EditDeckActivity SANS extra DECK_ID -> Mode Création
            Intent intent = new Intent(MainActivity.this, EditDeckActivity.class);
            startActivity(intent);
        });

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

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

            View cardView = inflater.inflate(R.layout.item_deck, containerDecks, false);

            TextView tvTitle = cardView.findViewById(R.id.tvDeckTitle);
            TextView tvDescription = cardView.findViewById(R.id.tvDeckDescription);
            Button btnEdit = cardView.findViewById(R.id.btnEditDeck);
            Button btnPlay = cardView.findViewById(R.id.btnPlayDeck);

            tvTitle.setText(deck.getTitle());
            tvDescription.setText(deck.getDescription());

            btnPlay.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, PlayDeckActivity.class);
                intent.putExtra("DECK_ID", deck.getId());
                startActivity(intent);
            });

            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, EditDeckActivity.class);
                intent.putExtra("DECK_ID", deck.getId());
                startActivity(intent);
            });

            containerDecks.addView(cardView);
        }
    }
}