package CarRecognizer;

import java.util.ArrayList;

public class Node {

	private double value;
	private ArrayList<Node> parents;
	private ArrayList<Node> cousins;
	private ArrayList<NodeWeightWrapper> children;

	public Node() {
		this.parents = new ArrayList<Node>();
		this.cousins = new ArrayList<Node>();
		this.children = new ArrayList<NodeWeightWrapper>();
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public ArrayList<Node> getParents() {
		return parents;
	}

	public void setParents(ArrayList<Node> parents) {
		this.parents = parents;
	}

	public ArrayList<Node> getCousins() {
		return cousins;
	}

	public void setCousins(ArrayList<Node> cousins) {
		this.cousins = cousins;
	}

	public ArrayList<NodeWeightWrapper> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<NodeWeightWrapper> children) {
		this.children = children;
	}
}
