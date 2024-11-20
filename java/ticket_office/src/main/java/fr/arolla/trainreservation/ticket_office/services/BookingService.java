package fr.arolla.trainreservation.ticket_office.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.arolla.trainreservation.ticket_office.Seat;
import fr.arolla.trainreservation.ticket_office.controllers.BookingRequest;
import fr.arolla.trainreservation.ticket_office.controllers.BookingResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class BookingService {

  private final RestClient restClient;
  private static final String GET_BOOKING_REFERENCE_URL = "http://127.0.0.1:8082/booking_reference";
  private static final String BOOK_TRAIN_URL = "http://127.0.0.1:8081/reserve";
  private static final String GET_DATA_FROM_TRAIN_URL = "http://127.0.0.1:8081/data_for_train/";


  public BookingService() {
    this.restClient = new WebRestClient();
  }

  public BookingService(RestClient restClient) {
    this.restClient = restClient;
  }

  public BookingResponse getBookingResponse(BookingRequest bookingRequest) {
    var seatCount = bookingRequest.count();
    var trainId = bookingRequest.train_id();

    // Step 1: Get a booking reference
    var bookingReference = restClient.getBookingReference(GET_BOOKING_REFERENCE_URL);

    // Step 2: Retrieve train data for the given train ID
    ArrayList<Seat> seats = getTrainDataFromID(trainId);

    // Step 3: find available seats (hard-code coach 'A' for now)
    var availableSeats = seats.stream().filter(seat -> seat.coach().equals("A") && seat.bookingReference() == null);

    // Step 4: call the '/reserve' end point
    var ids = bookTrain(availableSeats, seatCount, trainId, bookingReference);

    // Step 5: return reference and booked seats
    return new BookingResponse(trainId, bookingReference, ids);
  }

  private List<String> bookTrain(Stream<Seat> availableSeats, int seatCount, String trainId, String bookingReference) {
    var toReserve = availableSeats.limit(seatCount);
    var ids = toReserve.map(seat -> seat.number() + seat.coach()).toList();

    Map<String, Object> payload = new HashMap<>();
    payload.put("train_id", trainId);
    payload.put("seats", ids);
    payload.put("booking_reference", bookingReference);
    restClient.bookTrain(BOOK_TRAIN_URL, payload);
    return ids;
  }

  private ArrayList<Seat> getTrainDataFromID(String trainId) {
    var json = restClient.getDateFromTrain(GET_DATA_FROM_TRAIN_URL + trainId);
    ObjectMapper objectMapper = new ObjectMapper();
    ArrayList<Seat> seats = new ArrayList<>();
    try {
      var tree = objectMapper.readTree(json);
      var seatsNode = tree.get("seats");
      for (JsonNode node : seatsNode) {
        String coach = node.get("coach").asText();
        String seatNumber = node.get("seat_number").asText();
        var jsonBookingReference = node.get("booking_reference").asText();
        if (jsonBookingReference.isEmpty()) {
          var seat = new Seat(seatNumber, coach, null);
          seats.add(seat);
        } else {
          var seat = new Seat(seatNumber, coach, jsonBookingReference);
          seats.add(seat);
        }
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return seats;
  }
}
