package com.task10.repository;

import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.google.gson.Gson;
import com.task10.dto.TableDTO;
import com.task10.utils.DynamoDBClient;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TablesRepository implements Repository {

    private static final String TABLE_NAME = "Tables";

    private final Table dynamoDBTable;

    public TablesRepository() {
        var dynamoDBClient = DynamoDBClient.getInstance();
        this.dynamoDBTable = dynamoDBClient.getTable(TABLE_NAME);
    }

    public Map<String, Object> get(Long itemId) {
       return dynamoDBTable.getItem("id", itemId).asMap();
    }

    @Override
    public Map<String, Object> getAll() {
        var items = dynamoDBTable.scan();
        var result = StreamSupport.stream(items.spliterator(), false)
                .map(Item::asMap)
                .collect(Collectors.toList());

        return Map.of("tables", result);
    }

    @Override
    public Map<String, Object> create(String body) {
        var table = new Gson().fromJson(body, TableDTO.class);
        var item = new Item();
        item.withInt("id", table.getId());
        item.withInt("number", table.getNumber());
        item.withInt("places", table.getPlaces());
        item.withBoolean("isVip", table.getVip());
        item.withInt("minOrder", table.getMinOrder());
        dynamoDBTable.putItem(item);
        return Map.of("id", table.getId());
    }
}
