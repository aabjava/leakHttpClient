package ciab

import groovy.transform.CompileStatic
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.DefaultHttpClient
import io.micronaut.http.client.HttpClient


@CompileStatic
class ResultNoLeakService implements ResultProvider2 {

    static String url = "https://www.google.com"
    HttpClient httpClient = new DefaultHttpClient(url.toURL())

    @Override
    RemoteApiInfoOut getResults(RemoteApiInfoIn apiInfoIn) {

        HttpRequest request = HttpRequest.GET("/")

        String data = httpClient.toBlocking().exchange(request, String).body()
        RemoteApiInfoOut remoteData = new RemoteApiInfoOut(response: data, id: apiInfoIn.getId())
        remoteData.setId(apiInfoIn.getId())

        return remoteData
    }

}

