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

import com.example.lab7_20221203.auth.AuthService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText inputEmail, inputPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = AuthService.getInstance();

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.btnOpenRegister).setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );

        // Click en login
        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Si ya hay sesión activa, saltar al Main
        if (authService.isUserLoggedIn()) {
            navigateToMain();
        }
    }

    private void attemptLogin() {
        tilEmail.setError(null);
        tilPassword.setError(null);

        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        boolean hasError = false;
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("El correo es obligatorio");
            hasError = true;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Correo electrónico inválido");
            hasError = true;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("La contraseña es obligatoria");
            hasError = true;
        } else if (password.length() < 6) {
            tilPassword.setError("La contraseña debe tener al menos 6 caracteres");
            hasError = true;
        }

        if (hasError) {
            // Enfocar el primer campo con error
            if (tilEmail.getError() != null) {
                inputEmail.requestFocus();
            } else if (tilPassword.getError() != null) {
                inputPassword.requestFocus();
            }
            return;
        }

        setLoadingState(true);

        authService.loginUser(email, password, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                setLoadingState(false);
                navigateToMain();
            }

            @Override
            public void onError(String error) {
                setLoadingState(false);

                // Mapear errores de Firebase a campos específicos (Material Design)
                if (error != null) {
                    String errorLower = error.toLowerCase();
                    if (errorLower.contains("user") || errorLower.contains("email") || errorLower.contains("not found")) {
                        tilEmail.setError("Correo no registrado");
                        inputEmail.requestFocus();
                    } else if (errorLower.contains("password")) {
                        tilPassword.setError("Contraseña incorrecta");
                        inputPassword.requestFocus();
                    } else {
                        Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "No se pudo iniciar sesión", Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
            btnLogin.setText("Iniciando...");
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            btnLogin.setText("Iniciar sesión");
        }
    }

    private void navigateToMain() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}