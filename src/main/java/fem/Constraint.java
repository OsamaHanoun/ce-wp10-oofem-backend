package fem;

import inf.text.ArrayFormat;

public class Constraint {

	private boolean[] free = new boolean[3];

	public Constraint(boolean u1, boolean u2, boolean u3) {
		this.free[0] = u1;
		this.free[1] = u2;
		this.free[2] = u3;
	}

	public boolean isFree(int constraint) {
		return this.free[constraint];
	}

	public boolean[] getConstriant() {
		return this.free;
	}

	public void print() {
		String free[] = new String[this.free.length];
		
		for (int i = 1; i <= free.length; i++) {
		    if (this.free[i]) {
		    	free[i] ="u"+ i + ": free";
		    } else {
		    	free[i] = "u" + i + ": fixed";
		    }
		}
		
		System.out.println("\t" + ArrayFormat.format(free));
	}

}
