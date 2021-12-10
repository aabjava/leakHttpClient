package ciab

import groovy.transform.CompileStatic

@CompileStatic
interface ResultProvider {

    RemoteApiInfoOut getResults(RemoteApiInfoIn remoteApiInfoIn)
}
