package CarRecognizer;

import java.util.Random;

public class NodeWeightWrapper {

	private Node node;
	private double weight;
	
	public NodeWeightWrapper(Node node, double weight) {
		this.node = node;
		this.weight = weight;
	}

	public Node getNode() {
		return this.node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public double getWeight() {
		return this.weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
}
