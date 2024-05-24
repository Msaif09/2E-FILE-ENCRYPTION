package com.org.a2eencryption.Activities;

import static android.os.Build.VERSION.SDK_INT;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.org.a2eencryption.R;
import com.org.a2eencryption.Utility.EncryptionUtils;
import com.org.a2eencryption.Utility.FileUtil;
import com.org.a2eencryption.databinding.ActivityFileEncryptionBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidParameterSpecException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class FileEncryptionActivity extends AppCompatActivity {


   ActivityFileEncryptionBinding binding;

    ActivityResultLauncher<Intent> activityResultLauncher;

    Uri uri;
    File fileForEncrypt;
    String fileName;

    String userPassword;
    String systemPassword;

    String encryptedPassword;

    private String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+<>?";

   private String message = "After encryption you will get system generated password \n" +
           "and you must copy it and save some where safe. You will\n" +
           "need this password During Decrypting the file and also don't\n" +
           "forget to save your own password during Decryption you must \n" +
           "have both password (1. Your own password that you set, 2.System\n" +
           "genrated Password).";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFileEncryptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        binding.messageText.setText(message);

        binding.addFileBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFile();
            }
        });

        binding.encryptFileBt.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                userPassword =  binding.userPass.getEditText().getText().toString();
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.encryptFileBt.setVisibility(View.GONE);
                if (userPassword != null && !userPassword.trim().isEmpty()){
                    if(uri!=null){
                        saveFileInLocal(uri);
                        if(fileForEncrypt!=null){
                            systemPassword = getSystemPassword();
                            try {
                                encryptedPassword = Base64.getEncoder().encodeToString(EncryptionUtils.encryptMsg(userPassword,systemPassword));;
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
                            if(encryptedPassword!=null){
                                try {
                                    EncryptionUtils.encodeFile(encryptedPassword,uri,fileForEncrypt,FileEncryptionActivity.this);
                                    openDialogForCopySystemPassword();
                                    binding.progressBar.setVisibility(View.GONE);
                                    binding.encryptFileBt.setVisibility(View.VISIBLE);
                                    Toast.makeText(FileEncryptionActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                                    binding.addFileBt.setImageResource(R.drawable.ic_add);
                                    binding.addFileText.setText("Click To Add File");
                                    binding.userPass.getEditText().setText("");
                                    uri = null;
                                    systemPassword = null;
                                    fileForEncrypt=null;
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }else{
                                binding.progressBar.setVisibility(View.GONE);
                                binding.encryptFileBt.setVisibility(View.VISIBLE);
                                Toast.makeText(FileEncryptionActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                            }

                        }else{
                            binding.progressBar.setVisibility(View.GONE);
                            binding.encryptFileBt.setVisibility(View.VISIBLE);
                            Toast.makeText(FileEncryptionActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    }else {

                        binding.progressBar.setVisibility(View.GONE);
                        binding.encryptFileBt.setVisibility(View.VISIBLE);
                        Toast.makeText(FileEncryptionActivity.this, "Please select File!", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.encryptFileBt.setVisibility(View.VISIBLE);
                    Toast.makeText(FileEncryptionActivity.this, "Please enter your password!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        if (o.getResultCode() == RESULT_OK && o.getData() != null) {
                            uri = o.getData().getData();
                            fileName =  FileUtil.getFileName(FileEncryptionActivity.this, uri);
                            binding.addFileText.setText(fileName);
                            binding.addFileBt.setImageResource(R.drawable.ic_insert_file);
                        }
                    }
                });

    }

    private void openDialogForCopySystemPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_copy_text, null);
        builder.setView(dialogView);

        TextView textViewDialog = dialogView.findViewById(R.id.textViewDialog);
        textViewDialog.setText(systemPassword);
        Button buttonCopy = dialogView.findViewById(R.id.buttonCopy);

        buttonCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyTextToClipboard(textViewDialog.getText().toString());
            }
        });

        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void copyTextToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Text copied to clipboard!", Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void saveFileInLocal(Uri uri) {
        File file = new File(Environment.getExternalStorageDirectory(), "Download"+File.separator+"2E ENCRYPTION"+File.separator+"Encrypted File");
        if (!file.exists()) {
            file.mkdirs();
        }


        File result = new File(file.getAbsolutePath() + File.separator + fileName);
        fileForEncrypt = result;

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
            Toast.makeText(FileEncryptionActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getSystemPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(32);

        for (int i = 0; i < 32; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }

        return password.toString();



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