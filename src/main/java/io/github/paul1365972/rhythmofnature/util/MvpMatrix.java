package io.github.paul1365972.rhythmofnature.util;

import org.joml.Matrix4f;

public class MvpMatrix {
	
	private Matrix4f mvp, model, vp;
	
	public MvpMatrix() {
		mvp = new Matrix4f();
		model = new Matrix4f();
		vp = new Matrix4f();
	}
	
	public Matrix4f get() {
		return vp.mul(model, mvp);
	}
	
	public void resetModel() {
		model.identity();
	}
	
	public void setModel(float x, float y, float z, float sx, float sy) {
		model.identity().translation(x, y, z).scale(sx, sy, 1);
	}
	
	public void setModel(float x, float y, float z, float sx, float sy, float rotate) {
		model.identity().translation(x, y, z).rotateZ(rotate).scale(sx, sy, 1);
	}
	
	public void setProjectionView(float x, float y, float rotation, float sx, float sy) {
		vp.identity().setOrthoSymmetric(16f / 9f, 1, -1, 1).translate(-x, -y, 0).rotateZ(rotation).scale(sx, sy, 1);
	}
}
