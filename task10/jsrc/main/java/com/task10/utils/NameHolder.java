package com.task10.utils;

public final class NameHolder {

    private static final NameHolder INSTANCE = createInstance();

    private String tablesName;
    private String reservationsName;

    private NameHolder() {
    }

    public static NameHolder getInstance() {
        return INSTANCE;
    }

    private static NameHolder createInstance() {
        return new NameHolder();
    }

    public void setTablesName(String tablesName) {
        this.tablesName = tablesName;
    }

    public void setReservationsName(String reservationsName) {
        this.reservationsName = reservationsName;
    }

    public String getTablesName() {
        return tablesName;
    }

    public String getReservationsName() {
        return reservationsName;
    }
}
