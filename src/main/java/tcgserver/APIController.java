package tcgserver;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class APIController {

    @RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
    public String index() {
        return "{\"version\":\"0.1\"}";
    }
}
