package com.kss.astrologer.types;

public enum MonthName {
    JAN("Jan"), FEB("Feb"), MAR("Mar"), APR("Apr"), MAY("May"), JUN("Jun"),
    JUL("Jul"), AUG("Aug"), SEP("Sep"), OCT("Oct"), NOV("Nov"), DEC("Dec");

    private final String label;

    MonthName(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static String fromInt(int monthValue) {
        return values()[monthValue - 1].label;
    }
}
