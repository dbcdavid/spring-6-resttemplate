package guru.springframework.spring6resttemplate.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6resttemplate.config.RestTemplateBuilderConfig;
import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerDTOPageImpl;
import guru.springframework.spring6resttemplate.model.BeerStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest
@Import(RestTemplateBuilderConfig.class)
public class BeerClientMockTest {

    private static final String URL = "http://localhost:8080";

    BeerClient beerClient;

    MockRestServiceServer server;

    @Autowired
    RestTemplateBuilder restTemplateBuilderConfigured;

    @Autowired
    ObjectMapper objectMapper;

    @Mock
    RestTemplateBuilder mockRestTemplateBuilder = new RestTemplateBuilder(new MockServerRestTemplateCustomizer());

    BeerDTO dto;

    String dtoJson;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        RestTemplate restTemplate = restTemplateBuilderConfigured.build();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        when(mockRestTemplateBuilder.build()).thenReturn(restTemplate);
        beerClient = new BeerClientImpl(mockRestTemplateBuilder);

        dto = getBeerDTO();
        dtoJson = objectMapper.writeValueAsString(dto);
    }

    @Test
    void testDeleteBeer(){
        server.expect(method(HttpMethod.DELETE))
                .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH, dto.getId()))
                .andRespond(withNoContent());

        beerClient.deleteBeer(dto.getId());
        server.verify();
    }

    @Test
    void testDeleteBeerNotFound(){
        server.expect(method(HttpMethod.DELETE))
                .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH, dto.getId()))
                .andRespond(withResourceNotFound());

        assertThrows(HttpClientErrorException.class, () -> {
            beerClient.deleteBeer(dto.getId());
        });

        server.verify();
    }

    @Test
    void testUpdateBeer(){
        server.expect(method(HttpMethod.PUT))
                .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH, dto.getId()))
                .andRespond(withNoContent());

        mockGetOperation();

        BeerDTO returned = beerClient.updateBeer(dto);
        assertThat(returned.getId()).isEqualTo(dto.getId());
    }

    @Test
    void testCreateBeer(){
        URI uri = UriComponentsBuilder.fromPath(BeerClientImpl.GET_BEER_BY_ID_PATH).build(dto.getId());

        server.expect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
                .andExpect(requestTo(URL + BeerClientImpl.GET_BEER_PATH))
                .andRespond(withAccepted().location(uri));

        mockGetOperation();

        BeerDTO returned = beerClient.createBeer(dto);
        assertThat(returned.getId()).isEqualTo(dto.getId());
    }

    @Test
    void testGetBeerById() {
        mockGetOperation();

        BeerDTO returned = beerClient.getBeerById(dto.getId());
        assertThat(returned.getId()).isEqualTo(dto.getId());
    }

    @Test
    void testListBeersWithQueryParam() throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(getPage());
        URI uri = UriComponentsBuilder.fromHttpUrl(URL + BeerClientImpl.GET_BEER_PATH)
                .queryParam("beerName", "ALE")
                .build().toUri();

        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(uri))
                .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
                .andExpect(queryParam("beerName", "ALE"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        Page<BeerDTO> dtos = beerClient.listBeers("ALE", null, null, null, null);
        assertThat(dtos.getContent().size()).isEqualTo(1);
    }

    @Test
    void testListBeers() throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(getPage());

        server.expect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
                .andExpect(requestTo(URL + BeerClientImpl.GET_BEER_PATH))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        Page<BeerDTO> dtos = beerClient.listBeers();
        assertThat(dtos.getContent().size()).isGreaterThan(0);
    }

    BeerDTO getBeerDTO() {
        return BeerDTO.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("19.90"))
                .beerName("Mango Bobs")
                .beerStyle(BeerStyle.ALE)
                .quantityOnHand(200)
                .upc("123456")
                .build();
    }

    BeerDTOPageImpl getPage(){
        return new BeerDTOPageImpl(Arrays.asList(getBeerDTO()), 1, 25, 1);
    }

    private void mockGetOperation() {
        server.expect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH, dto.getId()))
                .andRespond(withSuccess(dtoJson, MediaType.APPLICATION_JSON));
    }
}
