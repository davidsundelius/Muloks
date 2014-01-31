package mouserunner.Model3D;

public class Matrix {

	private float[] matrix = new float[16];

	public Matrix() {
		loadIdentity();
	}

	public Matrix(float[] matrix) {
		for (int i = 0; i < 16; i++) {
			this.matrix[i] = matrix[i];
		}
	}

	public Matrix(Matrix matrix) {
		setMatrix(matrix);
	}

	public void setMatrix(Matrix matrix) {
		for (int i = 0; i < 16; i++) {
			this.matrix[i] = matrix.matrix[i];
		}
	}

	public void loadIdentity() {
		for (int i = 0; i < 16; i++) {
			matrix[i] = i % 5 == 0 ? 1 : 0;
		}
	}

	public void translate(final float[] v) {
		matrix[3] += v[0];
		matrix[7] += v[1];
		matrix[11] += v[2];
	}

	public void rotate(final float[] v) {
		final double cr = Math.cos(v[0]);
		final double sr = Math.sin(v[0]);
		final double cp = Math.cos(v[1]);
		final double sp = Math.sin(v[1]);
		final double cy = Math.cos(v[2]);
		final double sy = Math.sin(v[2]);
		final double srsp = sr * sp;
		final double crsp = cr * sp;

		matrix[0] = (float) (cp * cy);
		matrix[1] = (float) (cp * sy);
		matrix[2] = (float) (-sp);

		matrix[4] = (float) (srsp * cy - cr * sy);
		matrix[5] = (float) (srsp * sy + cr * cy);
		matrix[6] = (float) (sr * cp);

		matrix[8] = (float) (crsp * cy + sr * sy);
		matrix[9] = (float) (crsp * sy - sr * cy);
		matrix[10] = (float) (cr * cp);
	}

	public void multiply(final Matrix mmatrix) {
		float[] newMatrix=new float[16];
		for (int i = 0; i < 4; i++) 
			for (int j = 0; j < 4; j++)
				for (int k = 0; k < 4; k++)
					newMatrix[i + (j * 4)] += mmatrix.matrix[i + (k * 4)] * matrix[k + (j * 4)];
		matrix=newMatrix;
	}

	public float[] transform(float[] v) {
		float[] outv = new float[3];
		outv[0] = v[0] * matrix[0] + v[1] * matrix[1] + v[2] * matrix[2] + matrix[3];
		outv[1] = v[0] * matrix[4] + v[1] * matrix[5] + v[2] * matrix[6] + matrix[7];
		outv[2] = v[0] * matrix[8] + v[1] * matrix[9] + v[2] * matrix[10] + matrix[11];
		return outv;
	}
	
	public float[] inverseTransform(float[] v) {
		float[] outv = new float[3];
		outv[0] = v[0] - matrix[3];
		outv[1] = v[1] - matrix[7];
		outv[2] = v[0] - matrix[11];
		outv[0] = outv[0]*matrix[0] + outv[1]*matrix[1] + outv[2]*matrix[2];
		outv[1] = outv[0]*matrix[4] + outv[1]*matrix[5] + outv[2]*matrix[6];
		outv[2] = outv[0]*matrix[8] + outv[1]*matrix[9] + outv[2]*matrix[10];
		return outv;
	}

	public void printDebug() {
		System.out.println(matrix[0] + " " + matrix[1] + " " + matrix[2] + " " + matrix[3]);
		System.out.println(matrix[4] + " " + matrix[5] + " " + matrix[6] + " " + matrix[7]);
		System.out.println(matrix[8] + " " + matrix[9] + " " + matrix[10] + " " + matrix[11]);
		System.out.println(matrix[12] + " " + matrix[13] + " " + matrix[14] + " " + matrix[15]);
	}
}
