package com.rpg.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilidad de seguridad para hash criptografico SHA-256 (Reto Extra - EJERCICIOS.md).
 */
public final class SeguridadUtil {

    private SeguridadUtil() {
    }

    /**
     * Calcula el hash SHA-256 en representacion hexadecimal en minusculas.
     *
     * @param textoPlano texto o contraseña a hashear
     * @return cadena hexadecimal de 64 caracteres
     */
    public static String sha256(String textoPlano) {
        if (textoPlano == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(textoPlano.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 no disponible en la JVM", e);
        }
    }
}
