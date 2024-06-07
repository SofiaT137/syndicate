package com.task10.service;

import com.google.gson.Gson;
import com.task10.dto.TableDTO;
import com.task10.repository.TablesRepository;
import java.util.Map;

public class TablesService {

    private final TablesRepository tablesRepository;

    public TablesService(TablesRepository tablesRepository) {
        this.tablesRepository = tablesRepository;
    }

    public Map<String, Object> getAll() {
        return tablesRepository.getAll();
    }

    public Map<String, Object> get(int itemId) {
        return tablesRepository.get(itemId);
    }

    public Map<String, Object> create(String body) {
        var table = new Gson().fromJson(body, TableDTO.class);
        return tablesRepository.create(table);
    }
}
