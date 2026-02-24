import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

private PrivateKey loadPrivateKey(File file) throws Exception {
        // Register BouncyCastle provider if not already present
        if (java.security.Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            java.security.Security.addProvider(new BouncyCastleProvider());
        }

        try (java.io.FileReader reader = new java.io.FileReader(file);
             PEMParser pemParser = new PEMParser(reader)) {
            
            Object parsed = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            
            if (parsed instanceof PKCS8EncryptedPrivateKeyInfo) {
                // Modern encrypted PKCS#8 (BEGIN ENCRYPTED PRIVATE KEY)
                logger.info("Detected encrypted PKCS#8 private key, decrypting with passphrase...");
                String passphrase = authentication != null ? authentication.getPassphrase() : null;
                if (passphrase == null || passphrase.isEmpty()) {
                    throw new IllegalArgumentException(
                        "Encrypted private key requires a passphrase. Please provide the key password.");
                }
                PKCS8EncryptedPrivateKeyInfo encryptedInfo = (PKCS8EncryptedPrivateKeyInfo) parsed;
                PrivateKeyInfo keyInfo = encryptedInfo.decryptPrivateKeyInfo(
                    new JceOpenSSLPKCS8DecryptorProviderBuilder().build(passphrase.toCharArray()));
                return converter.getPrivateKey(keyInfo);
                
            } else if (parsed instanceof PEMEncryptedKeyPair) {
                // Legacy OpenSSL encrypted (BEGIN RSA PRIVATE KEY + Proc-Type: 4,ENCRYPTED)
                logger.info("Detected legacy OpenSSL encrypted key, decrypting with passphrase...");
                String passphrase = authentication != null ? authentication.getPassphrase() : null;
                if (passphrase == null || passphrase.isEmpty()) {
                    throw new IllegalArgumentException(
                        "Encrypted private key requires a passphrase. Please provide the key password.");
                }
                PEMEncryptedKeyPair encryptedPair = (PEMEncryptedKeyPair) parsed;
                PEMKeyPair keyPair = encryptedPair.decryptKeyPair(
                    new JcePEMDecryptorProviderBuilder().build(passphrase.toCharArray()));
                return converter.getPrivateKey(keyPair.getPrivateKeyInfo());
                
            } else if (parsed instanceof PEMKeyPair) {
                // Unencrypted PKCS#1 (BEGIN RSA PRIVATE KEY)
                logger.info("Detected unencrypted PKCS#1 key pair.");
                PEMKeyPair keyPair = (PEMKeyPair) parsed;
                return converter.getPrivateKey(keyPair.getPrivateKeyInfo());
                
            } else if (parsed instanceof PrivateKeyInfo) {
                // Unencrypted PKCS#8 (BEGIN PRIVATE KEY)
                logger.info("Detected unencrypted PKCS#8 private key.");
                return converter.getPrivateKey((PrivateKeyInfo) parsed);
                
            } else {
                throw new IllegalArgumentException(
                    "Unsupported PEM key format: " + (parsed != null ? parsed.getClass().getName() : "null"));
            }
        }
    }
