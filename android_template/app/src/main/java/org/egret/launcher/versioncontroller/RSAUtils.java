package org.egret.launcher.versioncontroller;

import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public final class RSAUtils {
    private static String RSA = "RSA";

    public static String encryptBase64DataWithPublicKey(String data, String publicKeyStr) {
        byte[] publicKeyDer = CryptoUtil.hexStringToByteArray(publicKeyStr);
        byte[] code;

        try {
            code = CryptoUtil.rsaPublicEncrypt(data.getBytes("UTF-8"), publicKeyDer);
        } catch (Exception e) {
            return "";
        }

        String ret = CryptoUtil.encodeBase64(code);

        return ret;
    }
}

class CryptoUtil {
    private static final int RSA_DECRYPT_BYTES = 1024 / 8;
    private static final int RSA_ENCRYPT_BYTES = RSA_DECRYPT_BYTES - 11;

    public static byte[] hexStringToByteArray(String s) {
        if (s == null) {
            return null;
        }
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static byte[] rsaPublicEncrypt(byte[] data, byte[] key)
            throws Exception {
        return rsaPublicCode(data, key, Cipher.ENCRYPT_MODE);
    }

    private static byte[] rsaPublicCode(byte[] bytes, byte[] key, int action)
            throws Exception {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(key);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPublicKey publicKey =
                (RSAPublicKey) keyFactory.generatePublic(spec);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
        cipher.init(action, publicKey);
        int length = action == Cipher.ENCRYPT_MODE ? RSA_ENCRYPT_BYTES :
                RSA_DECRYPT_BYTES;
        return CryptoUtil.blockCipher(bytes, cipher, length);
    }

    public static byte[] blockCipher(byte[] bytes, Cipher cipher, int length)
            throws Exception {
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        byte[] buffer;
        for (int i = 0; i < bytes.length; i += length) {
            int cipherLength = bytes.length - i;
            cipherLength = cipherLength >= length ? length : cipherLength;
            buffer = cipher.doFinal(bytes, i, cipherLength);
            byteBuilder.write(buffer);
        }
        return byteBuilder.toByteArray();
    }

    public static String encodeBase64(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return encodeUrl(Base64.encodeToString(bytes, Base64.NO_WRAP));
    }

    public static String encodeUrl(String message) {
        if (message == null) {
            return null;
        }
        String result = message.replace('+', '-');
        return result.replace('/', '_');
    }

}
