package tcgserver;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import io.restassured.RestAssured;
import static org.hamcrest.Matchers.*;

import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
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
    public void getCard() {
        // Arrange
        Card card = new Card(5);
        cardRepository.save(card);

        // Act
        ValidatableResponse response = get("/api/card/" + card.getId()).then();

        // Assert
        response.assertThat()
                .body("id", equalTo(card.getId()))
                .body("mana", equalTo(5));
    }

    @Test
    public void getGame() {
        // Arrange
        Game game = new Game(Arrays.asList(new Card(1), new Card(2), new Card(3)));
        game.addPlayer(new Player("userId1", game.getInitialDeck(), Collections.emptyList()));
        game.addPlayer(new Player("userId2", game.getInitialDeck(), Collections.emptyList()));
        gameRepository.save(game);

        // Act
        ValidatableResponse response = get("/api/game/" + game.getId()).then();

        // Assert
        response.assertThat()
                .body("id", equalTo(game.getId()))
                .body("initialDeck.size()", equalTo(3))
                .body("players.size()", equalTo(2))
                .body("players[0].userId", equalTo("userId1"))
                .body("players[1].userId", equalTo("userId2"));
    }

    @Test
    public void getUser() {
        // Arrange
        User user = new User();
        userRepository.save(user);

        // Act
        ValidatableResponse response = get("/api/user/" + user.getId()).then();

        // Assert
        response.assertThat()
                .body("id", equalTo(user.getId()));
    }
}
