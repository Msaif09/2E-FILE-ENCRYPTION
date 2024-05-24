package com.org.a2eencryption.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.org.a2eencryption.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        binding.encryptFileActivityBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    checkPermission();
                    Intent intent = new Intent(HomeActivity.this, FileEncryptionActivity.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(HomeActivity.this, FileEncryptionActivity.class);
                    startActivity(intent);
                }
            }
        });

        binding.decryptFileActivityBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    checkPermission();
                    Intent intent = new Intent(HomeActivity.this, FileDecryptionActivity.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(HomeActivity.this, FileDecryptionActivity.class);
                    startActivity(intent);
                }
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==101){
            if(grantResults.length>0){
                if (grantResults[0]== PackageManager.PERMISSION_GRANTED){

                }
            }else {
               requestStorage();
            }
        }
    }

    public boolean isStorageOk() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
    public void requestStorage() {
        ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);

    }
    private void checkPermission() {
           if (!isStorageOk()) {
               requestStorage();
           }
    }
}