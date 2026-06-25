package com.example.lab7_20221203.auth;

import android.util.Log;

import com.example.lab7_20221203.dto.RespuestaDesbloqueo;
import com.example.lab7_20221203.entity.Usuario;
import com.example.lab7_20221203.repository.FirestoreRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthService {

    private static final String TAG = "AuthService";
    private static AuthService instance;
    private final FirebaseAuth auth;
    private final FirestoreRepository firestoreRepository;

    private AuthService() {
        auth = FirebaseAuth.getInstance();
        auth.setLanguageCode("es");
        firestoreRepository = new FirestoreRepository();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    // ==================== SESIÓN ====================

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public void logout() {
        auth.signOut();
    }

    // ==================== LOGIN ====================

    public void loginUser(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(auth.getCurrentUser());
                    } else {
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Error desconocido";
                        Log.e(TAG, "Login error: " + errorMsg);
                        callback.onError(errorMsg);
                    }
                });
    }

    public void createUserAfterValidation(String email, String password,
                                          String codigo, String pin,
                                          RespuestaDesbloqueo respuesta,
                                          AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            guardarPerfilEnFirestore(firebaseUser, email, codigo, pin, respuesta, callback);
                        } else {
                            callback.onError("Error al obtener el usuario creado");
                        }
                    } else {
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Error al crear usuario en Firebase";
                        Log.e(TAG, "Error en createUser: " + errorMsg);
                        callback.onError(errorMsg);
                    }
                });
    }

    private void guardarPerfilEnFirestore(FirebaseUser firebaseUser,
                                          String email,
                                          String codigo,
                                          String pin,
                                          RespuestaDesbloqueo respuesta,
                                          AuthCallback callback) {
        Usuario usuario = new Usuario();
        usuario.setUid(firebaseUser.getUid());
        usuario.setEmail(email);
        usuario.setCodigoPUCP(codigo);
        usuario.setPin(pin);
        usuario.setIotAuthToken(respuesta.getIotAuthToken());

        long timestampMillis = System.currentTimeMillis();
        usuario.setTimestampAprobacion(timestampMillis);

        firestoreRepository.guardarUsuario(usuario, new FirestoreRepository.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(firebaseUser);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error guardando perfil en Firestore: " + error);
                callback.onError("Error guardando perfil: " + error);
            }
        });
    }

    // ==================== CALLBACKS ====================

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(String error);
    }
}
