package com.task10.repository;

import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.task10.dto.TableDTO;
import com.task10.utils.DynamoDBClient;
import com.task10.utils.NameHolder;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TablesRepository implements Repository<TableDTO> {

    private final NameHolder nameHolder;

    private final Table dynamoDBTable;

    public TablesRepository() {
        nameHolder = NameHolder.getInstance();
        var dynamoDBClient = DynamoDBClient.getInstance();
        var table = dynamoDBClient.getTable(nameHolder.getTablesName());
        System.out.println("I found the table " + table.getTableName());
        dynamoDBTable = table;
    }

    public Map<String, Object> get(int itemId) {
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
    public Map<String, Object> create(TableDTO table) {
        System.out.println("The table name is " + nameHolder.getTablesName() + "!!!!!");
        var item = new Item();
        item.withInt("id", table.getId());
        item.withInt("number", table.getNumber());
        item.withInt("places", table.getPlaces());
        item.withBoolean("isVip", table.getVip());
        item.withInt("minOrder", table.getMinOrder());
        var result = dynamoDBTable.putItem(item);
        System.out.println("Result: " + result);
        return Map.of("id", table.getId());
    }
}
