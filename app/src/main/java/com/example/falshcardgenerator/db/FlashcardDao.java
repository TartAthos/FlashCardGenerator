package com.example.falshcardgenerator.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.falshcardgenerator.model.Card;
import com.example.falshcardgenerator.model.Deck;

import java.util.ArrayList;
import java.util.List;

public class FlashcardDao {

    private final DatabaseHelper dbHelper;

    // Constructeur : on récupère le DatabaseHelper pour accéder à la BDD
    public FlashcardDao(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    // =========================================================================
    // 1. OPÉRATIONS SUR LES DECKS
    // =========================================================================

    /**
     * Récupère la liste complète des decks enregistrés en base.
     */
    public List<Deck> getAllDecks() {
        List<Deck> deckList = new ArrayList<>();
        // Ouverture de la base en mode lecture
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Exécution de la requête : SELECT * FROM decks
        // Les paramètres null correspondent aux filtres (WHERE, GROUP BY, ORDER BY...)
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_DECKS,
                null, // null = récupérer toutes les colonnes
                null, null, null, null, null
        );

        // Parcours des résultats ligne par ligne
        if (cursor.moveToFirst()) {
            do {
                // Extraction des données de la ligne actuelle grâce au nom des colonnes
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DECK_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DECK_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DECK_DESCRIPTION));

                // Transformation de la ligne SQL en objet Java Deck
                Deck deck = new Deck(id, title, description);
                deckList.add(deck);
            } while (cursor.moveToNext()); // Passe à la ligne suivante tant qu'il y en a
        }

        // Fermeture obligatoire du curseur pour libérer la mémoire
        cursor.close();
        return deckList;
    }

    /**
     * Insère un nouveau deck en base et retourne son ID généré.
     */
    public long insertDeck(Deck deck) {
        // Ouverture en mode écriture
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Préparation des paires (Nom de colonne -> Valeur)
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_DECK_TITLE, deck.getTitle());
        values.put(DatabaseHelper.COLUMN_DECK_DESCRIPTION, deck.getDescription());

        // Insertion sécurisée : renvoie l'ID de la nouvelle ligne (ou -1 en cas d'erreur)
        return db.insert(DatabaseHelper.TABLE_DECKS, null, values);
    }

    /**
     * Supprime un deck (et ses cartes automatiquement grâce au ON DELETE CASCADE).
     */
    public void deleteDeck(long deckId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Le "?" est un paramètre sécurisé remplacé par String.valueOf(deckId)
        db.delete(
                DatabaseHelper.TABLE_DECKS,
                DatabaseHelper.COLUMN_DECK_ID + " = ?",
                new String[]{String.valueOf(deckId)}
        );
    }

    // =========================================================================
    // 2. OPÉRATIONS SUR LES CARTES (CARDS)
    // =========================================================================

    /**
     * Récupère toutes les cartes appartenant à un deck précis.
     */
    public List<Card> getCardsForDeck(long deckId) {
        List<Card> cardList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Clause WHERE : deck_id = ?
        String selection = DatabaseHelper.COLUMN_CARD_DECK_ID + " = ?";
        String[] selectionArgs = {String.valueOf(deckId)};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_CARDS,
                null,
                selection,
                selectionArgs,
                null, null, null
        );

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CARD_ID));
                long dId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CARD_DECK_ID));
                String question = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CARD_QUESTION));
                String answer = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CARD_ANSWER));

                cardList.add(new Card(id, dId, question, answer));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return cardList;
    }

    /**
     * Ajoute une nouvelle carte dans un deck existant.
     */
    public long insertCard(Card card) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CARD_DECK_ID, card.getDeckId());
        values.put(DatabaseHelper.COLUMN_CARD_QUESTION, card.getQuestion());
        values.put(DatabaseHelper.COLUMN_CARD_ANSWER, card.getAnswer());

        return db.insert(DatabaseHelper.TABLE_CARDS, null, values);
    }

    /**
     * Modifie la question et la réponse d'une carte existante.
     */
    public int updateCard(Card card) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CARD_QUESTION, card.getQuestion());
        values.put(DatabaseHelper.COLUMN_CARD_ANSWER, card.getAnswer());

        // Met à jour la ligne où id = card.getId()
        return db.update(
                DatabaseHelper.TABLE_CARDS,
                values,
                DatabaseHelper.COLUMN_CARD_ID + " = ?",
                new String[]{String.valueOf(card.getId())}
        );
    }

    /**
     * Supprime une carte spécifique par son ID.
     */
    public void deleteCard(long cardId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(
                DatabaseHelper.TABLE_CARDS,
                DatabaseHelper.COLUMN_CARD_ID + " = ?",
                new String[]{String.valueOf(cardId)}
        );
    }
}
