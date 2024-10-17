package guru.springframework.spring6resttemplate.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BeerClientImplTest {

    @Autowired
    BeerClientImpl beerClient;

    @Test
    void listBeersNoName() {
        beerClient.listBeers();
    }

    @Test
    void listBeers() {
        beerClient.listBeers("ALE", null, true, 2, 30);
    }
}