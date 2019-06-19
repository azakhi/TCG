package tcgserver;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Document("users")
public class User {
    private static final String SALT = "salt";

    @Id
    private String id;
    private String name;
    private String password;
    private String authToken;
    private long expiresIn;

    public User() {
        this("", "");
    }

    public User(String name, String password) {
        this.name = name;
        this.password = password;
        this.authToken = "";
        this.expiresIn = 0;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getAuthToken() {
        return authToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public class UserSimple {
        public String id;
        public String name;
    }

    public UserSimple getSimple() {
        UserSimple userSimple = new UserSimple();
        userSimple.id = id;
        userSimple.name = name;

        return userSimple;
    }

    public class UserAuthorized {
        public String id;
        public String name;
        public String authToken;
        public long expiresIn;
    }

    public UserAuthorized getAuthorized() {
        UserAuthorized userAuthorized = new UserAuthorized();
        userAuthorized.id = id;
        userAuthorized.name = name;
        userAuthorized.authToken = authToken;
        userAuthorized.expiresIn = expiresIn;

        return userAuthorized;
    }

    public void refreshAuthToken() {
        String token = "";
        String input = id + password + System.currentTimeMillis() + SALT; // Pretty simple, not a really good way
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger signumRep = new BigInteger(1, messageDigest);
            token = signumRep.toString(16);
            while (token.length() < 32) token = "0" + token;
        } catch (NoSuchAlgorithmException e) {
            // TODO: Handle in a better way
        }

        authToken = token;
        expiresIn = System.currentTimeMillis() + 3 * 60 * 60 * 1000;
    }

    public static String passwordToHash(String password) {
        String hash = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest((SALT + password + SALT).getBytes());
            BigInteger signumRep = new BigInteger(1, messageDigest);
            hash = signumRep.toString(16);
            while (hash.length() < 32) hash = "0" + hash;
        } catch (NoSuchAlgorithmException e) {
            // TODO: Handle in a better way
        }

        return hash;
    }
}
