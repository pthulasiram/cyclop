/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cyclop.service.queryprotocoling.intern;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.cyclop.model.QueryHistory;
import org.cyclop.model.UserIdentifier;
import org.cyclop.service.common.FileStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

/** @author Maciej Miklas */
@Named
class AsyncFileStore<H> {

	private final static int FLUSH_MILIS = 30000;

	@Inject
	private FileStorage fileStorage;

	private final static Logger LOG = LoggerFactory.getLogger(AsyncFileStore.class);

	private final Map<UserIdentifier, H> diskQueue = new HashMap<>();

	public void store(UserIdentifier identifier, H history) {
		synchronized (diskQueue) {
			diskQueue.put(identifier, history);
		}
	}

	public Optional<H> getFromWriteQueue(UserIdentifier identifier) {
		LOG.debug("Reading history from queue for: {}", identifier);
		synchronized (diskQueue) {
			final H hist = diskQueue.get(identifier);
			LOG.trace("Found history: {}", hist);
			return Optional.ofNullable(hist);
		}
	}

	/**
	 * method must be synchronized to avoid parallel write access on files for
	 * single user-id. Second synchronization block on map ensures short lock
	 * time on map, so that {@link #store(UserIdentifier, QueryHistory)} method
	 * block time is reduced
	 */
	@Scheduled(initialDelay = FLUSH_MILIS, fixedDelay = FLUSH_MILIS)
	@PreDestroy
	public synchronized void flush() {
		LOG.debug("Flushing history");
		while (true) {
			UserIdentifier identifier;
			H history;

			// synchronize #historyMap only for short time to not block
			// store(...) function by file operation
			synchronized (diskQueue) {
				LOG.debug("Got into mutex");
				if (diskQueue.isEmpty()) {
					LOG.debug("Flush done - no more entries found");
					return;
				}
				identifier = diskQueue.keySet().iterator().next();
				history = diskQueue.remove(identifier);
			}
			fileStorage.store(identifier, history);
		}
	}

}
