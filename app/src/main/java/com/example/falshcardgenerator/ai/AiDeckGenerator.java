package com.example.falshcardgenerator.ai;

import android.content.Context;
import android.util.Log;

import com.example.falshcardgenerator.BuildConfig;
import com.example.falshcardgenerator.db.FlashcardDao;
import com.example.falshcardgenerator.model.Card;
import com.example.falshcardgenerator.model.Deck;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AiDeckGenerator {

    private static final String TAG = "GeminiDeckGenerator";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions";
    private static final String MODEL_NAME = "gemini-3.5-flash-lite";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private final FlashcardDao dao;
    private final OkHttpClient client;

    public AiDeckGenerator(Context context) {
        this.dao = new FlashcardDao(context.getApplicationContext());
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Génère un deck de flashcards via l'API Gemini et l'enregistre dans SQLite.
     *
     * @param theme    Thème principal (obligatoire)
     * @param subTheme Sous-thème (optionnel, peut être vide ou null)
     * @param cardCount Nombre de cartes souhaitées (ex: 5)
     * @return L'ID du deck créé en base de données, ou -1L en cas d'erreur.
     */
    public long generateAndSaveDeck(String theme, String subTheme, int cardCount) {
        String apiKey = BuildConfig.GEMINI_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            Log.e(TAG, "Clé API Gemini manquante dans local.properties !");
            return -1L;
        }

        boolean hasSubTheme = subTheme != null && !subTheme.trim().isEmpty();

        try {
            // 1. Construction du corps JSON de la requête (format compatible OpenAI)
            String requestJson = buildRequestPayload(theme, hasSubTheme ? subTheme.trim() : null, cardCount);

            RequestBody body = RequestBody.create(requestJson, JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            // 2. Exécution de l'appel réseau
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    String errorBody = response.body() != null ? response.body().string() : "Vide";
                    Log.e(TAG, "Erreur API (" + response.code() + ") : " + errorBody);
                    return -1L;
                }

                String responseString = response.body().string();
                Log.d(TAG, "Réponse brute API : " + responseString);

                // 3. Extraction des cartes depuis la réponse JSON
                List<Card> cards = parseCardsFromApiResponse(responseString);
                if (cards.isEmpty()) {
                    Log.e(TAG, "Aucune carte valide extraite de la réponse JSON.");
                    return -1L;
                }

                // 4. Insertion du deck et de ses cartes dans SQLite
                String deckTitle = theme;
                String deckDescription = hasSubTheme
                        ? subTheme.trim() + " — Généré par IA"
                        : "Généré par IA";

                long newDeckId = dao.insertDeck(new Deck(deckTitle, deckDescription));
                if (newDeckId == -1L) {
                    return -1L;
                }

                for (Card card : cards) {
                    card.setDeckId(newDeckId);
                    dao.insertCard(card);
                }

                return newDeckId;
            }

        } catch (IOException e) {
            Log.e(TAG, "Erreur réseau lors de l'appel à Gemini", e);
            return -1L;
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du traitement de la génération", e);
            return -1L;
        }
    }

    /**
     * Construit le JSON attendu par l'endpoint /chat/completions.
     */
    private String buildRequestPayload(String theme, String subTheme, int cardCount) throws Exception {
        JSONObject root = new JSONObject();
        root.put("model", MODEL_NAME);
        root.put("temperature", 0.4);

        // Force le modèle à répondre uniquement avec un objet JSON valide
        JSONObject responseFormat = new JSONObject();
        responseFormat.put("type", "json_object");
        root.put("response_format", responseFormat);

        JSONArray messages = new JSONArray();

        // Message Système : définit le format JSON strict attendu
        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content",
                "Tu es un assistant pédagogique expert en création de flashcards. " +
                        "Génère exactement " + cardCount + " flashcards claires, précises et concises en français. " +
                        "Tu dois répondre UNIQUEMENT avec un objet JSON valide respectant strictement ce schéma : " +
                        "{\"cards\": [{\"question\": \"...\", \"answer\": \"...\"}]}."
        );
        messages.put(systemMsg);

        // Message Utilisateur : inclut le thème et éventuellement le sous-thème
        String userPrompt = (subTheme != null)
                ? "Thème principal : " + theme + "\nSous-thème spécifique : " + subTheme
                : "Thème principal : " + theme;

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);
        messages.put(userMsg);

        root.put("messages", messages);
        return root.toString();
    }

    /**
     * Extrait le contenu JSON de choices[0].message.content et le convertit en objets Card.
     */
    private List<Card> parseCardsFromApiResponse(String apiResponseJson) throws Exception {
        List<Card> cards = new ArrayList<>();

        JSONObject root = new JSONObject(apiResponseJson);
        JSONArray choices = root.getJSONArray("choices");
        if (choices.length() == 0) {
            return cards;
        }

        String content = choices.getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim();

        // Sécurité au cas où le modèle entourerait le JSON de balises Markdown ```json ... ```
        if (content.startsWith("```")) {
            content = content.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "").trim();
        }

        JSONObject contentJson = new JSONObject(content);
        JSONArray cardsArray = contentJson.getJSONArray("cards");

        for (int i = 0; i < cardsArray.length(); i++) {
            JSONObject item = cardsArray.getJSONObject(i);
            String question = item.optString("question", "").trim();
            String answer = item.optString("answer", "").trim();

            if (!question.isEmpty() && !answer.isEmpty()) {
                cards.add(new Card(0, question, answer));
            }
        }

        return cards;
    }
}