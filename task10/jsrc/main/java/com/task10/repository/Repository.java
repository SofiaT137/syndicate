package com.task10.repository;

import java.util.Map;

public interface Repository {
    Map<String, Object> getAll();
    Map<String, Object> create(String body);
}
