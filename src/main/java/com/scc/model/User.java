package com.scc.model;

public record User(int u_id, String name, String email, String password, String roles, String status) {
    @Override
    public String toString(){
        return String.format("[%d] %s (%s) - Status: %s", u_id, name, roles, status );

    }
}