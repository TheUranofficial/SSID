package com.theuran.pokoyo;

import org.joml.Matrix4f;

/* Its timed structure because I don't watch on projection bbs code */
public class Projection {
    public final float fov;
    public final Matrix4f projectionMatrix;
    public final float zFar;
    public final float zNear;

    public Projection(float fov, float zNear, float zFar, int width, int height) {
        this.fov = fov;
        this.zNear = zNear;
        this.zFar = zFar;
        this.projectionMatrix = new Matrix4f();

        this.resize(width, height);
    }

    public void resize(int width, int height) {
        this.projectionMatrix.identity();
        this.projectionMatrix.perspective(this.fov, (float) width / (float) height, this.zNear, this.zFar, true);
    }
}