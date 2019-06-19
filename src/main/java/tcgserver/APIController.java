package tcgserver;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class APIController {
    public class APIException extends Exception {
        private HttpStatus status;
        public APIException(HttpStatus status, String message) {
            super(message);
            this.status = status;
        }

        public HttpStatus getStatus() {
            return status;
        }
    }

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

    @ExceptionHandler(APIException.class)
    public ResponseEntity handleException(APIException e) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("status", e.getStatus().value());
            obj.put("message", e.getMessage());
        } catch (JSONException je) {
            // TODO: Handle in a better way
        }

        return ResponseEntity.status(e.getStatus()).contentType(MediaType.APPLICATION_JSON).body(obj.toString());
    }

    @RequestMapping(value = "/api/cards", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Card.CardSimple> cards() {
        ArrayList<Card.CardSimple> simples = new ArrayList<>();
        List<Card> cards = cardRepository.findAll();
        for (Card c : cards) {
            simples.add(c.getSimple());
        }

        return simples;
    }

    @RequestMapping(value = "/api/cards/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Card.CardSimple card(@PathVariable("id") String id) throws APIException {
        Optional<Card> card = cardRepository.findById(id);
        if (card.isPresent()) {
            return card.get().getSimple();
        }

        throw new APIException(HttpStatus.NOT_FOUND, "Card not found");
    }

    @RequestMapping(value = "/api/games", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Game.GameSimple> games() {
        ArrayList<Game.GameSimple> simples = new ArrayList<>();
        List<Game> games = gameRepository.findAll();
        for (Game g : games) {
            simples.add(g.getSimple());
        }

        return simples;
    }

    @RequestMapping(value = "/api/games", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createGame(@RequestHeader(defaultValue = "") String Authorization) throws APIException {
        User user = getAuthorizedUser(Authorization);
        if (user != null) {
            Game game = new Game(cardRepository.findAll());
            game.addPlayer(user);
            gameRepository.save(game);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/api/games/" + game.getId()))
                    .build();
        }

        throw new APIException(HttpStatus.UNAUTHORIZED, "User login is required");
    }

    @RequestMapping(value = "/api/games/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game.GameSimple game(@PathVariable("id") String id) throws APIException {
        Optional<Game> game = gameRepository.findById(id);
        if (game.isPresent()) {
            return game.get().getSimple();
        }

        throw new APIException(HttpStatus.NOT_FOUND, "Game not found");
    }

    @RequestMapping(value = "/api/games/{id}/players", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Player.PlayerSimple> players(@PathVariable("id") String id) throws APIException {
        Optional<Game> game = gameRepository.findById(id);
        if (game.isPresent()) {
            ArrayList<Player.PlayerSimple> simples = new ArrayList<>();
            for (Player p : game.get().getPlayers()) {
                simples.add(p.getSimple());
            }

            return simples;
        }

        throw new APIException(HttpStatus.NOT_FOUND, "Game not found");
    }

    @RequestMapping(value = "/api/games/{id}/players", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addPlayer(@PathVariable("id") String id, @RequestHeader(defaultValue = "") String Authorization) throws APIException {
        User user = getAuthorizedUser(Authorization);
        Optional<Game> gameObject = gameRepository.findById(id);
        if (gameObject.isPresent()) {
            Game game = gameObject.get();
            if (user != null) {
                int index = game.addPlayer(user);
                if (index >= 0) {
                    gameRepository.save(game);
                    return ResponseEntity.status(HttpStatus.FOUND)
                            .location(URI.create("/api/games/" + game.getId() + "/players/" + index))
                            .build();
                }
                else {
                    throw new APIException(HttpStatus.BAD_REQUEST, "Could not add user to the game");
                }
            }
            else {
                throw new APIException(HttpStatus.UNAUTHORIZED, "User login is required");
            }
        }

        throw new APIException(HttpStatus.NOT_FOUND, "Game not found");
    }

    @RequestMapping(value = "/api/games/{id}/players/{index}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Player player(@PathVariable("id") String id, @PathVariable("index") int index, @RequestHeader(defaultValue = "") String Authorization) throws APIException {
        Optional<Game> gameObject = gameRepository.findById(id);
        if (gameObject.isPresent()) {
            List<Player> players = gameObject.get().getPlayers();
            if (index >= 0 && index < players.size()) {
                User user = getAuthorizedUser(Authorization);
                if (user != null && user.getId().equals(players.get(index).getUserId())) {
                    return players.get(index);
                }
                else {
                    throw new APIException(HttpStatus.UNAUTHORIZED, "User login is required");
                }
            }
        }

        throw new APIException(HttpStatus.NOT_FOUND, "User not found");
    }

    @RequestMapping(value = "/api/games/{id}/actions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Game.Action> actions(@PathVariable("id") String id) throws APIException {
        Optional<Game> game = gameRepository.findById(id);
        if (game.isPresent()) {
            return game.get().getActions();
        }

        throw new APIException(HttpStatus.NOT_FOUND, "Game not found");
    }

    @RequestMapping(value = "/api/games/{id}/actions", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addAction(@PathVariable("id") String id, Game.ActionType type, int player, int index, @RequestHeader(defaultValue = "") String Authorization) throws APIException {
        Optional<Game> gameObject = gameRepository.findById(id);
        if (gameObject.isPresent()) {
            Game game = gameObject.get();
            User user = getAuthorizedUser(Authorization);
            if (user != null) {
                if (player >= 0 && player < game.getPlayers().size() && user.getId().equals(game.getPlayers().get(player).getUserId())) {
                    Game.Action action = new Game.Action(player, type, index);
                    if (game.addAction(action)) {
                        gameRepository.save(game);
                        return ResponseEntity.status(HttpStatus.FOUND)
                                .location(URI.create("/api/games/" + game.getId() + "/actions/" + (game.getActions().size() - 1)))
                                .build();
                    }
                    else {
                        throw new APIException(HttpStatus.BAD_REQUEST, "Could not add action");
                    }
                }
                else {
                    throw new APIException(HttpStatus.BAD_REQUEST, "Invalid player index");
                }
            }
            else {
                throw new APIException(HttpStatus.UNAUTHORIZED, "User login is required");
            }
        }

        throw new APIException(HttpStatus.NOT_FOUND, "Game not found");
    }

    @RequestMapping(value = "/api/games/{id}/actions/{index}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game.Action action(@PathVariable("id") String id, @PathVariable("index") int index) throws APIException {
        Optional<Game> gameObject = gameRepository.findById(id);
        if (gameObject.isPresent()) {
            List<Game.Action> actions = gameObject.get().getActions();
            if (index >= 0 && index < actions.size()) {
                return actions.get(index);
            }
            else {
                throw new APIException(HttpStatus.NOT_FOUND, "Action not found");
            }
        }

        throw new APIException(HttpStatus.NOT_FOUND, "Game not found");
    }

    @RequestMapping(value = "/api/users", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public User.UserAuthorized createUser(String name, String password) throws APIException {
        if (userRepository.findByName(name).size() <= 0) {
            User user = new User(name, User.passwordToHash(password));
            userRepository.save(user);
            user.refreshAuthToken();
            userRepository.save(user);
            return user.getAuthorized();
        }

        throw new APIException(HttpStatus.BAD_REQUEST, "User with name already exist");
    }

    @RequestMapping(value = "/api/users/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public User.UserSimple user(@PathVariable("id") String id) throws APIException {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return user.get().getSimple();
        }

        throw new APIException(HttpStatus.NOT_FOUND, "User not found");
    }

    @RequestMapping(value = "/api/users/auth", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public User.UserAuthorized auth(String name, String password) throws APIException {
        List<User> users = userRepository.findByName(name);
        if (users.size() > 0) {
            User user = users.get(0);
            if (user.getPassword().equals(User.passwordToHash(password))) {
                user.refreshAuthToken();
                userRepository.save(user);
                return user.getAuthorized();
            }
            else {
                throw new APIException(HttpStatus.BAD_REQUEST, "Invalid password");
            }
        }

        throw new APIException(HttpStatus.NOT_FOUND, "User not found");
    }

    private User getAuthorizedUser(String authHeader) {
        if (authHeader.equals("")) {
            return null;
        }

        String[] headerSplit = authHeader.split("\\s+");
        if (headerSplit.length > 1) {
            String[] credentials = new String(Base64.decodeBase64(headerSplit[1])).split(":");
            if (credentials.length == 2) {
                Optional<User> userObject = userRepository.findById(credentials[0]);
                if (userObject.isPresent()) {
                    User user = userObject.get();
                    if (user.getAuthToken().equals(credentials[1]) && user.getExpiresIn() > System.currentTimeMillis()) {
                        return user;
                    }
                }
            }
        }

        return null;
    }
}
