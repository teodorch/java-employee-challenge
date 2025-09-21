package com.reliaquest.api.client.api;

public record Response<Entity>(Entity data, String status) {

}
