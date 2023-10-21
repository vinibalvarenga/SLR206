package linkedlists.lockbased;

import contention.abstractions.AbstractCompositionalIntSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HandOverHandListBasedSet extends AbstractCompositionalIntSet {

	// sentinel nodes
	private Node head;
	private Node tail;

	public HandOverHandListBasedSet() {
		head = new Node(Integer.MIN_VALUE);
		tail = new Node(Integer.MAX_VALUE);
		head.next = tail;
	}

	/*
	 * Insert
	 * 
	 * @see contention.abstractions.CompositionalIntSet#addInt(int)
	 */
	@Override
	public boolean addInt(int item) {
		head.lock();
		Node pred = head;
		Node curr = pred.next;
		try {
			curr.lock();
			try {
				while (curr.key < item) {
					pred.unlock();
					pred = curr;
					curr = pred.next;
					curr.lock();
				}
				if (curr.key == item) {
					return false;
				}
				Node node = new Node(item);
				node.next = curr;
				pred.next = node;
				return true;
			} finally {
				curr.unlock();
			}
		} finally {
			pred.unlock();
		}
	}

	/*
	 * Remove
	 * 
	 * @see contention.abstractions.CompositionalIntSet#removeInt(int)
	 */
	@Override
	public boolean removeInt(int item) {
		head.lock();
		Node pred = head;
		Node curr = pred.next;
		try {
			curr.lock();
			try {
				while (curr.key < item) {
					pred.unlock();
					pred = curr;
					curr = pred.next;
					curr.lock();
				}
				if (curr.key == item) {
					pred.next = curr.next;
					return true;
				}
				return false;
			} finally {
				curr.unlock();
			}
		} finally {
			pred.unlock();
		}
	}

	/*
	 * Contains
	 * 
	 * @see contention.abstractions.CompositionalIntSet#containsInt(int)
	 */
	@Override
	public boolean containsInt(int item) {
		head.lock();
		Node pred = head;
		Node curr = pred.next;
		try {
			curr.lock();
			try {
				while (curr.key < item) {
					pred.unlock();
					pred = curr;
					curr = pred.next;
					curr.lock();
				}
				return (curr.key == item);
			} finally {
				curr.unlock();
			}
		} finally {
			pred.unlock();
		}
	}

	private class Node {
		public int key;
		public Node next;
		private Lock lock;

		Node(int item) {
			this.key = item;
			this.next = null;
			this.lock = new ReentrantLock();
		}

		public void lock() {
			this.lock.lock();
		}

		public void unlock() {
			this.lock.unlock();
		}
	}

	@Override
	public void clear() {
		head = new Node(Integer.MIN_VALUE);
		head.next = new Node(Integer.MAX_VALUE);
	}

	/**
	 * Non atomic and thread-unsafe
	 */
	@Override
	public int size() {
		int count = 0;

		Node curr = head.next;
		while (curr.key != Integer.MAX_VALUE) {
			curr = curr.next;
			count++;
		}
		return count;
	}
}
