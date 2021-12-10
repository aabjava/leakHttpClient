package ciab

import groovy.transform.CompileStatic

@CompileStatic
interface ResultProvider2 {

    RemoteApiInfoOut getResults(RemoteApiInfoIn remoteApiInfoIn)
}
