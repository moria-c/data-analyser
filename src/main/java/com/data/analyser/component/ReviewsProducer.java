package com.data.analyser.component;

import com.data.analyser.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * This class read from csv file and push each line to BlockingQueue
 * When it finish to read the file it notify all consumer by pushing POISON_PILL messages for all consumers
 */
public class ReviewsProducer implements Callable<Boolean> {

    private static final Logger log = LoggerFactory.getLogger(ReviewsProducer.class);

    private String fileName;
    private BlockingQueue<String> queue;
    private int numberOfConsumers;

    public ReviewsProducer(String fileName, BlockingQueue<String> queue, int numberOfConsumers) {
        super();
        this.fileName = fileName;
        this.queue = queue;
        this.numberOfConsumers = numberOfConsumers;
    }

    @Override
    public Boolean call() throws Exception {
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            final String firstLine = br.readLine();
            if (firstLine == null){
                return true;
            }
            if (!firstLine.startsWith("Id")){
                queue.put(firstLine);
            }
            while ((line = br.readLine()) != null) {
                queue.put(line);
            }
            for (int i=0; i<numberOfConsumers; i++){
                queue.put(Constants.POISON_PILL);
            }

        } catch (IOException e) {
            log.error("Exception while trying to read from file", e);
            return false;
        }
        return true;
    }
}
