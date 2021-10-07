package com.rick.security.demo;


import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.converter.RsaKeyConverters;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

/**
 * @author zyc
 */
@Slf4j
public final class JwtUtil {

    /**
     * 私钥
     */
    private static final RSAPrivateKey PRIVATE_KEY = RsaKeyConverters.pkcs8().convert(JwtUtil.class.getResourceAsStream("/key.private"));

    /**
     * 公钥
     */
    private static final RSAPublicKey PUBLIC_KEY = RsaKeyConverters.x509().convert(JwtUtil.class.getResourceAsStream("/key.public"));

    /**
     * rsa算法加解密时的填充方式
     */
    private static final String RSA_PADDING = "RSA/ECB/PKCS1Padding";

    private JwtUtil() {
    }

    /**
     * 生成jwt
     *
     * @return jwt
     */
    public static String jwt() {
        try {
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject("RS512 Rick")
                    .issuer("http://www.doiduoyi.com")
                    .claim("scope", "message.read message.write")
                    .build();

            SignedJWT jwt = new SignedJWT(new JWSHeader(new JWSAlgorithm("RS512")), claimsSet);
            // 私钥签名，公钥验签
            jwt.sign(new RSASSASigner(PRIVATE_KEY));
            return jwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 生成私钥和公钥
     */
    private static void keys() {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(2048);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();
            log.info("{}{}{}", "\n-----BEGIN PRIVATE KEY-----\n", Base64.getMimeEncoder().encodeToString(privateKey.getEncoded()), "\n-----END PRIVATE KEY-----");
            log.info("{}{}{}", "\n-----BEGIN PUBLIC KEY-----\n", Base64.getMimeEncoder().encodeToString(publicKey.getEncoded()), "\n-----END PUBLIC KEY-----");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 加密
     *
     * @param plaintext 明文
     * @return 密文
     */
    private static String encrypt(String plaintext) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, PUBLIC_KEY);
            String encrypt = Base64.getEncoder().encodeToString(cipher.doFinal(plaintext.getBytes()));
            log.info("The plaintext {} is encrypted as: {}", plaintext, encrypt);
            return encrypt;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 解密
     *
     * @param cipherText 密文
     * @return 明文
     */
    private static String decrypt(String cipherText) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, PRIVATE_KEY);
            String decrypt = new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)));
            log.info("The ciphertext {} is decrypted as: {}", cipherText, decrypt);
            return decrypt;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static void main(String[] args) {
        // eyJhbGciOiJSUzUxMiJ9.eyJpc3MiOiJodHRwOlwvXC93d3cuZG9pZHVveWkuY29tIiwic3ViIjoiUlM1MTIgUmljayIsInNjb3BlIjoibWVzc2FnZS5yZWFkIG1lc3NhZ2Uud3JpdGUifQ.rEUa3KjsAZqon1ChR9Azt75ajeeO7GWg8gbwAWgMRSqHkERXyu4hSNfEJvu6opMtvMTYgunJ5G4eBFwYYsN7yXrbMZwpT3VCA2PHt0MqVzjgAF82JsrYVIp3jnlBHC1ojvA5KFYO_UZ6i38ubkWI9UDGUlxVgsMuvywH2tJZOEYokRY0JDQCw82ttda6Z5jckqFdu2yBX7cEOKhvT9GJoOMZXFr2m1uNoo5JRlTafCG8OhjCFRFlwB6MZFQ0znSGmWG_HKIkKHPJpwVLDt-rcPScdRbiG-qIp0Q2U7zWBtnQjHXkzkMbmRhJFVeATGYnW2Wbdtuc069tkNWP32sXnw
        System.out.println(JwtUtil.jwt());
    }

}
