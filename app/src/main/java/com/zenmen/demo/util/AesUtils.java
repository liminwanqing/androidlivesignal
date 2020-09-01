
package com.zenmen.demo.util;

import android.util.Log;

import java.nio.charset.Charset;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


/**
 * @author winky
 * @date 2019/5/30
 * 崩溃接口数据解密工具类
 */
public class AesUtils {
    private static final String ENCODE_ALGROTHN = "AES";

    private final static String DECRYPT_KEY = "1234567890abcdef";

    /**
     * 算法，CB模式（默认）：
     * 电码本模式    Electronic Codebook Book
     * CBC模式：
     * 密码分组链接模式    Cipher Block Chaining
     * CTR模式：
     * 计算器模式    Counter
     * CFB模式：
     * 密码反馈模式    Cipher FeedBack
     * OFB模式：
     * 输出反馈模式    Output FeedBack
     */
    private static final String ALGORITHMSTR = "AES/ECB/PKCS5Padding";

    private static final char[] DIGITS_UPPER = new char[]{'0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * AES加密
     *
     * @param content    待加密的内容
     * @param encryptKey 加密密钥 AES加密要求key必须要128个比特位（这里需要长度为16，否则会报错）
     * @return 加密后的byte[]
     * @throws Exception
     */
    public static byte[] aesEncryptToBytes(String content, String encryptKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptKey.getBytes(), ENCODE_ALGROTHN));

        return cipher.doFinal(content.getBytes(Charset.forName("UTF-8")));
    }


    /**
     * AES解密
     *
     * @param encryptBytes 待解密的byte[]
     * @param decryptKey   解密密钥 AES加密要求key必须要128个比特位（这里需要长度为16，否则会报错）
     * @return 解密后的String
     * @throws Exception
     */
    public static String aesDecryptByBytes(byte[] encryptBytes, String decryptKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decryptKey.getBytes(), ENCODE_ALGROTHN));
        byte[] decryptBytes = cipher.doFinal(encryptBytes);

        return new String(decryptBytes,  Charset.forName("UTF-8"));
    }


    /**
     * 将hex code AES解密
     *
     * @param encryptStr 待解密的hex code
     * @param decryptKey 解密密钥
     * @return 解密后的string
     * @throws Exception
     */
    public static String aesDecrypt(String encryptStr, String decryptKey) throws Exception {
        return (encryptStr == null || encryptStr.length() == 0) ?
                null
                :
                aesDecryptByBytes(decodeHex(encryptStr.toCharArray()), decryptKey);
    }

    /**
     * AES加密为hex code
     *
     * @param content    待加密的内容
     * @param encryptKey 加密密钥
     * @return 加密后的hex code
     * @throws Exception
     */
    public static String aesEncrypt(String content, String encryptKey) throws Exception {
        return encodeHexString(aesEncryptToBytes(content, encryptKey));
    }

    public static byte[] decodeHex(char[] data) throws IllegalArgumentException {
        int len = data.length;
        if ((len & 1) != 0) {
            throw new IllegalArgumentException("Odd number of characters.");
        } else {
            byte[] out = new byte[len >> 1];
            int i = 0;

            for (int j = 0; j < len; ++i) {
                int f = toDigit(data[j], j) << 4;
                ++j;
                f |= toDigit(data[j], j);
                ++j;
                out[i] = (byte) (f & 255);
            }

            return out;
        }
    }

    public static String encodeHexString(byte[] data) {
        int l = data.length;
        char[] out = new char[l << 1];
        int i = 0;

        for (int var5 = 0; i < l; ++i) {
            out[var5++] = DIGITS_UPPER[(240 & data[i]) >>> 4];
            out[var5++] = DIGITS_UPPER[15 & data[i]];
        }

        return new String(out);
    }

    protected static int toDigit(char ch, int index) throws IllegalArgumentException {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new IllegalArgumentException("Illegal hexadecimal character " + ch + " at index " + index);
        } else {
            return digit;
        }
    }

    public static String aesDecrypt(String encryptStr) {
        try {
            return aesDecrypt(encryptStr, DECRYPT_KEY);
        } catch (Exception e) {
            Log.e("AesUtils", "AesUtils.aesDecrypt fail@encryptStr:{} error:" + encryptStr);
        }
        return encryptStr.isEmpty() ? "" : encryptStr;
    }

    public static String aesEncrypt(String str) {
        try {
            return aesEncrypt(str, DECRYPT_KEY);
        } catch (Exception e) {
            Log.e("AesUtils", "AesUtils.aesEncrypt fail@encryptStr:{} error:" + str);
        }
        return str.isEmpty() ? "" : str;
    }
}

