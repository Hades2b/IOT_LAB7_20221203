package com.example.lab7_20221203.repository;


import com.example.lab7_20221203.entity.Usuario;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreRepository {
    private FirebaseFirestore db;
    private static final String COLLECTION_USERS = "usuarios";

    public FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void guardarUsuario(Usuario usuario, FirestoreCallback<Void> callback) {
        db.collection(COLLECTION_USERS)
                .document(usuario.getUid())
                .set(usuario)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) callback.onSuccess(null);
                    else callback.onError(task.getException().getMessage());
                });
    }

    public void obtenerUsuario(String uid, FirestoreCallback<Usuario> callback) {
        db.collection(COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        Usuario user = task.getResult().toObject(Usuario.class);
                        callback.onSuccess(user);
                    } else {
                        callback.onError(task.getException() != null ?
                                task.getException().getMessage() : "Documento no existe");
                    }
                });
    }

    public void actualizarTimestamp(String uid, long timestamp, FirestoreCallback<Void> callback) {
        db.collection(COLLECTION_USERS)
                .document(uid)
                .update("timestampAprobacion", timestamp)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) callback.onSuccess(null);
                    else callback.onError(task.getException().getMessage());
                });
    }

    public void actualizarFotoUrl(String uid, String fotoUrl, FirestoreCallback<Void> callback) {
        db.collection(COLLECTION_USERS)
                .document(uid)
                .update("fotoUrl", fotoUrl)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) callback.onSuccess(null);
                    else callback.onError(task.getException().getMessage());
                });
    }

    public interface FirestoreCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}