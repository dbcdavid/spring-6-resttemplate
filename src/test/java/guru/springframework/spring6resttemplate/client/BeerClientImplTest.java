package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerStyle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class BeerClientImplTest {

    @Autowired
    BeerClientImpl beerClient;

    @Test
    void deleteBeer(){
        BeerDTO newDTO = BeerDTO.builder()
                .upc("12345")
                .quantityOnHand(500)
                .price(new BigDecimal(20.50))
                .beerStyle(BeerStyle.STOUT)
                .beerName("Mango Bobs 2")
                .build();

        BeerDTO savedDTO = beerClient.createBeer(newDTO);

        assertNotNull(savedDTO);

        beerClient.deleteBeer(savedDTO.getId());
        assertThrows(HttpClientErrorException.class, () -> {
           beerClient.getBeerById(savedDTO.getId());
        });
    }

    @Test
    void updateBeer(){
        BeerDTO newDTO = BeerDTO.builder()
                .upc("12345")
                .quantityOnHand(500)
                .price(new BigDecimal(20.50))
                .beerStyle(BeerStyle.STOUT)
                .beerName("Mango Bobs 2")
                .build();

        BeerDTO savedDTO = beerClient.createBeer(newDTO);

        final String newName = "Mango Bobs 3";
        savedDTO.setBeerName(newName);
        BeerDTO updatedDTO = beerClient.updateBeer(savedDTO);

        assertEquals(newName, updatedDTO.getBeerName());
    }

    @Test
    void createBeer(){
        BeerDTO newDTO = BeerDTO.builder()
                .upc("123456")
                .quantityOnHand(500)
                .price(new BigDecimal(20.50))
                .beerStyle(BeerStyle.STOUT)
                .beerName("Mango Bobs")
                .build();

        BeerDTO savedDTO = beerClient.createBeer(newDTO);

        assertNotNull(savedDTO);
    }

    @Test
    void getBeerById(){
        Page<BeerDTO> beerDTOs = beerClient.listBeers();
        BeerDTO dto = beerDTOs.getContent().get(0);
        BeerDTO byId = beerClient.getBeerById(dto.getId());

        assertNotNull(byId);
    }

    @Test
    void listBeersNoName() {
        beerClient.listBeers();
    }

    @Test
    void listBeers() {
        beerClient.listBeers("ALE", null, true, 2, 30);
    }
}