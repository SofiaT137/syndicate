package com.task10.service;

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

    public Map<String, Object> get(Long itemId) {
        return tablesRepository.get(itemId);
    }

    public Map<String, Object> create(String body) {
        return tablesRepository.create(body);
    }
}
