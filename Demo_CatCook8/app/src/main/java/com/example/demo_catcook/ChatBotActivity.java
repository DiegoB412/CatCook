package com.example.demo_catcook;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatBotActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages = new ArrayList<>();

    private EditText edtConsulta;
    private ImageButton btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        edtConsulta = findViewById(R.id.edtConsulta);
        btnSend = findViewById(R.id.btnSend);

        // Configurar el RecyclerView
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(chatMessages);
        recyclerViewChat.setAdapter(chatAdapter);

        btnSend.setOnClickListener(v -> {
            String userMessage = edtConsulta.getText().toString().trim();
            if (!userMessage.isEmpty()) {
                // Agregar mensaje del usuario
                chatMessages.add(new ChatMessage(userMessage, true));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);

                // Llamar a la API de Gemini
                callGeminiAPI(userMessage);

                // Limpiar el campo de texto
                edtConsulta.setText("");
                recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
            }
        });
    }

    private void callGeminiAPI(String userMessage) {
        // Definir el contexto
        String contexto = "Eres un experto en cocina. Responde únicamente preguntas relacionadas con recetas, " +
                "ingredientes o técnicas culinarias. Si te hacen una pregunta que no es sobre cocina, responde diciendo: " +
                "'Lo siento, solo puedo ayudarte con temas de cocina.'";

        // Combinar el contexto con el mensaje del usuario
        String mensajeFinal = contexto + "\nUsuario: " + userMessage;

        // Configuración de la API de Gemini
        GenerativeModel gm = new GenerativeModel(
                "gemini-1.5-flash",
                "AIzaSyDP_bybI-xowVFsKGMHFX8y15f31VyDYOY");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder().addText(mensajeFinal).build();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                runOnUiThread(() -> {
                    // Agregar respuesta del chatbot
                    chatMessages.add(new ChatMessage(result.getText(), false));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    chatMessages.add(new ChatMessage("Error: " + t.getMessage(), false));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
                });
            }
        }, Executors.newSingleThreadExecutor());
    }

}
