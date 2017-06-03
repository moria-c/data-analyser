package com.data.analyser.utils;

import com.data.analyser.dto.ValueCountPair;

import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This util used to find top k frequent values from Stream contains Value Count Pairs
 */
public class FrequentUtil {

    public static List<String> topKFrequent(Stream<ValueCountPair> valueCountPairStream, int k) {
        PriorityQueue<ValueCountPair> queue = createMinHeap();
        maintainHeapOfSizeK(valueCountPairStream, k, queue);
        List<String> topKFrequent = queue.stream().map(x -> x.getValue())
                .collect(Collectors.toList());
        return topKFrequent;
    }

    private static void maintainHeapOfSizeK(Stream<ValueCountPair> keyCount, int k,
                                            PriorityQueue<ValueCountPair> queue) {
        keyCount.forEach(pair -> {
            queue.offer(pair);
            if (queue.size() > k) {
                queue.poll();
            }
        });
    }

    private static PriorityQueue<ValueCountPair> createMinHeap() {
        return new PriorityQueue<>((a, b) -> a.getCount() - b.getCount());
    }
}