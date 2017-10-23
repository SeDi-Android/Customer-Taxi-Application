package ru.sedi.customerclient.ServerManager;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import ru.sedi.customerclient.interfaces.IHttpClientFactory;

/**
 * Created by Marchenko Roman on 23.08.2016.
 */
public class HttpClientFactory {
    private static IHttpClientFactory mFactoryInstance = new IHttpClientFactory() {
        public HttpClient createHttpClient() {
            DefaultHttpClient client = new DefaultHttpClient();
            client.getParams().setParameter("http.useragent", "osmdroid");
            return client;
        }
    };

    public HttpClientFactory() {
    }

    public static void setFactoryInstance(IHttpClientFactory aHttpClientFactory) {
        mFactoryInstance = aHttpClientFactory;
    }

    public static HttpClient createHttpClient() {
        return mFactoryInstance.createHttpClient();
    }
}
