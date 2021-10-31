package com.horacehylee.matching_engine.domain;

public enum Side {
    BID('B'),
    ASK('A');

    private final char code;

    Side(char code) {
        this.code = code;
    }

    public static Side of(char code) {
        switch (code) {
            case 'B':
                return BID;
            case 'A':
                return ASK;
            default:
                throw new IllegalArgumentException("Unexpected side: " + code);
        }
    }

    public char getCode() {
        return code;
    }

    public Side getOpposite() {
        return this == BID ? ASK : BID;
    }
}
