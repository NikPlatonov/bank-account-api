package dev.platonov.bank.accountapi.models;

public enum DenyReason {
    EMPTY_RESERVE(1000),
    NOT_ENOUGH_MONEY(1001),
    ;

    public final int code;

    DenyReason(int code) {
        this.code = code;
    }
}
