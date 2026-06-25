package com.example.lab7_20221203.repository;

import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class StorageRepository {
    private StorageReference storageRef;

    public StorageRepository() {
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    public void subirImagenCredencial(String uid, Uri imagenUri, StorageCallback callback) {
        StorageReference fileRef = storageRef.child("credenciales_bicipucp/" + uid + ".jpg");
        fileRef.putFile(imagenUri)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        fileRef.getDownloadUrl().addOnSuccessListener(uri ->
                                callback.onSuccess(uri.toString())
                        ).addOnFailureListener(e ->
                                callback.onError(e.getMessage())
                        );
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    public interface StorageCallback {
        void onSuccess(String downloadUrl);
        void onError(String error);
    }
}
