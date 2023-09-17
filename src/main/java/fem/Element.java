package fem;

import iceb.jnumerics.Array2DMatrix;
import iceb.jnumerics.Vector3D;

public class Element {
	private double area, elasticModulus;
	private Node n1, n2;
	private int[] dofNumbers = new int[6];
	private double length;
	private double axialForce = 0;

	public Element(double elasticModulus, double area, Node n1, Node n2) {
		this.elasticModulus = elasticModulus;
		this.area = area;
		this.n1 = n1;
		this.n2 = n2;
		this.calcLength();
	}

	public Node getNode1() {
		return this.n1;
	}

	public Node getNode2() {
		return this.n2;
	}

	public double getElasticModulus() {
		return this.elasticModulus;
	}

	public double getArea() {
		return this.area;
	}

	public double getLength() {
		return this.length;
	}

	public double getAxialForce() {
		return axialForce;
	}

	public void setAxialForce(double internalForce) {
		this.axialForce = internalForce;
	}

	private void calcLength() {
		Vector3D position1 = this.n1.getPosition();
		Vector3D position2 = this.n2.getPosition();

		this.length = Math.sqrt(Math.pow((position1.c1 - position2.c1), 2) + Math.pow((position1.c2 - position2.c2), 2)
				+ Math.pow((position1.c3 - position2.c3), 2));

	}

	public void calcAxialForce() {
		Vector3D position1 = this.n1.getPosition().add(this.n1.getDisplacement());
		Vector3D position2 = this.n2.getPosition().add(this.n2.getDisplacement());
		double deltaLength = Math.sqrt(Math.pow((position1.c1 - position2.c1), 2)
				+ Math.pow((position1.c2 - position2.c2), 2) + Math.pow((position1.c3 - position2.c3), 2))
				- this.length;
		this.axialForce = this.area * this.elasticModulus * deltaLength / this.length;
	}

	public void enumerateDOFs() {
		for (int i = 0; i < 3; i++) {
			this.dofNumbers[i] = this.n1.getDOFNumbers()[i];

		}
		for (int i = 0; i < 3; i++) {
			this.dofNumbers[i + 3] = this.n2.getDOFNumbers()[i];
		}
	}

	public int[] getDOFNumbers() {
		return this.dofNumbers;
	}

	private Array2DMatrix computeLocalStiffnessMatrix() {
		double[][] values = new double[2][2];
		values[0][0] = 1;
		values[0][1] = -1;
		values[1][0] = -1;
		values[1][1] = 1;
		Array2DMatrix element_local_stiffness_matrix = new Array2DMatrix(values);

		double stiffness = this.area * this.elasticModulus / this.getLength();
		int row = element_local_stiffness_matrix.getRowCount();
		int col = element_local_stiffness_matrix.getColumnCount();
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {
				element_local_stiffness_matrix.set(i, j, stiffness * element_local_stiffness_matrix.get(i, j));

			}

		}
		return element_local_stiffness_matrix;
	}

	public Array2DMatrix Compute_Transformations_Matrix() {
		double[][] initial_transformation = new double[2][6];
		for (int i = 0; i < initial_transformation.length; i++) {
			for (int j = 0; j < initial_transformation[i].length; j++) {
				initial_transformation[i][j] = 0;
			}
		}
		Array2DMatrix transformation = new Array2DMatrix(initial_transformation);
		double[] cos_alpha = new double[3];
		double[] numerator = new double[3];
		for (int i = 0; i < 3; i++) {
			numerator[0] = this.n2.getPosition().c1 - this.n1.getPosition().c1;
			numerator[1] = this.n2.getPosition().c2 - this.n1.getPosition().c2;
			numerator[2] = this.n2.getPosition().c3 - this.n1.getPosition().c3;
			cos_alpha[i] = numerator[i] / this.getLength();
		}

		transformation.set(0, 0, cos_alpha[0]);
		transformation.set(1, 3, cos_alpha[0]);
		transformation.set(0, 1, cos_alpha[1]);
		transformation.set(1, 4, cos_alpha[1]);
		transformation.set(0, 2, cos_alpha[2]);
		transformation.set(1, 5, cos_alpha[2]);

		return transformation;
	}

	private Array2DMatrix computeTransposeTransformation() {
		double[][] initial_transformation = new double[6][2];
		Array2DMatrix transpose_transformation = new Array2DMatrix(initial_transformation);
		Array2DMatrix transformation = this.Compute_Transformations_Matrix();
		for (int i = 0; i < transpose_transformation.getRowCount(); i++) {
			for (int j = 0; j < transpose_transformation.getColumnCount(); j++) {
				transpose_transformation.set(i, j, transformation.get(j, i));
			}
		}

		return transpose_transformation;
	}

	public Array2DMatrix computeStiffnessMatrix() {
		Array2DMatrix k1 = (Array2DMatrix) this.computeTransposeTransformation()
				.multiply(this.computeLocalStiffnessMatrix());
		Array2DMatrix stiffness_matrix = (Array2DMatrix) k1.multiply(this.Compute_Transformations_Matrix());
		return stiffness_matrix;
	}

	public double computeForce() {
		Vector3D node1Displacement = this.getNode1().getDisplacement();
		Vector3D node2Displacement = this.getNode2().getDisplacement();
		Vector3D pos1 = this.getNode1().getPosition();
		Vector3D pos2 = this.getNode2().getPosition();
		node1Displacement = node1Displacement.add(pos1);
		node2Displacement = node2Displacement.add(pos2);
		double element_force = (node2Displacement.subtract(node1Displacement).normTwo() - this.getLength())
				* (this.getArea() * this.getElasticModulus()) / this.getLength();
		return element_force;
	}

	public void print() {
		System.out.println("\nFor the element: ");
		System.out.println("    The Young Mudulus is: " + this.elasticModulus);
		System.out.println("    The area is: " + this.area);
		System.out.println("    The lenght is: " + this.getLength() + "\n");
	}

}
