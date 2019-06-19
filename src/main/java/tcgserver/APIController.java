package tcgserver;

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
    public Card.CardSimple card(@PathVariable("id") String id) {
        Optional<Card> card = cardRepository.findById(id);
        if (card.isPresent()) {
            return card.get().getSimple();
        }

        return null;
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
    public ResponseEntity<String> createGame(String user) {
        Optional<User> userObject = userRepository.findById(user);
        if (userObject.isPresent()) {
            Game game = new Game(cardRepository.findAll());
            game.addPlayer(userObject.get());
            gameRepository.save(game);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/api/games/" + game.getId()))
                    .build();
        }

        return null;
    }

    @RequestMapping(value = "/api/games/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game.GameSimple game(@PathVariable("id") String id) {
        Optional<Game> game = gameRepository.findById(id);
        if (game.isPresent()) {
            return game.get().getSimple();
        }

        return null;
    }

    @RequestMapping(value = "/api/games/{id}/players", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Player.PlayerSimple> players(@PathVariable("id") String id) {
        Optional<Game> game = gameRepository.findById(id);
        if (game.isPresent()) {
            ArrayList<Player.PlayerSimple> simples = new ArrayList<>();
            for (Player p : game.get().getPlayers()) {
                simples.add(p.getSimple());
            }

            return simples;
        }

        return null;
    }

    @RequestMapping(value = "/api/games/{id}/players", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addPlayer(@PathVariable("id") String id, String user) {
        Optional<Game> gameObject = gameRepository.findById(id);
        if (gameObject.isPresent()) {
            Game game = gameObject.get();
            Optional<User> userObject = userRepository.findById(user);
            if (userObject.isPresent()) {
                int index = game.addPlayer(userObject.get());
                if (index >= 0) {
                    gameRepository.save(game);
                    return ResponseEntity.status(HttpStatus.FOUND)
                            .location(URI.create("/api/games/" + game.getId() + "/players/" + index))
                            .build();
                }
            }
        }

        return null;
    }

    @RequestMapping(value = "/api/games/{id}/players/{index}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Player player(@PathVariable("id") String id, @PathVariable("index") int index) {
        Optional<Game> gameObject = gameRepository.findById(id);
        if (gameObject.isPresent()) {
            List<Player> players = gameObject.get().getPlayers();
            if (index >= 0 && index < players.size()) {
                return players.get(index);
            }
        }

        return null;
    }

    @RequestMapping(value = "/api/games/{id}/actions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Game.Action> actions(@PathVariable("id") String id) {
        Optional<Game> game = gameRepository.findById(id);
        if (game.isPresent()) {
            return game.get().getActions();
        }

        return null;
    }

    @RequestMapping(value = "/api/games/{id}/actions", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addAction(@PathVariable("id") String id, Game.ActionType type, int player, int index) {
        Optional<Game> game = gameRepository.findById(id);
        if (game.isPresent()) {
            Game.Action action = new Game.Action(player, type, index);
            if (game.get().addAction(action)) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("/api/games/" + game.get().getId() + "/actions/" + (game.get().getActions().size() - 1)))
                        .build();
            }
        }

        return null;
    }

    @RequestMapping(value = "/api/games/{id}/actions/{index}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game.Action action(@PathVariable("id") String id, @PathVariable("index") int index) {
        Optional<Game> gameObject = gameRepository.findById(id);
        if (gameObject.isPresent()) {
            List<Game.Action> actions = gameObject.get().getActions();
            if (index >= 0 && index < actions.size()) {
                return actions.get(index);
            }
        }

        return null;
    }

    @RequestMapping(value = "/api/users/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public User.UserSimple user(@PathVariable("id") String id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return user.get().getSimple();
        }

        return null;
    }
}
