package com.bandwidth.tannin.rajawali;

import rajawali.BaseObject3D;

public class DonutSegment extends BaseObject3D {
    private float mInnerRadius;
    private float mOuterRadius;
    private float mStartAngle;
    private float mEndAngle;
    private float[] mColor;
    private float mY;
    private float mZ;
    
    private static final int NUM_VERTICES_CIRCLE = 100;
    
    public DonutSegment(float innerRadius, 
            float outerRadius,
            float startAngle,
            float endAngle,
            float[] color,
            float y,
            float z) {
        mInnerRadius = innerRadius;
        mOuterRadius = outerRadius;
        mStartAngle = startAngle;
        mEndAngle = endAngle;
        mColor = color;
        mY = y;
        mZ = z;
        init();
    }
    
    private int numVertices() {
        return Math.max(8, 2*(int)(NUM_VERTICES_CIRCLE*(mEndAngle-mStartAngle)/(2*Math.PI)));
    }
    
    private void init() {
        int numVerts = numVertices();
        float[] vertices = new float[numVerts*3];
        float[] textureCoords = new float[numVerts*2];
        float[] normals = new float[numVerts*3];
        float[] colors = new float[numVerts*4];
        
        int numTri = 2*(numVerts/2-1);
        int[] indices = new int[numTri*3];
        
        int vertexCount = 0;
        int texCoordCount = 0;
        int numSteps = numVerts/2;
        double angleStep = (mEndAngle-mStartAngle)/(numSteps-1);
        for(int i = 0; i < numSteps; ++i) {
            double angle = mStartAngle + angleStep*i;
            
            vertices[3*vertexCount+0] = (float)(mInnerRadius*Math.cos(angle));
            vertices[3*vertexCount+1] = (float)(mInnerRadius*Math.sin(angle))+mY;
            vertices[3*vertexCount+2] = mZ;
            
            normals[3*vertexCount+2] = 1.f;
            ++vertexCount;
            
            vertices[3*vertexCount+0] = (float)(mOuterRadius*Math.cos(angle));
            vertices[3*vertexCount+1] = (float)(mOuterRadius*Math.sin(angle))+mY;
            vertices[3*vertexCount+2] = mZ;
            
            normals[3*vertexCount+2] = 1.f;
            ++vertexCount;
            
            textureCoords[2*texCoordCount+0] = 0;
            textureCoords[2*texCoordCount+1] = i/numSteps;
            ++texCoordCount;
            
            textureCoords[2*texCoordCount+0] = 1;
            textureCoords[2*texCoordCount+1] = i/numSteps;
            ++texCoordCount;
        }
        
        int triCount = 0;
        for(int i = 0; i < numTri/2; ++i) {
            int baseInd = i*2;
            indices[3*triCount+0] = baseInd;
            indices[3*triCount+1] = baseInd+2;
            indices[3*triCount+2] = baseInd+1;
            ++triCount;
            
            indices[3*triCount+0] = baseInd+2;
            indices[3*triCount+1] = baseInd+3;
            indices[3*triCount+2] = baseInd+1;
            ++triCount;
        }
        
        int numColors = numVerts * 4;
        for(int i = 0; i < numColors; i += 4 ) {
            colors[i+0] = mColor[0];
            colors[i+1] = mColor[1];
            colors[i+2] = mColor[2];
            colors[i+3] = mColor[3];
        }
        
        setData(vertices, normals, textureCoords, colors, indices);
    }
}
