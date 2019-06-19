package tcgserver;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import io.restassured.RestAssured;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.codec.binary.Base64;
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

import java.util.ArrayList;
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
        User user = createUserForTest();
        for (int i = 0; i < Game.START_CARD_COUNT; i++) {
            Card card = new Card(5);
            cardRepository.save(card);
        }
        String token = new String(Base64.encodeBase64((user.getId() + ":" + user.getAuthToken()).getBytes()));

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true).redirects().follow(false)
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .header("Authorization", "Bearer " + token)
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
        String token = new String(Base64.encodeBase64(("a:b").getBytes()));

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true).redirects().follow(false)
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .header("Authorization", "Bearer " + token)
                .post("/api/games").then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("status", equalTo(HttpStatus.UNAUTHORIZED.value()))
                .body("message", notNullValue());
    }

    @Test
    public void getGame() {
        // Arrange
        Game game = new Game();
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
    public void startGame() {
        // Arrange
        Assume.assumeTrue(Game.MIN_PLAYERS > 0);
        userRepository.deleteAll();
        ArrayList<User> users = new ArrayList<>();
        Game game = new Game(null, false);
        for (int i = 0; i < Game.MIN_PLAYERS; i++) {
            User user = createUserForTest();
            game.addPlayer(user);
            users.add(user);
        }
        gameRepository.save(game);
        Assume.assumeTrue(game.getState() == Game.GameState.INITIAL);
        String token = new String(Base64.encodeBase64((users.get(0).getId() + ":" + users.get(0).getAuthToken()).getBytes()));

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true)
                .param("state", Game.GameState.ACTIVE)
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .header("Authorization", "Bearer " + token)
                .post("/api/games/" + game.getId()).then();

        // Assert
        response.assertThat()
                .body("id", equalTo(game.getId()))
                .body("state", equalTo(Game.GameState.ACTIVE.toString()));
    }

    @Test
    public void startGame_GameNotFount() {
        // Arrange
        gameRepository.deleteAll();
        User user = createUserForTest();
        String token = new String(Base64.encodeBase64((user.getId() + ":" + user.getAuthToken()).getBytes()));

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true)
                .param("state", Game.GameState.ACTIVE)
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .header("Authorization", "Bearer " + token)
                .post("/api/games/a").then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", notNullValue());
    }

    @Test
    public void startGame_Unauthorized() {
        // Arrange
        Assume.assumeTrue(Game.MIN_PLAYERS > 0);
        userRepository.deleteAll();
        ArrayList<User> users = new ArrayList<>();
        Game game = new Game(null, false);
        for (int i = 0; i < Game.MIN_PLAYERS; i++) {
            User user = createUserForTest();
            game.addPlayer(user);
            users.add(user);
        }
        gameRepository.save(game);
        Assume.assumeTrue(game.getState() == Game.GameState.INITIAL);
        String token = new String(Base64.encodeBase64(("a:b").getBytes()));

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true)
                .param("state", Game.GameState.ACTIVE)
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .header("Authorization", "Bearer " + token)
                .post("/api/games/" + game.getId()).then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("status", equalTo(HttpStatus.UNAUTHORIZED.value()))
                .body("message", notNullValue());
    }

    @Test
    public void startGame_NotPlayer() {
        // Arrange
        Assume.assumeTrue(Game.MIN_PLAYERS > 0);
        userRepository.deleteAll();
        ArrayList<User> users = new ArrayList<>();
        Game game = new Game(null, false);
        for (int i = 0; i < Game.MIN_PLAYERS; i++) {
            User user = createUserForTest();
            game.addPlayer(user);
            users.add(user);
        }
        gameRepository.save(game);
        Assume.assumeTrue(game.getState() == Game.GameState.INITIAL);
        User user = createUserForTest();
        String token = new String(Base64.encodeBase64((user.getId() + ":" + user.getAuthToken()).getBytes()));

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true)
                .param("state", Game.GameState.ACTIVE)
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .header("Authorization", "Bearer " + token)
                .post("/api/games/" + game.getId()).then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .body("status", equalTo(HttpStatus.FORBIDDEN.value()))
                .body("message", notNullValue());
    }

    @Test
    public void startGame_NotEnoughPlayers() {
        // Arrange
        Assume.assumeTrue(Game.MIN_PLAYERS > 1);
        Game game = new Game(null, false);
        User user = createUserForTest();
        game.addPlayer(user);
        gameRepository.save(game);
        Assume.assumeTrue(game.getState() == Game.GameState.INITIAL);
        String token = new String(Base64.encodeBase64((user.getId() + ":" + user.getAuthToken()).getBytes()));

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true)
                .param("state", Game.GameState.ACTIVE)
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .header("Authorization", "Bearer " + token)
                .post("/api/games/" + game.getId()).then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(HttpStatus.BAD_REQUEST.value()))
                .body("message", notNullValue());
    }


    @Test
    public void getPlayers() {
        // Arrange
        Game game = new Game();
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
        Game game = new Game();
        gameRepository.save(game);
        User user = createUserForTest();
        String token = new String(Base64.encodeBase64((user.getId() + ":" + user.getAuthToken()).getBytes()));

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true).redirects().follow(false)
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .header("Authorization", "Bearer " + token)
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
        User user = createUserForTest();
        String token = new String(Base64.encodeBase64((user.getId() + ":" + user.getAuthToken()).getBytes()));

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true).redirects().follow(false)
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .header("Authorization", "Bearer " + token)
                .post("/api/games/a/players").then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", notNullValue());
    }

    @Test
    public void addPlayer_UserNotFound() {
        // Arrange
        Game game = new Game();
        gameRepository.save(game);
        userRepository.deleteAll();
        String token = new String(Base64.encodeBase64(("a:b").getBytes()));

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true).redirects().follow(false)
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .header("Authorization", "Bearer " + token)
                .post("/api/games/" + game.getId() + "/players").then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("status", equalTo(HttpStatus.UNAUTHORIZED.value()))
                .body("message", notNullValue());
    }

    @Test
    public void addPlayer_MaxPlayers() {
        // Arrange
        Game game = new Game();
        for (int i = 0; i < Game.MAX_PLAYERS; i++) {
            User temp = createUserForTest();
            game.addPlayer(temp);
        }
        Assume.assumeTrue(game.getPlayers().size() >= Game.MAX_PLAYERS);
        gameRepository.save(game);
        User user = createUserForTest();
        String token = new String(Base64.encodeBase64((user.getId() + ":" + user.getAuthToken()).getBytes()));

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true).redirects().follow(false)
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .header("Authorization", "Bearer " + token)
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
        User user = createUserForTest();
        Game game = new Game();
        int index = game.addPlayer(user);
        Assume.assumeTrue(index >= 0);
        gameRepository.save(game);
        String token = new String(Base64.encodeBase64((user.getId() + ":" + user.getAuthToken()).getBytes()));

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true).redirects().follow(false)
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .header("Authorization", "Bearer " + token)
                .post("/api/games/" + game.getId() + "/players").then();

        // Assert
        response.assertThat()
                .statusCode(302)
                .header("Location", equalTo("/api/games/" + game.getId() + "/players/" + index));
    }

    @Test
    public void getPlayer() {
        // Arrange
        Game game = new Game();
        userRepository.deleteAll();
        User user = createUserForTest();
        game.addPlayer(user);
        gameRepository.save(game);
        String token = new String(Base64.encodeBase64((user.getId() + ":" + user.getAuthToken()).getBytes()));

        // Act
        ValidatableResponse response = given()
                .header("Authorization", "Bearer " + token)
                .get("/api/games/" + game.getId() + "/players/0").then();

        // Assert
        response.assertThat()
                .body("userId", equalTo(user.getId()))
                .body("deck.size()", equalTo(3))
                .body("hand.size()", equalTo(0));
    }

    @Test
    public void getPlayer_NotFound() {
        // Arrange
        Game game = new Game();
        gameRepository.save(game);
        userRepository.deleteAll();
        User user = createUserForTest();
        String token = new String(Base64.encodeBase64((user.getId() + ":" + user.getAuthToken()).getBytes()));

        // Act
        ValidatableResponse response = given()
                .header("Authorization", "Bearer " + token)
                .get("/api/games/" + game.getId() + "/players/0").then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", notNullValue());
    }

    @Test
    public void getPlayer_Unauthorized() {
        // Arrange
        userRepository.deleteAll();
        User user = createUserForTest();
        Game game = new Game();
        game.addPlayer(user);
        gameRepository.save(game);
        String token = new String(Base64.encodeBase64(("a:b").getBytes()));

        // Act
        ValidatableResponse response = given()
                .header("Authorization", "Bearer " + token)
                .get("/api/games/" + game.getId() + "/players/0").then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("status", equalTo(HttpStatus.UNAUTHORIZED.value()))
                .body("message", notNullValue());
    }

    @Test
    public void getPlayer_GameNotFound() {
        // Arrange
        gameRepository.deleteAll();
        userRepository.deleteAll();
        User user = createUserForTest();
        String token = new String(Base64.encodeBase64((user.getId() + ":" + user.getAuthToken()).getBytes()));

        // Act
        ValidatableResponse response = given()
                .header("Authorization", "Bearer " + token)
                .get("/api/games/a/players/0").then();

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
        userRepository.deleteAll();
        ArrayList<User> users = new ArrayList<>();
        Game game = new Game(null, false);
        for (int i = 0; i < Game.MIN_PLAYERS; i++) {
            User user = createUserForTest();
            game.addPlayer(user);
            users.add(user);
        }
        Assume.assumeTrue(game.start());
        gameRepository.save(game);
        String token = new String(Base64.encodeBase64((users.get(game.getActivePlayer()).getId() + ":" + users.get(game.getActivePlayer()).getAuthToken()).getBytes()));
        int currentActionCount = game.getActions().size();

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true).redirects().follow(false)
                .param("type", Game.ActionType.DRAW_CARD)
                .param("player", game.getActivePlayer())
                .param("index", 0)
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .header("Authorization", "Bearer " + token)
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
        User user = createUserForTest();
        String token = new String(Base64.encodeBase64((user.getId() + ":" + user.getAuthToken()).getBytes()));

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true).redirects().follow(false)
                .param("type", Game.ActionType.DRAW_CARD)
                .param("player", 0)
                .param("index", 0)
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .header("Authorization", "Bearer " + token)
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
        userRepository.deleteAll();
        ArrayList<User> users = new ArrayList<>();
        Game game = new Game(null, false);
        for (int i = 0; i < Game.MIN_PLAYERS; i++) {
            User user = createUserForTest();
            game.addPlayer(user);
            users.add(user);
        }
        Assume.assumeTrue(game.start());
        String token = new String(Base64.encodeBase64((users.get(game.getActivePlayer()).getId() + ":" + users.get(game.getActivePlayer()).getAuthToken()).getBytes()));
        gameRepository.save(game);

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true).redirects().follow(false)
                .param("type", Game.ActionType.PLAY_CARD)
                .param("player", game.getActivePlayer())
                .param("index", -1)
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .header("Authorization", "Bearer " + token)
                .post("/api/games/" + game.getId() + "/actions").then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(HttpStatus.BAD_REQUEST.value()))
                .body("message", notNullValue());
    }

    @Test
    public void addAction_DifferentPlayer() {
        // Arrange
        userRepository.deleteAll();
        ArrayList<User> users = new ArrayList<>();
        Game game = new Game(null, false);
        for (int i = 0; i < Game.MIN_PLAYERS; i++) {
            User user = createUserForTest();
            game.addPlayer(user);
            users.add(user);
        }
        Assume.assumeTrue(game.start());
        String token = new String(Base64.encodeBase64((users.get(game.getActivePlayer()).getId() + ":" + users.get(game.getActivePlayer()).getAuthToken()).getBytes()));
        gameRepository.save(game);

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true).redirects().follow(false)
                .param("type", Game.ActionType.DRAW_CARD)
                .param("player", -1)
                .param("index", 0)
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .header("Authorization", "Bearer " + token)
                .post("/api/games/" + game.getId() + "/actions").then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(HttpStatus.BAD_REQUEST.value()))
                .body("message", notNullValue());
    }

    @Test
    public void addAction_Unauthorized() {
        // Arrange
        userRepository.deleteAll();
        ArrayList<User> users = new ArrayList<>();
        Game game = new Game(null, false);
        for (int i = 0; i < Game.MIN_PLAYERS; i++) {
            User user = createUserForTest();
            game.addPlayer(user);
            users.add(user);
        }
        Assume.assumeTrue(game.start());
        gameRepository.save(game);
        String token = new String(Base64.encodeBase64(("a:b").getBytes()));

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true).redirects().follow(false)
                .param("type", Game.ActionType.DRAW_CARD)
                .param("player", 0)
                .param("index", 0)
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .header("Authorization", "Bearer " + token)
                .post("/api/games/" + game.getId() + "/actions").then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("status", equalTo(HttpStatus.UNAUTHORIZED.value()))
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
    public void getAction_GameNotFound() {
        // Arrange
        gameRepository.deleteAll();

        // Act
        ValidatableResponse response = get("/api/games/a/actions/0").then();

        // Assert
        response.assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", notNullValue());
    }

    @Test
    public void getUser() {
        // Arrange
        User user = createUserForTest();

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

    @Test
    public void createUser() {
        // Arrange
        userRepository.deleteAll();

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true)
                .param("name", "username")
                .param("password", "123")
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .post("/api/users").then();

        // Assert
        response.assertThat()
                .body("id", notNullValue())
                .body("name", equalTo("username"))
                .body("authToken", notNullValue())
                .body("expiresIn", greaterThanOrEqualTo(System.currentTimeMillis()));

        assertEquals(1, userRepository.findByName("username").size());
    }

    @Test
    public void getAuthToken() {
        // Arrange
        userRepository.deleteAll();
        User user = createUserForTest("username", "123");

        // Act
        ValidatableResponse response = given().urlEncodingEnabled(true)
                .param("name", "username")
                .param("password", "123")
                .header("Accept", ContentType.JSON.getAcceptHeader())
                .post("/api/users/auth").then();

        // Assert
        response.assertThat()
                .body("id", notNullValue())
                .body("name", equalTo("username"))
                .body("authToken", notNullValue())
                .body("expiresIn", greaterThanOrEqualTo(System.currentTimeMillis()));
    }

    private User createUserForTest() {
        return createUserForTest("", "");
    }

    private User createUserForTest(String name, String password) {
        User user = new User(name, User.passwordToHash(password));
        userRepository.save(user);
        user.refreshAuthToken();
        userRepository.save(user);
        return user;
    }
}
