package fem;

public class Force {

	private double[] components = new double[3];

	public Force(double r1, double r2, double r3) {
		this.components[0] = r1;
		this.components[1] = r2;
		this.components[2] = r3;
	}

	public double getComponent(int axis) {
		return this.components[axis];
	}

	public void print() {		
			System.out.print("\t" + "r1= " +components[0] );			
			System.out.print("\t" + "r2= " +components[1] );			
			System.out.print("\t" + "r3= " +components[2] + "\n");			
	}
}
