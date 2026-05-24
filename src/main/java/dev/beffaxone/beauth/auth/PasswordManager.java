package dev.beffaxone.beauth.auth;

import at.favre.lib.crypto.bcrypt.BCrypt;
import dev.beffaxone.beauth.BeAuth;

public class PasswordManager {

    private final BeAuth plugin;

    public PasswordManager(BeAuth plugin) {
        this.plugin = plugin;
    }

    public String hashPassword(String password) {
        int rounds = plugin.getConfigManager().getBCryptRounds();
        rounds = Math.max(10, Math.min(15, rounds));
        return BCrypt.withDefaults().hashToString(rounds, password.toCharArray());
    }

    public boolean verifyPassword(String password, String hash) {
        if (hash == null || !hash.startsWith("$2a$") && !hash.startsWith("$2b$") && !hash.startsWith("$2y$")) {
            return false;
        }
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hash.toCharArray());
        return result.verified;
    }
}
