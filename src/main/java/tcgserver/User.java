package tcgserver;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("users")
public class User {
    @Id
    private String id;

    public String getId() {
        return id;
    }

    public class UserSimple {
        public String id;
    }

    public UserSimple getSimple() {
        UserSimple userSimple = new UserSimple();
        userSimple.id = id;

        return userSimple;
    }
}
