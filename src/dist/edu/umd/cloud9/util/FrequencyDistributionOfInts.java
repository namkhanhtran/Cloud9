/*
 * Cloud9: A MapReduce Library for Hadoop
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.umd.cloud9.util;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import edu.umd.cloud9.io.PairOfInts;

/**
 * An implementation of a frequency distribution for int events, backed by a
 * fastutil map. One common use is to store frequency counts for a vocabulary
 * space that has been integerized, i.e., each term has been mapped to an
 * integer. This class keeps track of frequencies using ints, so beware when
 * dealing with a large number of observations; see also
 * {@link LargeFrequencyDistributionOfInts}.
 *
 * @author Jimmy Lin
 *
 */
public class FrequencyDistributionOfInts extends Int2IntOpenHashMap {

	private static final long serialVersionUID = -8991144500446882265L;

	private long mSumOfFrequencies = 0;

	/**
	 * Increments the frequency of an event <code>key</code>.
	 */
	public void increment(int key) {
		if (containsKey(key)) {
			put(key, get(key) + 1);
		} else {
			put(key, 1);
		}
	}

	/**
	 * Increments the frequency of an event <code>key</code> by <code>cnt</code>.
	 */
	public void increment(int key, int cnt) {
		if (containsKey(key)) {
			put(key, get(key) + cnt);
		} else {
			put(key, cnt);
		}
	}

	/**
	 * Decrements the frequency of an event <code>key</code>.
	 */
	public void decrement(int key) {
		if (containsKey(key)) {
			int v = get(key);
			if (v == 1) {
				remove(key);
			} else {
				put(key, this.get(key) - 1);
			}
		} else {
			throw new RuntimeException("Can't decrement non-existent event!");
		}
	}

	/**
	 * Decrements the frequency of an event <code>key</code> by <code>cnt</code>.
	 */
	public void decrement(int key, int cnt) {
		if (containsKey(key)) {
			int v = get(key);
			if (v < cnt) {
				throw new RuntimeException("Can't decrement past zero!");
			} else if (v == cnt) {
				remove(key);
			} else {
				put(key, this.get(key) - cnt);
			}
		} else {
			throw new RuntimeException("Can't decrement non-existent event!");
		}
	}

	/**
	 * Returns the frequency of a particular event <i>key</i>.
	 */
	@Override
	public int get(int key) {
		return super.get(key);
	}

	/**
	 * Sets the frequency of a particular event <code>key</code> to count <code>v</code>.
	 */
	@Override
	public int put(int key, int v) {
		int rv = super.put(key, v);
		mSumOfFrequencies = mSumOfFrequencies - rv + v;

		return rv;
	}

	/**
	 * Sets the frequency of a particular event <code>ok</code> to count <code>ov</code>.
	 */
	@Override
	public Integer put(Integer ok, Integer ov) {
		return put((int) ok, (int) ov);
	}

	/**
	 * Removes the count of a particular event <code>key</code>.
	 */
	@Override
	public int remove(int key) {
		int rv = super.remove(key);
		mSumOfFrequencies -= rv;

		return rv;
	}

	/**
	 * Removes the count of a particular event <code>ok</code>.
	 */
	@Override
	public Integer remove(Object ok) {
		return this.remove((int) (Integer) ok);
	}

	/**
	 * Returns events sorted by frequency of occurrence.
	 */
	public List<PairOfInts> getFrequencySortedEvents() {
		List<PairOfInts> list = Lists.newArrayList();

		for (Int2IntMap.Entry e : int2IntEntrySet()) {
			list.add(new PairOfInts(e.getIntKey(), e.getIntValue()));
		}

		Collections.sort(list, new Comparator<PairOfInts>() {
			public int compare(PairOfInts e1, PairOfInts e2) {
				if (e1.getRightElement() > e2.getRightElement()) {
					return -1;
				}

				if (e1.getRightElement() < e2.getRightElement()) {
					return 1;
				}

				if (e1.getLeftElement() == e2.getLeftElement()) {
					throw new RuntimeException("Event observed twice!");
				}

				return e1.getLeftElement() < e2.getLeftElement() ? -1 : 1;
			}
		});

		return list;
	}

	/**
	 * Returns top <i>n</i> events sorted by frequency of occurrence.
	 */
	public List<PairOfInts> getFrequencySortedEvents(int n) {
		List<PairOfInts> list = getFrequencySortedEvents();
		return list.subList(0, n);
	}

	/**
	 * Returns events in sorted order.
	 */
	public List<PairOfInts> getSortedEvents() {
		List<PairOfInts> list = Lists.newArrayList();

		for (Int2IntMap.Entry e : int2IntEntrySet()) {
			list.add(new PairOfInts(e.getIntKey(), e.getIntValue()));
		}

		Collections.sort(list, new Comparator<PairOfInts>() {
			public int compare(PairOfInts e1, PairOfInts e2) {
				if (e1.getLeftElement() > e2.getLeftElement()) {
					return 1;
				}

				if (e1.getLeftElement() < e2.getLeftElement()) {
					return -1;
				}

				throw new RuntimeException("Event observed twice!");
			}
		});

		return list;
	}

	/**
	 * Returns top <i>n</i> events in sorted order.
	 */
	public List<PairOfInts> getSortedEvents(int n) {
		List<PairOfInts> list = getSortedEvents();
		return list.subList(0, n);
	}

	/**
	 * Returns number of distinct events observed. Note that if an event is
	 * observed and then its count subsequently removed, the event will not be
	 * included in this count.
	 */
	public int getNumberOfEvents() {
		return size();
	}

	/**
	 * Returns the sum of frequencies of all observed events.
	 */
	public long getSumOfFrequencies() {
		return mSumOfFrequencies;
	}
}
