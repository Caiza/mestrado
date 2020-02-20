package she.adaptation.periodic;

import java.util.LinkedList;

/*
 * Essa classe constrói uma janela de tamnho especifico conforme a propriedade capacity
 * pra caucular a média. Essa media vai conforme o tamanho da janela.
 */
public class Window {

	private int capacity;

	private int minSize;

	private LinkedList<Double> linkedList = new LinkedList<Double>();

	public Window(int capacity, int minSize) {
		this.capacity = capacity;
		this.minSize = minSize;
	}

	/**
	 * @return the capacity
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * @param capacity the capacity to set
	 */
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	/**
	 * @return the minSize
	 */
	public int getMinSize() {
		return minSize;
	}

	/**
	 * @param minSize the minSize to set
	 */
	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}

	/**
	 * @return the linkedList
	 */
	public LinkedList<Double> getLinkedList() {
		return linkedList;
	}

	public void add(double result) {

		if (linkedList.size() >= capacity) {
			linkedList.removeFirst();
		}

		linkedList.add(result);
	}

	public double getAverage() {

		if (!isAveragePossible())
			return 0;

		double sum = 0;

		for (Double val : linkedList) {
			sum += val;
		}

		return sum / linkedList.size();
	}

	public void reset() {
		linkedList.clear();
	}

	public boolean isAveragePossible() {
		return linkedList.size() >= minSize;
	}
}
