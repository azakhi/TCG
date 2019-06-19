package tcgserver;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import io.restassured.RestAssured;
import static org.hamcrest.Matchers.*;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Assume;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Main.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, properties = "spring.profiles.active=test")
public class APIControllerTest {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @Before
    public void setUp () {
        RestAssured.port = 8080;
        RestAssured.baseURI = "http://localhost";
        gameRepository.deleteAll();
        cardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @After
    public void tearDown() {
        gameRepository.deleteAll();
        cardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void getApiIndex() {
        get("/api").then().assertThat().body("version", equalTo("0.1"));
    }

    @Test
    public void getCards() {
        // Arrange
        cardRepository.deleteAll();
        Card card1 = new Card(1);
        cardRepository.save(card1);
        Card card2 = new Card(2);
        cardRepository.save(card2);

        // Act
        ValidatableResponse response = get("/api/cards").then();

        // Assert
        response.assertThat()
                .body("size()", equalTo(2))
                .body("[0].id", equalTo(card1.getId()))
                .body("[1].id", equalTo(card2.getId()));
    }

    @Test
    public void getCard() {
        // Arrange
        Card card = new Card(5);
        cardRepository.save(card);

        // Act
        ValidatableResponse response = get("/api/cards/" + card.getId()).then();

        // Assert
        response.assertThat()
                .body("id", equalTo(card.getId()))
                .body("mana", equalTo(5));
    }

    @Test
    public void getCard_NotFound() {
        // Arrange
        cardRepository.deleteAll();

        // Act
        ValidatableResponse response = get("/api/cards/a").then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", notNullValue());
    }

    @Test
    public void getGames() {
        // Arrange
        gameRepository.deleteAll();
        Game game1 = new Game();
        gameRepository.save(game1);
        Game game2 = new Game();
        gameRepository.save(game2);

        // Act
        ValidatableResponse response = get("/api/games").then();

        // Assert
        response.assertThat()
                .body("size()", equalTo(2))
                .body("[0].id", equalTo(game1.getId()))
                .body("[1].id", equalTo(game2.getId()));
    }

    @Test
    public void createGame() {
        // Arrange
        User user = new User();
        userRepository.save(user);
        for (int i = 0; i < Game.START_CARD_COUNT; i++) {
            Card card = new Card(5);
            cardRepository.save(card);
        }

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true).redirects().follow(false)
                .param("user", user.getId())
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .post("/api/games").then();

        // Assert
        response.assertThat()
                .statusCode(302)
                .header("Location", startsWith("/api/games/"));
    }

    @Test
    public void createGame_UserNotFound() {
        // Arrange
        userRepository.deleteAll();

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true).redirects().follow(false)
                .param("user", "a")
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .post("/api/games").then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(HttpStatus.BAD_REQUEST.value()))
                .body("message", notNullValue());
    }

    @Test
    public void getGame() {
        // Arrange
        Game game = new Game(Arrays.asList(new Card(1), new Card(2), new Card(3)));
        game.addPlayer(new Player("userId1", game.getInitialDeck(), Collections.emptyList()));
        game.addPlayer(new Player("userId2", game.getInitialDeck(), Collections.emptyList()));
        gameRepository.save(game);

        // Act
        ValidatableResponse response = get("/api/games/" + game.getId()).then();

        // Assert
        response.assertThat()
                .body("id", equalTo(game.getId()))
                .body("initialDeck.size()", equalTo(3))
                .body("players.size()", equalTo(2))
                .body("players[0].userId", equalTo("userId1"))
                .body("players[1].userId", equalTo("userId2"));
    }

    @Test
    public void getGame_NotFound() {
        // Arrange
        gameRepository.deleteAll();

        // Act
        ValidatableResponse response = get("/api/games/a").then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", notNullValue());
    }

    @Test
    public void getPlayers() {
        // Arrange
        Game game = new Game(Arrays.asList(new Card(1), new Card(2), new Card(3)));
        game.addPlayer(new Player("userId1", game.getInitialDeck(), Collections.emptyList()));
        game.addPlayer(new Player("userId2", game.getInitialDeck(), Collections.emptyList()));
        gameRepository.save(game);

        // Act
        ValidatableResponse response = get("/api/games/" + game.getId() + "/players").then();

        // Assert
        response.assertThat()
                .body("size()", equalTo(2))
                .body("[0].userId", equalTo("userId1"))
                .body("[1].userId", equalTo("userId2"));
    }

    @Test
    public void getPlayers_GameNotFound() {
        // Arrange
        gameRepository.deleteAll();

        // Act
        ValidatableResponse response = get("/api/games/a/players").then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", notNullValue());
    }

    @Test
    public void addPlayer() {
        // Arrange
        Game game = new Game(Arrays.asList(new Card(1), new Card(2), new Card(3)));
        gameRepository.save(game);
        User user = new User();
        userRepository.save(user);

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true).redirects().follow(false)
                .param("user", user.getId())
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .post("/api/games/" + game.getId() + "/players").then();

        // Assert
        response.assertThat()
                .statusCode(302)
                .header("Location", equalTo("/api/games/" + game.getId() + "/players/0"));
    }

    @Test
    public void addPlayer_GameNotFound() {
        // Arrange
        gameRepository.deleteAll();
        User user = new User();
        userRepository.save(user);

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true).redirects().follow(false)
                .param("user", user.getId())
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .post("/api/games/a/players").then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", notNullValue());
    }

    @Test
    public void addPlayer_MaxPlayers() {
        // Arrange
        Game game = new Game();
        gameRepository.save(game);
        for (int i = 0; i < Game.MAX_PLAYERS; i++) {
            User temp = new User();
            userRepository.save(temp);
            game.addPlayer(temp);
        }
        Assume.assumeTrue(game.getPlayers().size() >= Game.MAX_PLAYERS);
        User user = new User();
        userRepository.save(user);

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true).redirects().follow(false)
                .param("user", user.getId())
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .post("/api/games/" + game.getId() + "/players").then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(HttpStatus.BAD_REQUEST.value()))
                .body("message", notNullValue());
    }

    @Test
    public void addPlayer_ExistingPlayer() {
        // Arrange
        User user = new User();
        userRepository.save(user);
        Game game = new Game(Arrays.asList(new Card(1), new Card(2), new Card(3)));
        int index = game.addPlayer(user);
        Assume.assumeTrue(index >= 0);
        gameRepository.save(game);

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true).redirects().follow(false)
                .param("user", user.getId())
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .post("/api/games/" + game.getId() + "/players").then();

        // Assert
        response.assertThat()
                .statusCode(302)
                .header("Location", equalTo("/api/games/" + game.getId() + "/players/" + index));
    }

    @Test
    public void getPlayer() {
        // Arrange
        Game game = new Game(Arrays.asList(new Card(1), new Card(2), new Card(3)));
        game.addPlayer(new Player("userId", game.getInitialDeck(), Collections.emptyList()));
        gameRepository.save(game);

        // Act
        ValidatableResponse response = get("/api/games/" + game.getId() + "/players/0").then();

        // Assert
        response.assertThat()
                .body("userId", equalTo("userId"))
                .body("deck.size()", equalTo(3))
                .body("hand.size()", equalTo(0));
    }

    @Test
    public void getPlayer_NotFound() {
        // Arrange
        Game game = new Game();
        gameRepository.save(game);

        // Act
        ValidatableResponse response = get("/api/games/" + game.getId() + "/players/0").then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", notNullValue());
    }

    @Test
    public void getActions() {
        // Arrange
        Game game = new Game();
        for (int i = 0; i < Game.MIN_PLAYERS; i++) {
            game.addPlayer(new Player("userId" + i, game.getInitialDeck(), Collections.emptyList()));
        }
        Assume.assumeTrue(game.start());
        if (!game.isDrawCardAtTurnStart()) {
            Assume.assumeTrue(game.addAction(new Game.Action(game.getActivePlayer(), Game.ActionType.DRAW_CARD)));
        }
        gameRepository.save(game);

        // Act
        ValidatableResponse response = get("/api/games/" + game.getId() + "/actions").then();

        // Assert
        response.assertThat()
                .body("size()", equalTo(1))
                .body("[0].type", equalTo("DRAW_CARD"));
    }

    @Test
    public void getActions_GameNotFound() {
        // Arrange
        gameRepository.deleteAll();

        // Act
        ValidatableResponse response = get("/api/games/a/actions").then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", notNullValue());
    }

    @Test
    public void addAction() {
        // Arrange
        Game game = new Game(null, false);
        for (int i = 0; i < Game.MIN_PLAYERS; i++) {
            game.addPlayer(new Player("userId" + i, game.getInitialDeck(), Collections.emptyList()));
        }
        Assume.assumeTrue(game.start());
        gameRepository.save(game);
        int currentActionCount = game.getActions().size();

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true).redirects().follow(false)
                .param("type", Game.ActionType.DRAW_CARD)
                .param("player", game.getActivePlayer())
                .param("index", 0)
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .post("/api/games/" + game.getId() + "/actions").then();

        // Assert
        response.assertThat()
                .statusCode(302)
                .header("Location", equalTo("/api/games/" + game.getId() + "/actions/" + currentActionCount));
    }

    @Test
    public void addAction_GameNotFound() {
        // Arrange
        gameRepository.deleteAll();

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true).redirects().follow(false)
                .param("type", Game.ActionType.DRAW_CARD)
                .param("player", 0)
                .param("index", 0)
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .post("/api/games/a/actions").then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", notNullValue());
    }

    @Test
    public void addAction_InvalidAction() {
        // Arrange
        Game game = new Game(null, false);
        for (int i = 0; i < Game.MIN_PLAYERS; i++) {
            game.addPlayer(new Player("userId" + i, game.getInitialDeck(), Collections.emptyList()));
        }
        Assume.assumeTrue(game.start());
        gameRepository.save(game);

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true).redirects().follow(false)
                .param("type", Game.ActionType.DRAW_CARD)
                .param("player", -1)
                .param("index", 0)
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .post("/api/games/" + game.getId() + "/actions").then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(HttpStatus.BAD_REQUEST.value()))
                .body("message", notNullValue());
    }

    @Test
    public void getAction() {
        // Arrange
        Game game = new Game();
        for (int i = 0; i < Game.MIN_PLAYERS; i++) {
            game.addPlayer(new Player("userId" + i, game.getInitialDeck(), Collections.emptyList()));
        }
        Assume.assumeTrue(game.start());
        if (!game.isDrawCardAtTurnStart()) {
            Assume.assumeTrue(game.addAction(new Game.Action(game.getActivePlayer(), Game.ActionType.DRAW_CARD)));
        }
        gameRepository.save(game);

        // Act
        ValidatableResponse response = get("/api/games/" + game.getId() + "/actions/0").then();

        // Assert
        response.assertThat()
                .body("type", equalTo("DRAW_CARD"));
    }

    @Test
    public void getAction_NotFound() {
        // Arrange
        Game game = new Game(null, false);
        for (int i = 0; i < Game.MIN_PLAYERS; i++) {
            game.addPlayer(new Player("userId" + i, game.getInitialDeck(), Collections.emptyList()));
        }
        Assume.assumeTrue(game.start());
        gameRepository.save(game);

        // Act
        ValidatableResponse response = get("/api/games/" + game.getId() + "/actions/-1").then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", notNullValue());
    }

    @Test
    public void getUser() {
        // Arrange
        User user = new User();
        userRepository.save(user);

        // Act
        ValidatableResponse response = get("/api/users/" + user.getId()).then();

        // Assert
        response.assertThat()
                .body("id", equalTo(user.getId()));
    }

    @Test
    public void getUser_NotFound() {
        // Arrange
        userRepository.deleteAll();

        // Act
        ValidatableResponse response = get("/api/users/a").then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", notNullValue());
    }
}
