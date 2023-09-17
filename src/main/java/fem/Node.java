package fem;

import iceb.jnumerics.MatrixFormat;
import iceb.jnumerics.Vector3D;

public class Node {

	private Constraint constraint = new Constraint(true, true, true);
	private Vector3D displacement = new Vector3D();
	private int[] dofNumber = new int[3];
	private Force force = new Force(0, 0, 0);
	private Vector3D position;

	public Node(double x1, double x2, double x3) {
		this.position = new Vector3D(x1, x2, x3);
	}

	public void setConstraint(Constraint c) {
		this.constraint = c;
	}

	public Constraint getConstraint() {
		return this.constraint;
	}

	public void setForce(Force force) {
		this.force = force;
	}

	public Force getForce() {
		return this.force;
	}

	public int enumerateDOFs(int start) {
		for (int i = 0; i < 3; i++) {
			if (this.constraint.isFree(i)) {
				this.dofNumber[i] = start;
				start = start + 1;
			} else {
				this.dofNumber[i] = -1;
			}
		}
		return start;
	}

	public int[] getDOFNumbers() {
		return this.dofNumber;
	}

	public Vector3D getPosition() {
		return this.position;
	}

	public void setDisplacement(Vector3D displacement) {
		this.displacement = displacement;

	}

	public Vector3D getDisplacement() {
		return this.displacement;
	}

	public void print() {

		System.out.println("Thr coordinates are: " + "\n       " + MatrixFormat.format(this.position));
		System.out.println("Thr displacements are: " + "\n       " + MatrixFormat.format(this.getDisplacement()));
		System.out.println("The constraints are: ");
		for (int j = 0; j < 3; j++) {
			if (this.getConstraint().isFree(j) == true) {
				System.out.print("             free     ");
			} else {
				System.out.print("            fixed     ");
			}
		}
		System.out.print("\n");
	}
}
