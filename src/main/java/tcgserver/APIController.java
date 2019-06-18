package tcgserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class APIController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
    public String index() {
        return "{\"version\":\"0.1\"}";
    }

    @RequestMapping(value = "/api/card/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Card.CardSimple card(@PathVariable("id") String id) {
        return null;
    }

    @RequestMapping(value = "/api/game/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game.GameSimple game(@PathVariable("id") String id) {
        return null;
    }

    @RequestMapping(value = "/api/user/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public User.UserSimple user(@PathVariable("id") String id) {
        return null;
    }
}
