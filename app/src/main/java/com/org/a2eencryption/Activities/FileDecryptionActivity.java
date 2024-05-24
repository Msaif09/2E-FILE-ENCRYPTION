package com.org.a2eencryption.Activities;

import static android.os.Build.VERSION.SDK_INT;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.org.a2eencryption.R;
import com.org.a2eencryption.Utility.EncryptionUtils;
import com.org.a2eencryption.Utility.FileUtil;
import com.org.a2eencryption.databinding.ActivityFileDecryptionBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class FileDecryptionActivity extends AppCompatActivity {


    ActivityFileDecryptionBinding binding;

    ActivityResultLauncher<Intent> activityResultLauncher;

    Uri uri;
    File fileForDecrypt;
    String fileName;

    String userPassword;
    String systemPassword;

    String decryptedPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFileDecryptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        binding.addFileBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFile();
            }
        });
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        if (o.getResultCode() == RESULT_OK && o.getData() != null) {
                            uri = o.getData().getData();
                            fileName =  FileUtil.getFileName(FileDecryptionActivity.this, uri);
                            binding.addFileText.setText(fileName);
                            binding.addFileBt.setImageResource(R.drawable.ic_insert_file);
                        }
                    }
                });

        binding.encryptFileBt.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                userPassword =  binding.userPass.getEditText().getText().toString();
                systemPassword =  binding.systemPass.getEditText().getText().toString();
                if (userPassword != null && !userPassword.trim().isEmpty()
                        && systemPassword != null && !systemPassword.trim().isEmpty()){
                    if(uri!=null){
                        saveFileInLocal(uri);
                        if(fileForDecrypt!=null){
                            try {
                                decryptedPassword = Base64.getEncoder().encodeToString(EncryptionUtils.encryptMsg(userPassword,systemPassword));;
                            } catch (NoSuchAlgorithmException e) {
                                throw new RuntimeException(e);
                            } catch (NoSuchPaddingException e) {
                                throw new RuntimeException(e);
                            } catch (InvalidKeyException e) {
                                throw new RuntimeException(e);
                            } catch (InvalidParameterSpecException e) {
                                throw new RuntimeException(e);
                            } catch (IllegalBlockSizeException e) {
                                throw new RuntimeException(e);
                            } catch (BadPaddingException e) {
                                throw new RuntimeException(e);
                            } catch (UnsupportedEncodingException e) {
                                throw new RuntimeException(e);
                            }
                            if(decryptedPassword!=null){
                                try {
                                    EncryptionUtils.decodeFile(decryptedPassword,uri,fileForDecrypt,FileDecryptionActivity.this);
                                    Toast.makeText(FileDecryptionActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                                    binding.addFileBt.setImageResource(R.drawable.ic_add);
                                    binding.addFileText.setText("Click To Add Encrypted File");
                                    binding.userPass.getEditText().setText("");
                                    binding.systemPass.getEditText().setText("");
                                    uri = null;
                                    systemPassword = null;
                                    fileForDecrypt=null;
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }else{
                                Toast.makeText(FileDecryptionActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                            }

                        }else{
                            Toast.makeText(FileDecryptionActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(FileDecryptionActivity.this, "Please select File!", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(FileDecryptionActivity.this, "Please enter your password!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void saveFileInLocal(Uri uri) {
        File file = new File(Environment.getExternalStorageDirectory(), "Download"+File.separator+"2E ENCRYPTION"+File.separator+"Decrypted File");
        if (!file.exists()) {
            file.mkdirs();
        }


        File result = new File(file.getAbsolutePath() + File.separator + fileName);
        fileForDecrypt = result;

        try {
            InputStream inStream = getContentResolver().openInputStream(uri);
            FileOutputStream outStream = new FileOutputStream(result);

            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }

            inStream.close();
            outStream.close();
        }catch (Exception e){
            Toast.makeText(FileDecryptionActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private void addFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        if (SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false);
        }
        activityResultLauncher.launch(intent);
    }
}