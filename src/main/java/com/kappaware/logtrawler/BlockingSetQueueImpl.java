/*
 * Copyright (C) 2016 BROADSoftware
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kappaware.logtrawler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * add() is only effective if an equal object does not already exists in the queue.
 * 
 * Note this could be implemented using the contains() of the Queue, but I think using a HashSet is more time effective.
 * 
 * This is not synchronized (Doing so will dead lock, as poll is blocking). In case of race condition, it may occurs  
 * @author sa
 *
 * @param <E>
 */
public class BlockingSetQueueImpl<E> implements BlockingSetQueue<E> {
	BlockingQueue<E> queue = new LinkedBlockingQueue<E>();
	Set<E> set = new HashSet<E>();

	@Override
	 public boolean add(E e) {
		if(!set.contains(e)) {
			// It is important to put in HashSet BEFORE in blocking queue. 
			// Doing reverse way could generate the following case:
			// This by the following
			// - add().queue.add()
			// - poll().poll() get it
			// - poll().set.remove()
			// - add().set.add()
			// An event will be in the HashSet, but not in queue, so will never be removed.
			set.add(e);
			queue.add(e);
			return true;
		} else {
			return false;
		}
	}

	@Override
	 public E poll(long timeout, TimeUnit unit) throws InterruptedException {
		E e = this.queue.poll(timeout, unit);
		if(e != null) {
			this.set.remove(e);
		}
		return e;
	}
}
