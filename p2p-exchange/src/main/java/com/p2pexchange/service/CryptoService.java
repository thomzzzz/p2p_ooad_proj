package com.p2pexchange.service;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

/**
 * Service for encryption and decryption operations using Strategy Pattern.
 */
@Service
public class CryptoService {
    
    // Strategy interface
    public interface EncryptionStrategy {
        byte[] encrypt(byte[] data, String key) throws Exception;
        byte[] decrypt(byte[] encryptedData, String key) throws Exception;
        String generateKey();
    }
    
    // AES encryption strategy
    private class AESEncryptionStrategy implements EncryptionStrategy {
        @Override
        public byte[] encrypt(byte[] data, String key) throws Exception {
            // Convert key to bytes and create SecretKeySpec
            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] iv = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            
            // Encrypt data
            byte[] encryptedData = cipher.doFinal(data);
            
            // Combine IV and encrypted data
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
            
            return combined;
        }
        
        @Override
        public byte[] decrypt(byte[] encryptedData, String key) throws Exception {
            // Extract IV from encrypted data
            byte[] iv = new byte[16];
            byte[] actualEncryptedData = new byte[encryptedData.length - 16];
            System.arraycopy(encryptedData, 0, iv, 0, iv.length);
            System.arraycopy(encryptedData, 16, actualEncryptedData, 0, actualEncryptedData.length);
            
            // Convert key to bytes and create SecretKeySpec
            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            
            // Decrypt data
            return cipher.doFinal(actualEncryptedData);
        }
        
        @Override
        public String generateKey() {
            try {
                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(256);
                SecretKey secretKey = keyGen.generateKey();
                return Base64.getEncoder().encodeToString(secretKey.getEncoded());
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Failed to generate AES key", e);
            }
        }
    }
    
    // RSA encryption strategy
    private class RSAEncryptionStrategy implements EncryptionStrategy {
        private KeyPair keyPair;
        
        public RSAEncryptionStrategy() {
            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
                keyPair = keyPairGenerator.generateKeyPair();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Failed to initialize RSA strategy", e);
            }
        }
        
        @Override
        public byte[] encrypt(byte[] data, String key) throws Exception {
            // In RSA, we encrypt with the public key
            Key publicKey = keyPair.getPublic();
            if (key != null && !key.isEmpty()) {
                // If a key is provided, use it instead of the generated one
                byte[] keyBytes = Base64.getDecoder().decode(key);
                // Implementation would reconstruct the public key from bytes
            }
            
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            
            return cipher.doFinal(data);
        }
        
        @Override
        public byte[] decrypt(byte[] encryptedData, String key) throws Exception {
            // In RSA, we decrypt with the private key
            Key privateKey = keyPair.getPrivate();
            if (key != null && !key.isEmpty()) {
                // If a key is provided, use it instead of the generated one
                byte[] keyBytes = Base64.getDecoder().decode(key);
                // Implementation would reconstruct the private key from bytes
            }
            
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            
            return cipher.doFinal(encryptedData);
        }
        
        @Override
        public String generateKey() {
            // Return the base64 encoded public key
            return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        }
        
        public String getPrivateKey() {
            // Return the base64 encoded private key
            return Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        }
    }
    
    // Strategy storage
    private Map<String, EncryptionStrategy> strategies = new HashMap<>();
    
    // Constructor
    public CryptoService() {
        // Register encryption strategies
        strategies.put("AES", new AESEncryptionStrategy());
        strategies.put("RSA", new RSAEncryptionStrategy());
    }
    
    // Main encryption/decryption methods
    public byte[] encryptFile(byte[] fileData, String algorithm, String key) throws Exception {
        EncryptionStrategy strategy = strategies.get(algorithm);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported encryption algorithm: " + algorithm);
        }
        return strategy.encrypt(fileData, key);
    }
    
    public byte[] decryptFile(byte[] encryptedData, String algorithm, String key) throws Exception {
        EncryptionStrategy strategy = strategies.get(algorithm);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported encryption algorithm: " + algorithm);
        }
        return strategy.decrypt(encryptedData, key);
    }
    
    public String generateKey(String algorithm) {
        EncryptionStrategy strategy = strategies.get(algorithm);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported encryption algorithm: " + algorithm);
        }
        return strategy.generateKey();
    }
    
    public String calculateChecksum(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data);
            
            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to calculate checksum", e);
        }
    }
    
    // Get RSA private key if using RSA
    public String getRSAPrivateKey() {
        EncryptionStrategy strategy = strategies.get("RSA");
        if (strategy instanceof RSAEncryptionStrategy) {
            return ((RSAEncryptionStrategy) strategy).getPrivateKey();
        }
        return null;
    }
}