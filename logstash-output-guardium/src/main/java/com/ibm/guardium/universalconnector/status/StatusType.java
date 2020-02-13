package com.ibm.guardium.universalconnector.status;

enum StatusType
{
    BLUE(0, "BLUE"),
    GREEN(1, "GREEN"),
    YELLOW(2, "YELLOW"),
    RED(3, "RED");

    private final int type;
    private final String name;

    StatusType(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return name;
    }

    public Boolean isSame(String name){
        return this.toString().equals(name);
    }
}
