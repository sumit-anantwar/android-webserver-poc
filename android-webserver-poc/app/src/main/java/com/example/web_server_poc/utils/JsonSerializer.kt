package com.example.web_server_poc.utils

import kotlinx.serialization.json.Json


val JsonSerializer: Json by lazy {
    Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
}