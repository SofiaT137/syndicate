package com.task10.repository;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.google.gson.Gson;
import com.task10.dto.ReservationDTO;
import com.task10.utils.DynamoDBClient;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ReservationRepository implements Repository<ReservationDTO> {

    private static final String TABLE_NAME = "Reservations";
    private static final String RESERVATIONS = "reservations";
    private static final String ERROR_MESSAGE = "The time is occupied";

    private final Table dynamoDBTable;

    public ReservationRepository() {
        var dynamoDBClient = DynamoDBClient.getInstance();
        this.dynamoDBTable = dynamoDBClient.getTable(TABLE_NAME);
    }

    @Override
    public Map<String, Object> getAll() {
        var items = dynamoDBTable.scan();
        var result = StreamSupport.stream(items.spliterator(), false)
                .map(Item::asMap)
                .collect(Collectors.toList());

        return Map.of(RESERVATIONS, result);
    }

    @Override
    public Map<String, Object> create(ReservationDTO reservation) {
        var id = getUUID();
        ensureNoTimeConflict(reservation);
        var item = new Item();
        item.withString("id", id.toString());
        item.withInt("tableNumber", reservation.getTableNumber());
        item.withString("clientName", reservation.getClientName());
        item.withString("phoneNumber", reservation.getPhoneNumber());
        item.withString("date", reservation.getDate());
        item.withString("slotTimeStart", reservation.getSlotTimeStart());
        item.withString("slotTimeEnd", reservation.getSlotTimeEnd());
        dynamoDBTable.putItem(item);
        return item.asMap();
    }

    private UUID getUUID() {
        return UUID.randomUUID();
    }

    private void ensureNoTimeConflict(ReservationDTO reservation) {
        var startReservation = LocalTime.parse(reservation.getSlotTimeStart());
        var endReservation = LocalTime.parse(reservation.getSlotTimeEnd());
        var reservations = (ArrayList <Map<String, Object>>) getAll().get(RESERVATIONS);
        reservations.stream()
                .filter(reserv -> (Integer) reserv.get("tableNumber") == reservation.getTableNumber())
                .filter(reserv -> reserv.get("date").equals(reservation.getDate()))
                .forEach(reserv -> {
                    var startSlot = LocalTime.parse(reserv.get("slotTimeStart").toString());
                    var endSlot = LocalTime.parse(reserv.get("slotTimeEnd").toString());
                    boolean isOverlapping = endReservation.isAfter(startSlot) && startReservation.isBefore(endSlot);
                    if (isOverlapping) {
                        throw new RuntimeException(ERROR_MESSAGE);
                    }
                });
    }
}
