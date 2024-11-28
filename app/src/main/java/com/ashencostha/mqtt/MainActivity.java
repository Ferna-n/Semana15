package com.ashencostha.mqtt;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;


public class MainActivity extends AppCompatActivity {

    private static final String BROKER_URL = "tcp://test.mosquitto.org:1883";
    private static final String CLIENT_ID = "cb6b663950934067b555499841d33521";
    private MqttServicio mqttServicio;
    private EditText topicEditText;
    private EditText messageEditText;
    private TextView messagesTextView;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mqttServicio = new MqttServicio();
        mqttServicio.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                mainHandler.post(() -> {
                    Toast.makeText(MainActivity.this, "Conexión perdida, reconectando...", Toast.LENGTH_SHORT).show();
                    mqttServicio.onConnectionLost(cause);
                });
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                mainHandler.post(() -> {
                    String receivedMessage = "Mensaje recibido en tópico " + topic + ": " + new String(message.getPayload());
                    messagesTextView.append(receivedMessage + "\n");
                });
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                mainHandler.post(() -> {
                    Toast.makeText(MainActivity.this, "Entrega completa", Toast.LENGTH_SHORT).show();
                });
            }
        });
        //MQTT

        topicEditText = findViewById(R.id.topicEditText);
        messageEditText = findViewById(R.id.messageEditText);
        messagesTextView = findViewById(R.id.messagesTextView);
        Button publishButton = findViewById(R.id.publishButton);
        Button subscribeButton = findViewById(R.id.subscribeButton);

        mainHandler = new Handler(Looper.getMainLooper());

        // Conectar al broker
        try {
            mqttServicio.connect(BROKER_URL, CLIENT_ID);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // Publicar un mensaje
        publishButton.setOnClickListener(v -> {
            String topic = topicEditText.getText().toString();
            String message = messageEditText.getText().toString();
            mqttServicio.publish(topic, message);
        });

        // Suscribir al tópico
        subscribeButton.setOnClickListener(v -> {
            String topic = topicEditText.getText().toString();
            mqttServicio.subscribe(topic);
            Toast.makeText(this, "Suscrito a: " + topic, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        mqttServicio.disconnect();
        super.onDestroy();
    }
}
