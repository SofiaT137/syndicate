package com.task10.service;

import com.google.gson.Gson;
import com.task10.dto.ReservationDTO;
import com.task10.repository.ReservationRepository;
import com.task10.repository.TablesRepository;
import java.util.Map;

public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TablesRepository tablesRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              TablesRepository tablesRepository) {
        this.reservationRepository = reservationRepository;
        this.tablesRepository = tablesRepository;
    }

    public Map<String, Object> getAll() {
        return reservationRepository.getAll();
    }

    public Map<String, Object> create(String body) {
        var reservation = new Gson().fromJson(body, ReservationDTO.class);
        checkIfTableExists(reservation.getTableNumber());
        return reservationRepository.create(reservation);
    }

    private void checkIfTableExists(int tableNumber) {
        var table = tablesRepository.get(tableNumber);
        if (table.isEmpty()) {
            throw new RuntimeException("Table " + tableNumber + " does not exist");
        }
    }
}
