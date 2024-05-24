package com.org.a2eencryption.Utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;

import androidx.core.content.FileProvider;


import com.org.a2eencryption.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtils {

    private  EncryptionUtils instance;

    public static byte[] generateKey(String password) throws Exception
    {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes("UTF-8");
        digest.update(bytes,0,bytes.length);
        return digest.digest();

    }

    public EncryptionUtils getInstance(){
        if(instance!=null){
            return  instance;
        }else {
            instance = new EncryptionUtils();
        }
        return  instance;
    }

    @SuppressLint("ResourceType")
    public static Uri encodeFile(String password, Uri fileUri, File file, Context context) throws Exception
    {
        byte[] key = generateKey(password);
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        byte[] iv = new byte[cipher.getBlockSize()];
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec,ivParameterSpec);
        InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
        CipherOutputStream outputStream = new CipherOutputStream(new FileOutputStream(file),cipher);


        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outputStream.close();

        if(file != null){
            return FileProvider.getUriForFile(Objects.requireNonNull(context),
                    BuildConfig.APPLICATION_ID + ".provider", file);
        }else {
            return  null;
        }
    }

    @SuppressLint("ResourceType")
    public static Uri decodeFile(String password, Uri fileUri, File file, Context context) throws Exception
    {


        byte[] key = generateKey(password);
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        byte[] iv = new byte[cipher.getBlockSize()];
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec,ivParameterSpec);
       FileOutputStream outputStream = new FileOutputStream(file);
        CipherInputStream inputStream = new CipherInputStream(context.getContentResolver().openInputStream(fileUri),cipher);


        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outputStream.close();

        if(file != null){
            return FileProvider.getUriForFile(Objects.requireNonNull(context),
                    BuildConfig.APPLICATION_ID + ".provider", file);
        }else {
            return  null;
        }
    }



    public static byte[] encryptMsg(String message,String password)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, UnsupportedEncodingException {
        /* Encrypt the message. */
        byte[] key = new byte[0];
        try {
            key = generateKey(password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SecretKeySpec secret = new SecretKeySpec(key, "AES");
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        byte[] cipherText = new byte[0];
        try {
            cipherText = cipher.doFinal(message.getBytes("UTF-8"));
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }
        return cipherText;
    }

    public static String decryptMsg(byte[] cipherText, String password)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException
    {
        /* Decrypt the message, given derived encContentValues and initialization vector. */
        byte[] key = new byte[0];
        try {
            key = generateKey(password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SecretKeySpec secret = new SecretKeySpec(key, "AES");
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret);
        String decryptString = new String(cipher.doFinal(cipherText), "UTF-8");
        return decryptString;
    }
}