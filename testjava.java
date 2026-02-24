private PrivateKey loadPrivateKey(File file) throws Exception {
    String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
    
    boolean isPkcs1 = content.contains("BEGIN RSA PRIVATE KEY");
    boolean isEncryptedPkcs8 = content.contains("BEGIN ENCRYPTED PRIVATE KEY");
    
    // Legacy OpenSSL encrypted format (Proc-Type: 4,ENCRYPTED) — still not supported
    if (!isEncryptedPkcs8 && content.contains("ENCRYPTED")) {
        throw new IllegalArgumentException(
            "Legacy OpenSSL encrypted PEM keys are not supported. " +
            "Please convert using: openssl pkcs8 -topk8 -in old.key -out new.key");
    }

    String[] lines = content.split("\\R");
    StringBuilder sb = new StringBuilder();
    boolean inKey = false;
    for (String line : lines) {
        String trimmed = line.trim();
        if (trimmed.startsWith("-----BEGIN")) {
            inKey = true;
            continue;
        }
        if (trimmed.startsWith("-----END")) {
            inKey = false;
            break;
        }
        if (inKey) {
            if (trimmed.contains(":")) {
                continue;
            }
            sb.append(trimmed);
        }
    }
    
    String b64 = sb.toString().replaceAll("\\s+", "");
    byte[] decoded = Base64.getDecoder().decode(b64);
    
    // Handle encrypted PKCS#8 keys using Java built-in API
    if (isEncryptedPkcs8) {
        String passphrase = authentication != null ? authentication.getPassphrase() : null;
        if (passphrase == null || passphrase.isEmpty()) {
            throw new IllegalArgumentException(
                "Encrypted private key requires a passphrase. Please provide the key password.");
        }
        logger.info("Decrypting encrypted PKCS#8 private key...");
        javax.crypto.EncryptedPrivateKeyInfo encryptedInfo = 
            new javax.crypto.EncryptedPrivateKeyInfo(decoded);
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(encryptedInfo.getAlgName());
        javax.crypto.SecretKeyFactory keyFactory = 
            javax.crypto.SecretKeyFactory.getInstance(encryptedInfo.getAlgName());
        java.security.spec.PBEKeySpec pbeKeySpec = 
            new java.security.spec.PBEKeySpec(passphrase.toCharArray());
        javax.crypto.SecretKey pbeKey = keyFactory.generateSecret(pbeKeySpec);
        java.security.AlgorithmParameters algParams = encryptedInfo.getAlgParameters();
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, pbeKey, algParams);
        PKCS8EncodedKeySpec spec = encryptedInfo.getKeySpec(cipher);
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (Exception e) {
            return KeyFactory.getInstance("EC").generatePrivate(spec);
        }
    }
    
    if (isPkcs1) {
        logger.info("Detected PKCS#1 RSA key, wrapping to PKCS#8...");
        decoded = wrapPkcs1ToPkcs8(decoded);
    }
    
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
    try {
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    } catch (Exception e) {
        try {
            return KeyFactory.getInstance("EC").generatePrivate(spec);
        } catch (Exception e2) {
            throw new Exception("Failed to load private key as RSA or EC: " + e.getMessage(), e);
        }
    }
}
