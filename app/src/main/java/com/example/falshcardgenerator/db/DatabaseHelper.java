package com.example.falshcardgenerator.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "flashcards.db";
    private static final int DATABASE_VERSION = 1;

    // Noms des tables
    public static final String TABLE_DECKS = "decks";
    public static final String TABLE_CARDS = "cards";

    // Colonnes Table Decks
    public static final String COLUMN_DECK_ID = "id";
    public static final String COLUMN_DECK_TITLE = "title";
    public static final String COLUMN_DECK_DESCRIPTION = "description";

    // Colonnes Table Cards
    public static final String COLUMN_CARD_ID = "id";
    public static final String COLUMN_CARD_DECK_ID = "deck_id";
    public static final String COLUMN_CARD_QUESTION = "question";
    public static final String COLUMN_CARD_ANSWER = "answer";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Requête de création de la table Decks
        String CREATE_DECKS_TABLE = "CREATE TABLE " + TABLE_DECKS + " (" +
                COLUMN_DECK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DECK_TITLE + " TEXT NOT NULL, " +
                COLUMN_DECK_DESCRIPTION + " TEXT)";

        // Requête de création de la table Cards (avec clé étrangère)
        String CREATE_CARDS_TABLE = "CREATE TABLE " + TABLE_CARDS + " (" +
                COLUMN_CARD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CARD_DECK_ID + " INTEGER NOT NULL, " +
                COLUMN_CARD_QUESTION + " TEXT NOT NULL, " +
                COLUMN_CARD_ANSWER + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COLUMN_CARD_DECK_ID + ") REFERENCES " +
                TABLE_DECKS + "(" + COLUMN_DECK_ID + ") ON DELETE CASCADE)";

        db.execSQL(CREATE_DECKS_TABLE);
        db.execSQL(CREATE_CARDS_TABLE);

        // Pré-remplissage avec des données par défaut
        seedDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DECKS);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Active la gestion des clés étrangères (ON DELETE CASCADE)
        db.setForeignKeyConstraintsEnabled(true);
    }

    private void seedDatabase(SQLiteDatabase db) {
        // --- Deck 1 : Vocabulaire Anglais ---
        ContentValues deck1Values = new ContentValues();
        deck1Values.put(COLUMN_DECK_TITLE, "Vocabulaire Anglais");
        deck1Values.put(COLUMN_DECK_DESCRIPTION, "Mots de base en anglais");
        long deck1Id = db.insert(TABLE_DECKS, null, deck1Values);

        ContentValues card1 = new ContentValues();
        card1.put(COLUMN_CARD_DECK_ID, deck1Id);
        card1.put(COLUMN_CARD_QUESTION, "Apple");
        card1.put(COLUMN_CARD_ANSWER, "Pomme");
        db.insert(TABLE_CARDS, null, card1);

        ContentValues card2 = new ContentValues();
        card2.put(COLUMN_CARD_DECK_ID, deck1Id);
        card2.put(COLUMN_CARD_QUESTION, "House");
        card2.put(COLUMN_CARD_ANSWER, "Maison");
        db.insert(TABLE_CARDS, null, card2);

        // --- Deck 2 : Capitales du Monde ---
        ContentValues deck2Values = new ContentValues();
        deck2Values.put(COLUMN_DECK_TITLE, "Capitales du Monde");
        deck2Values.put(COLUMN_DECK_DESCRIPTION, "Test de géographie");
        long deck2Id = db.insert(TABLE_DECKS, null, deck2Values);

        ContentValues card3 = new ContentValues();
        card3.put(COLUMN_CARD_DECK_ID, deck2Id);
        card3.put(COLUMN_CARD_QUESTION, "France");
        card3.put(COLUMN_CARD_ANSWER, "Paris");
        db.insert(TABLE_CARDS, null, card3);

        ContentValues card4 = new ContentValues();
        card4.put(COLUMN_CARD_DECK_ID, deck2Id);
        card4.put(COLUMN_CARD_QUESTION, "Japon");
        card4.put(COLUMN_CARD_ANSWER, "Tokyo");
        db.insert(TABLE_CARDS, null, card4);
    }
}
