package com.data.analyser.component;

import com.data.analyser.Constants;
import com.data.analyser.dto.Review;
import com.data.analyser.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ReviewsConsumer implements Callable<Boolean> {

    private static final Logger log = LoggerFactory.getLogger(ReviewsConsumer.class);

    private BlockingQueue<String> queue;
    private final ConcurrentHashMap<Review.ReviewId, Boolean> uniqueVerifierSet;
    private ConcurrentHashMap<String, AtomicInteger> productCount;
    private ConcurrentHashMap<User, AtomicInteger> userCount;
    private ConcurrentHashMap<String, AtomicInteger> wordsCount;
    private TranslateReviewsManager translateReviewsManager;
    private final boolean shouldTranslate;

    String reviewTextSplitBy = "([^a-zA-Z]+)";

    public ReviewsConsumer(BlockingQueue<String> queue,
                           ConcurrentHashMap<Review.ReviewId, Boolean> uniqueVerifierSet,
                           ConcurrentHashMap<String, AtomicInteger> productCount,
                           ConcurrentHashMap<User, AtomicInteger> userCount,
                           ConcurrentHashMap<String, AtomicInteger> wordsCount,
                           TranslateReviewsManager translateReviewsManager,
                           boolean shouldTranslate) {
        this.queue = queue;
        this.uniqueVerifierSet = uniqueVerifierSet;
        this.productCount = productCount;
        this.userCount = userCount;
        this.wordsCount = wordsCount;
        this.translateReviewsManager = translateReviewsManager;
        this.shouldTranslate = shouldTranslate;
    }

    @Override
    public Boolean call() {
        try {
            while (true) {
                final String nextLine = queue.take();
                if (nextLine.equals(Constants.POISON_PILL)) {
                    if (shouldTranslate) {
                        translateReviewsManager.submitLastRequest();
                    }
                    return true;
                } else {
                    analyseLine(nextLine);
                }
            }
        } catch (Exception ex){
            log.error("Exception during analysing review", ex);
            return false;
        }
    }

    private void analyseLine(String nextLine) {
        final Review review = new Review(nextLine);
        if (isDuplicateReview(review)) {
            return;
        }
        final User user = new User(review.getUserId(), review.getUserProfileName());
        final String[] wordsOfReview = review.getText().split(reviewTextSplitBy);

        addCount(review.getProductId(), productCount, 1);
        addCount(user, userCount, 1);
        //todo not count words like a as is.. etc
        Arrays.stream(wordsOfReview).forEach(word -> addCount(word, wordsCount, 1));
        if (shouldTranslate) {
            translateReviewsManager.translate(review);
        }

    }

    private boolean isDuplicateReview(Review review) {
        //review counted as duplicate if same user review same product twice
        final Boolean previousValue = uniqueVerifierSet.putIfAbsent(review.getReviewId(), true);
        return previousValue != null;
    }

    private <T> void addCount(T key, ConcurrentMap<T, AtomicInteger> concurrentMap, int count) {
        final AtomicInteger countToAdd = new AtomicInteger(count);
        final AtomicInteger actualValue = concurrentMap.putIfAbsent(key, countToAdd);
        if (actualValue != null){
            actualValue.addAndGet(count);
        }
    }
}