package com.example.lab7_20221203;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.lab7_20221203.auth.AuthService;
import com.example.lab7_20221203.dto.RespuestaDesbloqueo;
import com.example.lab7_20221203.entity.Usuario;
import com.example.lab7_20221203.repository.FirestoreRepository;
import com.example.lab7_20221203.service.BiciService;
import com.example.lab7_20221203.viewModel.ContadorViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private TextView tvEstado, tvContador, tvSubtitulo;
    private MaterialButton btnNuevoDesbloqueo;
    private MaterialCardView cardContador;
    private ContadorViewModel viewModel;
    private AuthService authService;
    private FirestoreRepository firestoreRepository;
    private BiciService biciService;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        setSupportActionBar(findViewById(R.id.toolbar));

        // Inicializar servicios
        authService = AuthService.getInstance();
        firestoreRepository = new FirestoreRepository();
        biciService = new BiciService(this);

        // Obtener UID del usuario actual
        FirebaseUser user = authService.getCurrentUser();
        if (user == null) {
            // Si no hay usuario, volver a Login
            finish();
            return;
        }
        currentUid = user.getUid();

        // Vincular vistas
        tvEstado = findViewById(R.id.tvEstado);
        tvContador = findViewById(R.id.tvContador);
        tvSubtitulo = findViewById(R.id.tvSubtitulo);
        btnNuevoDesbloqueo = findViewById(R.id.btnNuevoDesbloqueo);
        cardContador = findViewById(R.id.cardContador);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(ContadorViewModel.class);

        // Observar cambios en segundos
        viewModel.getSegundosRestantes().observe(this, segundos -> {
            if (segundos != null) {
                tvContador.setText(String.valueOf(segundos));
                // Cambiar color de la card según el estado
                if (segundos > 0) {
                    cardContador.setCardBackgroundColor(getColor(R.color.green_light));
                    tvContador.setTextColor(getColor(R.color.green_dark));
                } else {
                    cardContador.setCardBackgroundColor(getColor(R.color.red_light));
                    tvContador.setTextColor(getColor(R.color.red_dark));
                }
            }
        });

        // Observar estado expirado
        viewModel.getExpirado().observe(this, expirado -> {
            if (expirado != null) {
                if (expirado) {
                    tvEstado.setText("Tiempo de gracia expirado");
                    tvSubtitulo.setText("Candado trabado por seguridad");
                    btnNuevoDesbloqueo.setVisibility(View.VISIBLE);
                } else {
                    tvEstado.setText("Candado IoT energizado");
                    tvSubtitulo.setText("Retire la unidad");
                    btnNuevoDesbloqueo.setVisibility(View.GONE);
                }
            }
        });

        // Cargar datos del usuario y iniciar contador
        cargarDatosUsuario();

        // Botón para solicitar nuevo desbloqueo
        btnNuevoDesbloqueo.setOnClickListener(v -> solicitarNuevoDesbloqueo());
    }

    private void cargarDatosUsuario() {
        firestoreRepository.obtenerUsuario(currentUid, new FirestoreRepository.FirestoreCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                if (usuario != null && usuario.getTimestampAprobacion() > 0) {
                    // Iniciar contador con el timestamp guardado
                    viewModel.iniciarContador(usuario.getTimestampAprobacion());
                } else {
                    // Si no hay timestamp, mostrar expirado
                    viewModel.getExpirado().setValue(true);
                    tvContador.setText("0");
                }
            }

            @Override
            public void onError(String error) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Error al cargar perfil: " + error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void solicitarNuevoDesbloqueo() {
        // Mostrar progreso (deshabilitar botón)
        btnNuevoDesbloqueo.setEnabled(false);
        btnNuevoDesbloqueo.setText("Validando...");

        // Obtener código y pin del usuario desde Firestore (o desde memoria)
        firestoreRepository.obtenerUsuario(currentUid, new FirestoreRepository.FirestoreCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                if (usuario != null) {
                    String codigo = usuario.getCodigoPUCP();
                    String pin = usuario.getPin();
                    // Llamar al servicio
                    biciService.solicitarDesbloqueo(codigo, pin)
                            .observe(MainActivity.this, resultado -> {
                                btnNuevoDesbloqueo.setEnabled(true);
                                btnNuevoDesbloqueo.setText("SOLICITAR NUEVO DESBLOQUEO");
                                if (resultado.isSuccess()) {
                                    RespuestaDesbloqueo respuesta = resultado.respuesta;
                                    // Actualizar timestamp en Firestore
                                    long nuevoTimestamp = System.currentTimeMillis();
                                    // Si el servidor devuelve timestamp, usarlo; si no, usar actual
                                    firestoreRepository.actualizarTimestamp(currentUid, nuevoTimestamp,
                                            new FirestoreRepository.FirestoreCallback<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // Reiniciar contador
                                                    viewModel.reiniciarContador(nuevoTimestamp);
                                                    Snackbar.make(findViewById(android.R.id.content),
                                                            "Desbloqueo renovado", Snackbar.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onError(String error) {
                                                    Snackbar.make(findViewById(android.R.id.content),
                                                            "Error al actualizar timestamp", Snackbar.LENGTH_LONG).show();
                                                }
                                            });
                                } else {
                                    Snackbar.make(findViewById(android.R.id.content),
                                            "Error: " + resultado.error, Snackbar.LENGTH_LONG).show();
                                }
                            });
                } else {
                    btnNuevoDesbloqueo.setEnabled(true);
                    btnNuevoDesbloqueo.setText("SOLICITAR NUEVO DESBLOQUEO");
                    Snackbar.make(findViewById(android.R.id.content),
                            "No se encontraron datos del usuario", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String error) {
                btnNuevoDesbloqueo.setEnabled(true);
                btnNuevoDesbloqueo.setText("SOLICITAR NUEVO DESBLOQUEO");
                Snackbar.make(findViewById(android.R.id.content),
                        "Error al obtener datos: " + error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    // Menú
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_carnet) {
            Intent intent = new Intent(this, PerfilActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            authService.logout();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }
}