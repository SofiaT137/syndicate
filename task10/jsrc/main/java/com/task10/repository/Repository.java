package com.task10.repository;

import java.util.Map;

public interface Repository<T> {
    Map<String, Object> getAll();
    Map<String, Object> create(T dto);
}
