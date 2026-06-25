package com.example.lab7_20221203;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.lab7_20221203.auth.AuthService;
import com.example.lab7_20221203.entity.Usuario;
import com.example.lab7_20221203.repository.FirestoreRepository;
import com.example.lab7_20221203.repository.StorageRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseUser;


public class PerfilActivity extends AppCompatActivity {

    private ImageView ivFoto;
    private TextView tvNombre, tvCodigo, tvEmail, tvUrl;
    private MaterialButton btnSubirFoto;
    private ProgressBar progressBar;

    private AuthService authService;
    private FirestoreRepository firestoreRepository;
    private StorageRepository storageRepository;
    private String currentUid;
    private Usuario usuarioActual;

    // Lanzador para seleccionar imagen de la galería
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        subirFoto(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        authService = AuthService.getInstance();
        firestoreRepository = new FirestoreRepository();
        storageRepository = new StorageRepository();

        FirebaseUser user = authService.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUid = user.getUid();

        // Vincular vistas
        ivFoto = findViewById(R.id.ivFoto);
        tvNombre = findViewById(R.id.tvNombre);
        tvCodigo = findViewById(R.id.tvCodigo);
        tvEmail = findViewById(R.id.tvEmail);
        tvUrl = findViewById(R.id.tvUrl);
        btnSubirFoto = findViewById(R.id.btnSubirFoto);
        progressBar = findViewById(R.id.progressBar);

        // Toolbar: botón atrás
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Cargar datos del usuario
        cargarDatosUsuario();

        // Click en botón subir foto
        btnSubirFoto.setOnClickListener(v -> abrirGaleria());
    }

    private void cargarDatosUsuario() {
        firestoreRepository.obtenerUsuario(currentUid, new FirestoreRepository.FirestoreCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                usuarioActual = usuario;
                if (usuario != null) {
                    tvNombre.setText(usuario.getCodigoPUCP());
                    tvCodigo.setText("Código: " + usuario.getCodigoPUCP());
                    tvEmail.setText(usuario.getEmail());
                    String url = usuario.getFotoUrl()==null ? "..." : usuario.getFotoUrl();
                    tvUrl.setText("URL en Firebase Storage: "+url);

                    if (usuario.getFotoUrl() != null && !usuario.getFotoUrl().isEmpty()) {
                        Glide.with(PerfilActivity.this)
                                .load(usuario.getFotoUrl())
                                .placeholder(R.drawable.ic_user)
                                .error(R.drawable.ic_user)
                                .circleCrop()
                                .into(ivFoto);
                    } else {
                        ivFoto.setImageResource(R.drawable.ic_user);
                    }
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(PerfilActivity.this,  "Error al cargar perfil: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void subirFoto(Uri uri) {
        progressBar.setVisibility(View.VISIBLE);
        btnSubirFoto.setEnabled(false);

        storageRepository.subirImagenCredencial(currentUid, uri, new StorageRepository.StorageCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                // Actualizar URL en Firestore
                firestoreRepository.actualizarFotoUrl(currentUid, downloadUrl, new FirestoreRepository.FirestoreCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.GONE);
                        btnSubirFoto.setEnabled(true);
                        Toast.makeText(PerfilActivity.this, "Foto subida correctamente", Toast.LENGTH_SHORT).show();
                        // Cargar la nueva foto
                        Glide.with(PerfilActivity.this)
                                .load(downloadUrl)
                                .circleCrop()
                                .into(ivFoto);
                        // Guardar en el objeto local
                        if (usuarioActual != null) {
                            usuarioActual.setFotoUrl(downloadUrl);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        progressBar.setVisibility(View.GONE);
                        btnSubirFoto.setEnabled(true);
                        Toast.makeText(PerfilActivity.this, "Error al guardar URL: " + error, Toast.LENGTH_LONG).show();

                    }
                });
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                btnSubirFoto.setEnabled(true);
                Toast.makeText(PerfilActivity.this, "Error al subir foto: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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