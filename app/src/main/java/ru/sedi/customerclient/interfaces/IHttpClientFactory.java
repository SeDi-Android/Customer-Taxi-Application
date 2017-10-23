package ru.sedi.customerclient.interfaces;

import org.apache.http.client.HttpClient;


public interface IHttpClientFactory {
    HttpClient createHttpClient();
}

