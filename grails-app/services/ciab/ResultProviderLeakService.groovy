package ciab

import groovy.transform.CompileStatic
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.DefaultHttpClient
import io.micronaut.http.client.HttpClient


@CompileStatic
class ResultProviderLeakService implements ResultProvider {

    static String url = "https://www.google.com"

    @Override
    RemoteApiInfoOut getResults(RemoteApiInfoIn apiInfoIn) {
        HttpClient httpClient = new DefaultHttpClient(url.toURL())
        HttpRequest request = HttpRequest.GET("/")

        String data = httpClient.toBlocking().exchange(request, String).body()
        RemoteApiInfoOut remoteData = new RemoteApiInfoOut(response: data, id: apiInfoIn.getId())
        remoteData.setId(apiInfoIn.getId())

        return remoteData
    }

}

@CompileStatic
class RemoteApiInfoIn {
    static int instanceNumber = 1
    int id
    String data

    RemoteApiInfoIn(String data) {
        this.id = instanceNumber
        this.data = data
        instanceNumber++
    }
}

@CompileStatic
class RemoteApiInfoOut {

    int id
    boolean error = false
    String response
    double timeInSeconds
    double memoryUsedInMb

    String toString() {
        return id
    }
}
