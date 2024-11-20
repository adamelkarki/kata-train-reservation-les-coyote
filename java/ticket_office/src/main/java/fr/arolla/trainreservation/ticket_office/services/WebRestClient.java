package fr.arolla.trainreservation.ticket_office.services;

import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class WebRestClient implements RestClient{

  private final RestTemplate restTemplate;

  public WebRestClient(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public WebRestClient() {
    this.restTemplate = new RestTemplate();
  }

  @Override
  public String getBookingReference(String url) {
    return restTemplate.getForObject(url, String.class);
  }

  @Override
  public String bookTrain(String url, Map<String, Object> payload) {
    return restTemplate.postForObject(url, payload, String.class);
  }

  @Override
  public String getDateFromTrain(String url) {
    return restTemplate.getForObject(url, String.class);
  }
}
