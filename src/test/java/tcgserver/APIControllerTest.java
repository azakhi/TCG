package tcgserver;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import io.restassured.RestAssured;
import static org.hamcrest.Matchers.*;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Main.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class APIControllerTest {

    @Before
    public void setUp () {
        RestAssured.port = 8080;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    public void getApiIndex() {
        get("/api").then().assertThat().body("version", equalTo("0.1"));
    }
}
