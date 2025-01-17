/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit.queue.impl;

import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.boon.core.reflection.ConstructorAccess;
import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.concurrent.ExecutorContext;
import io.advantageous.qbit.queue.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.advantageous.qbit.concurrent.ScheduledExecutorBuilder.scheduledExecutorBuilder;

/**
 * This is the base for all the queues we use.
 * <p>
 * created by Richard on 8/4/14.
 *
 * @param <T> type
 * @author rhightower
 */
public class BasicQueue<T> implements Queue<T> {

    private final BlockingQueue<Object> queue;
    private final boolean checkIfBusy;
    private final int batchSize;
    private final Logger logger = LoggerFactory.getLogger(BasicQueue.class);
    private final ReceiveQueueManager<T> receiveQueueManager;
    private final String name;
    private final int pollTimeWait;
    private final TimeUnit pollTimeTimeUnit;
    private final boolean tryTransfer;
    private final boolean debug = GlobalConstants.DEBUG;
    private final int checkEvery;
    private final AtomicBoolean stop = new AtomicBoolean();
    private final UnableToEnqueueHandler unableToEnqueueHandler;
    private ExecutorContext executorContext;
    private final int enqueueTimeout;
    private final TimeUnit enqueueTimeoutTimeUnit;

    public BasicQueue(final String name,
                      final int waitTime,
                      @SuppressWarnings("SameParameterValue") final TimeUnit timeUnit,
                      final int enqueueTimeout,
                      final TimeUnit enqueueTimeoutTimeUnit,
                      final int batchSize,
                      final Class<? extends BlockingQueue> queueClass,
                      final boolean checkIfBusy,
                      final int size,
                      final int checkEvery,
                      boolean tryTransfer,
                      UnableToEnqueueHandler unableToEnqueueHandler) {

        logger.info("Queue created {} {} batchSize {} size {} checkEvery {} tryTransfer {} pollTimeWait, enqueueTimeout",
                name, queueClass, batchSize, size, checkEvery, tryTransfer, waitTime, enqueueTimeout);


        this.enqueueTimeout = enqueueTimeout;
        this.tryTransfer = tryTransfer;
        this.name = name;
        this.pollTimeWait = waitTime;
        this.pollTimeTimeUnit = timeUnit;
        this.batchSize = batchSize;
        this.enqueueTimeoutTimeUnit = enqueueTimeoutTimeUnit;
        this.unableToEnqueueHandler = unableToEnqueueHandler;

        boolean shouldCheckIfBusy;

        this.receiveQueueManager = new BasicReceiveQueueManager<>();


        if (size == -1) {

            //noinspection unchecked
            this.queue = ClassMeta.classMeta(queueClass).noArgConstructor().create();
        } else {

            final ClassMeta<? extends BlockingQueue> classMeta = ClassMeta.classMeta(queueClass);
            if (queueClass != LinkedTransferQueue.class) {
                if (debug) logger.debug("Not a LinkedTransfer queue");
                final ConstructorAccess<Object> constructor = classMeta.declaredConstructor(int.class);
                //noinspection unchecked
                this.queue = (BlockingQueue<Object>) constructor.create(size);
            } else {
                final ConstructorAccess<? extends BlockingQueue> constructorAccess = classMeta.noArgConstructor();

                //noinspection unchecked
                this.queue = (BlockingQueue<Object>) constructorAccess.create();
            }
        }


        shouldCheckIfBusy = queue instanceof TransferQueue;

        this.checkIfBusy = shouldCheckIfBusy && checkIfBusy;

        this.checkEvery = checkEvery;


        logger.info("Queue done creating {} batchSize {} checkEvery {} tryTransfer {}" +
                        "pollTimeWait/polltime {}, enqueueTimeout {}",
                this.name, this.batchSize, this.checkEvery, this.tryTransfer,
                this.pollTimeWait, this.enqueueTimeout);


    }


    /**
     * This returns a new instance of ReceiveQueue every time you call it
     * so call it only once per thread.
     *
     * @return received queue
     */
    @Override
    public ReceiveQueue<T> receiveQueue() {
        logger.info("ReceiveQueue requested for {}", name);
        return new BasicReceiveQueue<>(queue, pollTimeWait, pollTimeTimeUnit, batchSize);
    }

    /**
     * This returns a new instance of SendQueue every time you call it
     * so call it only once per thread.
     *
     * @return sendQueue.
     */
    @Override
    public SendQueue<T> sendQueue() {
        logger.info("SendQueue requested for {}", name);
        return new BasicSendQueue<>(name, batchSize, queue,
                checkIfBusy, checkEvery, tryTransfer,
                enqueueTimeoutTimeUnit, enqueueTimeout, unableToEnqueueHandler);
    }


    @Override
    public void startListener(final ReceiveQueueListener<T> listener) {


        stop.set(false);
        logger.info("Starting queue listener for  {} {}" , name, listener);

        if (executorContext != null) {
            throw new IllegalStateException("Queue.startListener::Unable to startClient up twice: " + name);
        }

        this.executorContext = scheduledExecutorBuilder()
                .setThreadName("QueueListener " + name)
                .setInitialDelay(50)
                .setPeriod(50).setRunnable(() -> manageQueue(listener))
                .build();

        executorContext.start();
    }

    @Override
    public void stop() {

        logger.info("Stopping queue  {}", name);

        stop.set(true);
        if (executorContext != null) {
            executorContext.stop();
        }

    }

    @Override
    public int size() {
        return queue.size();
    }

    private void manageQueue(ReceiveQueueListener<T> listener) {
        this.receiveQueueManager.manageQueue(receiveQueue(), listener, batchSize, stop);
    }

    @Override
    public String toString() {
        return "BasicQueue{" +
                "name='" + name + '\'' +
                '}';
    }
}
