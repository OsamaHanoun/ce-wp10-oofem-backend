package fem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import iceb.jnumerics.Array2DMatrix;
import iceb.jnumerics.ArrayVector;
import iceb.jnumerics.IMatrix;
import iceb.jnumerics.QuadraticMatrixInfo;
import iceb.jnumerics.SolveFailedException;
import iceb.jnumerics.Vector3D;
import iceb.jnumerics.lse.GeneralMatrixLSESolver;
import iceb.jnumerics.lse.ILSESolver;

public class Structure {
	private ArrayList<Node> nodes = new ArrayList<Node>();
	private ArrayList<Element> elements = new ArrayList<Element>();
	private int dof = 0;
	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

	public Node addNode(double x1, double x2, double x3) {
		Node node = new Node(x1, x2, x3);
		this.nodes.add(node);
		return node;
	}

	public Element addElement(double elasticModulus, double area, Node node1, Node node2) {
		Element element = new Element(elasticModulus, area, node1, node2);
		this.elements.add(element);
		return element;
	}

	public int getNumberOfNodes() {
		return this.nodes.size();

	}

	public Node getNode(int i) {
		return this.nodes.get(i);
	}

	public int getNumberOfElements() {
		return this.elements.size();
	}

	public Element getElement(int i) {
		return this.elements.get(i);
	}

	public ArrayVector solve() throws SolveFailedException {
		this.enumerateDOFs();
		ArrayVector uGlobal = new ArrayVector(this.dof);
		ILSESolver solver = new GeneralMatrixLSESolver();
		QuadraticMatrixInfo aInfo = solver.getAInfo();
		IMatrix a = solver.getA();
		double[] b = new double[this.dof];
		aInfo.setSize(this.dof);
		solver.initialize();
		this.assembleStiffnessMatrix(a);
		this.assembleLoadVector(b);

		solver.solve(b);

		for (int i = 0; i < this.dof; i++) {
			uGlobal.set(i, b[i]);
		}
		this.setNodesDisplacements(uGlobal);
		this.calcElementsAxialForces();
		return uGlobal;
	}

	private int enumerateDOFs() {
		int counter = 0;

		for (int i = 0; i < this.nodes.size(); i++) {
			counter = this.nodes.get(i).enumerateDOFs(counter);
		}

		for (int i = 0; i < this.elements.size(); i++) {
			this.elements.get(i).enumerateDOFs();
		}

		this.dof = counter;
		return counter;
	}

	private void setNodesDisplacements(ArrayVector uGlobal) {
		for (int i = 0; i < this.nodes.size(); i++) {
			double[] nodeDisplacement = new double[3];
			int[] dof_node = this.nodes.get(i).getDOFNumbers();

			for (int j = 0; j < 3; j++) {
				if (dof_node[j] != -1)
					nodeDisplacement[j] = uGlobal.get(dof_node[j]);
			}

			this.nodes.get(i).setDisplacement(new Vector3D(nodeDisplacement));
		}
	}

	private void calcElementsAxialForces() {
		for (int i = 0; i < this.elements.size(); i++)
			this.elements.get(i).calcAxialForce();
	}

	private IMatrix assembleStiffnessMatrix(IMatrix kGlobal) {
		int numElements = this.elements.size();
		for (int i = 0; i < numElements; i++) {
			Array2DMatrix K_element = this.elements.get(i).computeStiffnessMatrix();
			int[] dof_element = this.elements.get(i).getDOFNumbers();

			for (int j = 0; j < dof_element.length; j++) {
				if (dof_element[j] != -1) {
					for (int k = 0; k < dof_element.length; k++) {
						if (dof_element[k] != -1) {
							kGlobal.set(dof_element[j], dof_element[k],
									kGlobal.get(dof_element[j], dof_element[k]) + K_element.get(j, k));
						}
					}
				}
			}
		}
		return kGlobal;
	}

	private void assembleLoadVector(double[] rGlobal) {
		int number_of_nodes = this.nodes.size();
		for (int i = 0; i < number_of_nodes; i++) {
			int[] dof_node = this.nodes.get(i).getDOFNumbers();
			for (int j = 0; j < dof_node.length; j++) {
				if (dof_node[j] != -1) {
					rGlobal[dof_node[j]] = this.nodes.get(i).getForce().getComponent(j);
				}
			}
		}

	}

	public void selectDisplacements(ArrayVector uGlobal) {
		for (int i = 0; i < this.nodes.size(); i++) {
			double[] nodeDisplacement = new double[3];
			int[] dof_node = this.nodes.get(i).getDOFNumbers();

			for (int j = 0; j < 3; j++) {
				if (dof_node[j] != -1)
					nodeDisplacement[j] = uGlobal.get(dof_node[j]);
			}

			this.nodes.get(i).setDisplacement(new Vector3D(nodeDisplacement));
		}
	}

	public double getSmallestDistanceBetweenTwoNodes() {
		double minimumDistance = Double.POSITIVE_INFINITY;
		int nodesCount = this.nodes.size();

		for (int i = 0; i < nodesCount - 1; i++) {
			Vector3D nodeCoordinates = this.nodes.get(i).getPosition();

			for (int j = 0; j < nodesCount; j++) {
				Vector3D nextNodeCoordinates = this.nodes.get(i + 1).getPosition();
				double dx1 = Math.abs(nodeCoordinates.getX1() - nextNodeCoordinates.getX1());
				double dx2 = Math.abs(nodeCoordinates.getX2() - nextNodeCoordinates.getX2());
				double dx3 = Math.abs(nodeCoordinates.getX3() - nextNodeCoordinates.getX3());
				minimumDistance = Math.min(minimumDistance, dx1);
				minimumDistance = Math.min(minimumDistance, dx2);
				minimumDistance = Math.min(minimumDistance, dx3);
			}
		}
		return minimumDistance;
	}

	public double getSmallestElementLength() {
		double minLength = Double.POSITIVE_INFINITY;
		int elementsCount = this.elements.size();

		for (int i = 0; i < elementsCount; i++) {
			double currentLength = this.elements.get(i).getLength();
			minLength = Math.min(minLength, currentLength);
		}

		return minLength;
	}

	public void print() {

		System.out.println("form structure");

	}

	private String getCurrentTime() {
		String time = dtf.format(LocalDateTime.now());
		return "[" + time + "]: ";
	}

}
