package de.one_piece_api.util;

import net.minecraft.util.Identifier;

public class DataGenUtil {


    public static String generateDeterministicId(Identifier skillId) {
        try {
            // SHA-256 Hash der skill ID
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(skillId.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // In Base36 konvertieren (a-z, 0-9)
            java.math.BigInteger bigInt = new java.math.BigInteger(1, hash);
            String base36 = bigInt.toString(36);

            // Auf exakt 16 Zeichen trimmen/padden
            if (base36.length() >= 16) {
                return base36.substring(0, 16);
            } else {
                // Mit Nullen auffüllen falls zu kurz
                return String.format("%-16s", base36).replace(' ', '0');
            }
        } catch (java.security.NoSuchAlgorithmException e) {
            // Fallback: einfacher Hash
            return generateSimpleHash(skillId.toString());
        }
    }

    private static String generateSimpleHash(String skillId) {
        int hash = skillId.hashCode();
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();

        // 16 Zeichen basierend auf Hash generieren
        for (int i = 0; i < 16; i++) {
            hash = hash * 31 + i; // Mix für bessere Verteilung
            result.append(chars.charAt(Math.abs(hash) % chars.length()));
        }

        return result.toString();
    }
}
