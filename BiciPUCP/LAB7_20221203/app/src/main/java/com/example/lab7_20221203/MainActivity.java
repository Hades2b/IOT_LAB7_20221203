package com.example.lab7_20221203;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
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

        setSupportActionBar(findViewById(R.id.toolbar));

        authService = AuthService.getInstance();
        firestoreRepository = new FirestoreRepository();
        biciService = new BiciService(this);

        FirebaseUser user = authService.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        currentUid = user.getUid();

        tvEstado = findViewById(R.id.tvEstado);
        tvContador = findViewById(R.id.tvContador);
        tvSubtitulo = findViewById(R.id.tvSubtitulo);
        btnNuevoDesbloqueo = findViewById(R.id.btnNuevoDesbloqueo);
        cardContador = findViewById(R.id.cardContador);

        viewModel = new ViewModelProvider(this).get(ContadorViewModel.class);

        viewModel.getSegundosRestantes().observe(this, segundos -> {
            if (segundos != null) {
                tvContador.setText(String.valueOf(segundos));
                if (segundos > 0) {
                    cardContador.setCardBackgroundColor(getColor(R.color.green_light));
                    tvContador.setTextColor(getColor(R.color.green_dark));
                } else {
                    cardContador.setCardBackgroundColor(getColor(R.color.red_light));
                    tvContador.setTextColor(getColor(R.color.red_dark));
                }
            }
        });

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

        cargarDatosUsuario();

        btnNuevoDesbloqueo.setOnClickListener(v -> solicitarNuevoDesbloqueo());
    }

    private void cargarDatosUsuario() {
        firestoreRepository.obtenerUsuario(currentUid, new FirestoreRepository.FirestoreCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                if (usuario != null && usuario.getTimestampAprobacion() > 0) {
                    viewModel.iniciarContador(usuario.getTimestampAprobacion());
                } else {
                    viewModel.setExpiradoManual(true);
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
        btnNuevoDesbloqueo.setEnabled(false);
        btnNuevoDesbloqueo.setText("Validando...");

        firestoreRepository.obtenerUsuario(currentUid, new FirestoreRepository.FirestoreCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                if (usuario != null) {
                    biciService.solicitarDesbloqueo(usuario.getCodigoPUCP(), usuario.getPin())
                            .observe(MainActivity.this, resultado -> {
                                btnNuevoDesbloqueo.setEnabled(true);
                                btnNuevoDesbloqueo.setText("SOLICITAR NUEVO DESBLOQUEO");
                                if (resultado.isSuccess()) {
                                    RespuestaDesbloqueo res = resultado.respuesta;
                                    long nuevoTs = authService.parseTimestamp(res.getTimestampAprobacion());
                                    
                                    firestoreRepository.actualizarTimestamp(currentUid, nuevoTs,
                                            new FirestoreRepository.FirestoreCallback<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    viewModel.reiniciarContador(nuevoTs);
                                                    Snackbar.make(findViewById(android.R.id.content),
                                                            "Desbloqueo renovado", Snackbar.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onError(String error) {
                                                    Snackbar.make(findViewById(android.R.id.content),
                                                            "Error al actualizar Firestore", Snackbar.LENGTH_LONG).show();
                                                }
                                            });
                                } else {
                                    Snackbar.make(findViewById(android.R.id.content),
                                            "Error: " + resultado.error, Snackbar.LENGTH_LONG).show();
                                }
                            });
                }
            }

            @Override
            public void onError(String error) {
                btnNuevoDesbloqueo.setEnabled(true);
                btnNuevoDesbloqueo.setText("SOLICITAR NUEVO DESBLOQUEO");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_carnet) {
            startActivity(new Intent(this, PerfilActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            authService.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
