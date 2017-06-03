package com.data.analyser;

import com.data.analyser.component.ReviewsProducer;
import com.data.analyser.component.ReviewsConsumer;
import com.data.analyser.component.TranslateReviewsManager;
import com.data.analyser.dto.Review;
import com.data.analyser.dto.User;
import com.data.analyser.dto.ValueCountPair;
import com.data.analyser.utils.FrequentUtil;
import com.data.analyser.mock.HttpAsyncClientMock;
import com.data.analyser.component.ThrottlingHttpRequestsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ReviewAnalyserMain {

    private static final Logger log = LoggerFactory.getLogger(ReviewAnalyserMain.class);


    public static void main(String [] args) {
        boolean shouldTranslate = false;
        if (args.length < 1){
            log.error("file name should be specified as first argument");
            System.exit(-1);
        }
        String fileName = args[0];
        if (args.length > 1) {
            String shouldTranslateArg = args[1];
            if (shouldTranslateArg.equals(Constants.SHOULD_TRANSLATE)) {
                shouldTranslate = true;
            }
        }
        final int numberOfConsumers = Runtime.getRuntime().availableProcessors();

        final ExecutorService readExecutor = Executors.newSingleThreadExecutor();
        final ExecutorService processingExecutors = Executors.newFixedThreadPool(numberOfConsumers);


        final int blockingQueueCapacity = 14000;
        final int maxConcurrentHttpCalls = 100;

        final BlockingQueue<String> queue = new ArrayBlockingQueue<>(blockingQueueCapacity);
        final ConcurrentHashMap<String, AtomicInteger> productCount = new ConcurrentHashMap<>();
        final ConcurrentHashMap<User, AtomicInteger> userCount = new ConcurrentHashMap<>();
        final ConcurrentHashMap<String, AtomicInteger> wordsCount = new ConcurrentHashMap<>();
        final ConcurrentHashMap<Review.ReviewId, Boolean> uniqueVerifierSet = new ConcurrentHashMap<>();

        final HttpAsyncClientMock asyncClientMock = new HttpAsyncClientMock(maxConcurrentHttpCalls);
        final ThrottlingHttpRequestsManager httpManager = new ThrottlingHttpRequestsManager(maxConcurrentHttpCalls, asyncClientMock);

        final ReviewsProducer producer = new ReviewsProducer(fileName, queue, numberOfConsumers);
        final Future<Boolean> producerFuture = readExecutor.submit(producer);

        List<Future<Boolean>> consumersFuture = new ArrayList<>();
        for (int i=0; i<numberOfConsumers; i++) {
            consumersFuture.add(
                    submitConsumer(processingExecutors ,queue,
                            productCount, userCount, wordsCount,
                            uniqueVerifierSet, httpManager, shouldTranslate));
        }

        waitForFinish(producerFuture);
        log.info("Finish reading CSV file");
        consumersFuture.forEach(f -> waitForFinish(f));
        log.info("Finish manipulating all data, product count: {}, users : {}, words: {}",
                productCount.mappingCount(), userCount.mappingCount(), wordsCount.mappingCount());

        final Future<List<String>> productResults = processingExecutors
                .submit(() -> getTop1000FrequentStringsSorted(productCount, x->x));
        final Future<List<String>> wordResults = processingExecutors
                .submit(() -> getTop1000FrequentStringsSorted(wordsCount, x->x));
        final Future<List<String>> userResults = processingExecutors
                .submit(() -> getTop1000FrequentStringsSorted(userCount, User::getProfileName));

        System.out.println("1000 most active users:");
        printResults(userResults);
        System.out.println("1000 most commented food items:");
        printResults(productResults);
        System.out.println("1000 most used words in the reviews:");
        printResults(wordResults);
        readExecutor.shutdown();
        processingExecutors.shutdown();
        asyncClientMock.shutdown();
    }

    private static void waitForFinish(Future<Boolean> isFinishedSuccessfully) {
        try {
            final Boolean readFinished = isFinishedSuccessfully.get();
            if (readFinished == false){
                log.warn("Executing did not finished successfully");
                System.exit(-1);
            }
        } catch (InterruptedException e) {
            log.error("InterruptedException during trying to process to finish, e");
        } catch (ExecutionException e) {
            log.error("ExecutionException during trying to process to finish, e");
        }
    }

    private static Future<Boolean> submitConsumer(ExecutorService executors, BlockingQueue<String> queue,
                                                  ConcurrentHashMap<String, AtomicInteger> productCount,
                                                  ConcurrentHashMap<User, AtomicInteger> userCount,
                                                  ConcurrentHashMap<String, AtomicInteger> wordsCount,
                                                  ConcurrentHashMap<Review.ReviewId, Boolean> uniqueVerifierSet,
                                                  ThrottlingHttpRequestsManager requestHandler, boolean shouldTranslate) {
        final ReviewsConsumer analyser = new ReviewsConsumer(queue, uniqueVerifierSet, productCount, userCount, wordsCount,
                new TranslateReviewsManager(requestHandler), shouldTranslate);
        Future<Boolean> analyseFuture = executors.submit(analyser);
        return analyseFuture;
    }


    private static  <T> List<String> getTop1000FrequentStringsSorted(ConcurrentHashMap<T, AtomicInteger> productCount, Function<T,String> getKeyValue) {
        final Stream<ValueCountPair> pairStream = productCount.entrySet().stream()
                .map(entry -> new ValueCountPair(getKeyValue.apply(entry.getKey()), entry.getValue().get()));
        final List<String> top1000Products = FrequentUtil.topKFrequent(pairStream, 1000);
        Collections.sort(top1000Products);
        return top1000Products;
    }

    private static void printResults(Future<List<String>> results) {
        try {
            final List<String> resultList = results.get();
            System.out.println(resultList.stream().collect(Collectors.joining("\t")));
        } catch (InterruptedException e) {
            log.error("InterruptedException during trying to process to finish, e");
        } catch (ExecutionException e) {
            log.error("ExecutionException during trying to process to finish, e");
        }
    }
}