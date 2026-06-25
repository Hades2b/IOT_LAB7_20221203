package com.example.lab7_20221203;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.lab7_20221203.auth.AuthService;
import com.example.lab7_20221203.dto.RespuestaDesbloqueo;
import com.example.lab7_20221203.service.BiciService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;


public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword, tilCodigo, tilPin;
    private TextInputEditText inputEmail, inputPassword, inputCodigo, inputPin;
    private MaterialButton btnRegister;
    private ProgressBar progressBar;
    private AuthService authService;
    private BiciService biciService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authService = AuthService.getInstance();
        biciService = new BiciService(this);

        // Vincular vistas
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilCodigo = findViewById(R.id.tilCodigo);
        tilPin = findViewById(R.id.tilPin);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputCodigo = findViewById(R.id.inputCodigo);
        inputPin = findViewById(R.id.inputPin);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);

        // Redirigir a Login
        findViewById(R.id.btnOpenLogin).setOnClickListener(v ->
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class))
        );

        // Click en registrar
        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        // Limpiar errores previos
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilCodigo.setError(null);
        tilPin.setError(null);

        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        String codigo = inputCodigo.getText().toString().trim();
        String pin = inputPin.getText().toString().trim();

        // Validaciones (mismo código que antes)
        boolean hasError = false;
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("El correo es obligatorio");
            hasError = true;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Correo inválido");
            hasError = true;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("La contraseña es obligatoria");
            hasError = true;
        } else if (password.length() < 6) {
            tilPassword.setError("Mínimo 6 caracteres");
            hasError = true;
        }
        if (TextUtils.isEmpty(codigo)) {
            tilCodigo.setError("El código PUCP es obligatorio");
            hasError = true;
        } else if (codigo.length() != 8 || !codigo.matches("\\d{8}")) {
            tilCodigo.setError("Debe tener 8 dígitos");
            hasError = true;
        }
        if (TextUtils.isEmpty(pin)) {
            tilPin.setError("El PIN es obligatorio");
            hasError = true;
        } else if (pin.length() != 4 || !pin.matches("\\d{4}")) {
            tilPin.setError("Debe tener 4 dígitos");
            hasError = true;
        }

        if (hasError) {
            // Enfocar primer error
            if (tilEmail.getError() != null) inputEmail.requestFocus();
            else if (tilPassword.getError() != null) inputPassword.requestFocus();
            else if (tilCodigo.getError() != null) inputCodigo.requestFocus();
            else if (tilPin.getError() != null) inputPin.requestFocus();
            return;
        }

        // --- Iniciar proceso con BiciService (versión LiveData) ---
        setLoadingState(true);

        biciService.solicitarDesbloqueoLive(codigo, pin)
                .observe(this, new Observer<BiciService.ResultadoDesbloqueo>() {
                    @Override
                    public void onChanged(BiciService.ResultadoDesbloqueo resultado) {
                        setLoadingState(false);

                        if (resultado.isSuccess()) {
                            // Éxito → crear usuario en Firebase
                            RespuestaDesbloqueo respuesta = resultado.respuesta;
                            authService.createUserAfterValidation(
                                    email, password, codigo, pin, respuesta,
                                    new AuthService.AuthCallback() {
                                        @Override
                                        public void onSuccess(FirebaseUser user) {
                                            navigateToMain();
                                        }

                                        @Override
                                        public void onError(String error) {
                                            Toast.makeText(RegisterActivity.this, "Error al crear usuario: " + error, Toast.LENGTH_LONG).show();
                                        }
                                    }
                            );
                        } else {
                            // Error del microservicio
                            Toast.makeText(RegisterActivity.this, resultado.error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(false);
            btnRegister.setText("Validando...");
        } else {
            progressBar.setVisibility(View.GONE);
            btnRegister.setEnabled(true);
            btnRegister.setText("Registrar");
        }
    }

    private void navigateToMain() {
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        finish();
    }
}