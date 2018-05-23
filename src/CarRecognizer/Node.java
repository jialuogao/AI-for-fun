package CarRecognizer;

import java.util.ArrayList;

public class Node {

	private double value;
	private ArrayList<NodeWeightWrapper> children;

	public Node() {
		this.children = new ArrayList<NodeWeightWrapper>();
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public ArrayList<NodeWeightWrapper> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<NodeWeightWrapper> children) {
		this.children = children;
	}
}
