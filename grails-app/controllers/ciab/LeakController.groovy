package ciab


import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.core.util.StopWatch
import org.springframework.beans.factory.annotation.Autowired

import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

@CompileStatic
@Slf4j
class LeakController {

    int numberResults = 1000
    static final int mb = 1024 * 1024
    static currentRun = 1

    static Runtime runtime = Runtime.getRuntime()

    @Autowired
    ResultProvider results

    @Autowired
    ResultProvider2 results2

    private void initResults() {
        results.each { ResultProvider service ->
            RemoteApiInfoIn newInfo = new RemoteApiInfoIn("INIT SERVICE")
            service.getResults(newInfo)
        }
    }

    private void initResults2() {
        results2.each { ResultProvider2 service ->
            RemoteApiInfoIn newInfo = new RemoteApiInfoIn("INIT SERVICE")
            service.getResults(newInfo)
        }
    }

    boolean shouldLeak() {
        StopWatch globalStopWatch = new StopWatch("Total")
        globalStopWatch.start()
        initResults()
        System.gc()
        double startedRam = getCurrentHeap()
        log.debug("Should leak current HEAP = ${startedRam}")

        List<CompletableFuture<RemoteApiInfoOut>> resultsData = new ArrayList<>()

        for (int i in 1..numberResults) {
            results.each { ResultProvider resultsApi ->
                resultsData.add(CompletableFuture.supplyAsync(new Supplier<RemoteApiInfoOut>() {

                    @Override
                    RemoteApiInfoOut get() {


                        RemoteApiInfoIn newInfo = new RemoteApiInfoIn("soma data now")
                        StopWatch stopWatch = new StopWatch(newInfo.id.toString())
                        stopWatch.start("ApiCall")

                        RemoteApiInfoOut realResult = resultsApi.getResults(newInfo)
                        stopWatch.stop()
                        printStats(stopWatch, realResult.id)
                        realResult.setTimeInSeconds(stopWatch.getTotalTimeSeconds())
                        realResult.setMemoryUsedInMb(getCurrentHeap())

                        return realResult
                    }
                }).exceptionally(new Function<Throwable, RemoteApiInfoOut>() {
                    @Override
                    RemoteApiInfoOut apply(Throwable throwable) {
                        log.error("Error calling apply ${throwable.getLocalizedMessage()}")
                        RemoteApiInfoOut remoteApiInfoOut = new RemoteApiInfoOut(response: "error")

                        return remoteApiInfoOut
                    }
                }))

            }
        }

        resultsData.eachWithIndex { CompletableFuture<RemoteApiInfoOut> completableFuture, int index ->
            completableFuture.thenAccept(new Consumer<RemoteApiInfoOut>() {
                @Override
                void accept(RemoteApiInfoOut insuranceProduct) {
                    if (insuranceProduct == null) {
                        return
                    }
                    System.gc()
                    if (index == numberResults - 1) {
                        globalStopWatch.stop()
                        Thread.sleep(2000)
                        System.gc()
                        double totalRamNotReleased = getCurrentHeap() - startedRam
                        log.debug("Number of calls = ${index + 1} Initial Heap = ${startedRam} Current Heap = ${getCurrentHeap()} Memory leaked = ${totalRamNotReleased} MB , total time = ${globalStopWatch.getTotalTimeSeconds()} sec")
                    }

                }
            })
        }

        render("nothing here to see")
    }

    boolean shouldNotLeak() {
        StopWatch globalStopWatch = new StopWatch("Total")
        globalStopWatch.start()
        initResults2()
        System.gc()
        double startedRam = getCurrentHeap()
        log.debug("Should Not leak current HEAP = ${startedRam}")
        List<CompletableFuture<RemoteApiInfoOut>> resultsData = new ArrayList<>()

        for (int i in 1..numberResults) {
            results2.each { ResultProvider2 resultsApi ->
                resultsData.add(CompletableFuture.supplyAsync(new Supplier<RemoteApiInfoOut>() {

                    @Override
                    RemoteApiInfoOut get() {

                        RemoteApiInfoIn newInfo = new RemoteApiInfoIn("soma data now")
                        StopWatch stopWatch = new StopWatch(newInfo.id.toString())
                        stopWatch.start("ApiCall")

                        RemoteApiInfoOut realResult = resultsApi.getResults(newInfo)
                        stopWatch.stop()
                        printStats(stopWatch, realResult.id)
                        realResult.setTimeInSeconds(stopWatch.getTotalTimeSeconds())
                        realResult.setMemoryUsedInMb(getCurrentHeap())

                        return realResult
                    }
                }).exceptionally(new Function<Throwable, RemoteApiInfoOut>() {
                    @Override
                    RemoteApiInfoOut apply(Throwable throwable) {
                        log.error("Error calling apply ${throwable.getLocalizedMessage()}")
                        RemoteApiInfoOut remoteApiInfoOut = new RemoteApiInfoOut(response: "error")

                        return remoteApiInfoOut
                    }
                }))

            }
        }

        resultsData.eachWithIndex { CompletableFuture<RemoteApiInfoOut> it, int index ->
            it.thenAccept(new Consumer<RemoteApiInfoOut>() {
                @Override
                void accept(RemoteApiInfoOut insuranceProduct) {

                    if (insuranceProduct == null) {
                        return
                    }
                    System.gc()
                    if (index == numberResults - 1) {
                        globalStopWatch.stop()
                        Thread.sleep(2000)
                        System.gc()
                        double totalRamNotReleased = getCurrentHeap() - startedRam
                        log.debug("Number of calls = ${index + 1} Initial Heap = ${startedRam} Current Heap = ${getCurrentHeap()} Memory leaked = ${totalRamNotReleased} MB , total time = ${globalStopWatch.getTotalTimeSeconds()} sec")
                    }


                }
            })
        }

        render("nothing here to see2")
    }

    static double getCurrentHeap() {

        return ((runtime.totalMemory() - runtime.freeMemory()) / mb) as double
    }

    private void printStats(StopWatch stopWatch, int callNumber) {
        println("$callNumber - ${stopWatch.getLastTaskName()} call took = ${stopWatch.getTotalTimeSeconds()} sec, used memory = ${getCurrentHeap()} MB")

    }

}
