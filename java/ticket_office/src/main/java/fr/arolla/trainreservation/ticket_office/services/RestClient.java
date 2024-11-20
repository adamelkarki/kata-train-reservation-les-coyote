package fr.arolla.trainreservation.ticket_office.services;

import java.util.Map;

public interface RestClient {
  public String getBookingReference(String url);
  public String bookTrain(String url, Map<String, Object> payload);
  public String getDateFromTrain(String url);
}
