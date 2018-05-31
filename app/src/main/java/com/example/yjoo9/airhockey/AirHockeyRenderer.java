package com.example.yjoo9.airhockey;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.example.yjoo9.airhockey.android.util.LoggerConfig;
import com.example.yjoo9.airhockey.android.util.MatrixHelper;
import com.example.yjoo9.airhockey.android.util.ShaderHelper;
import com.example.yjoo9.firstopengl.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.orthoM;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

public class AirHockeyRenderer implements GLSurfaceView.Renderer {
    private static final String U_MATRIX = "u_Matrix";
    private static final String A_COLOR = "a_Color";
    private static final int COLOR_COMPONENT_COUNT = 3;
    //private static final String U_COLOR = "u_Color";
    private static final String A_POSITION = "a_Position";
    private static final int POSITION_COMPONENT_COUNT= 4; //x, y, z, w
    private static final int BYTES_PER_FLOAT = 4;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT+COLOR_COMPONENT_COUNT)*BYTES_PER_FLOAT;

    private final FloatBuffer mVertexData;
    private final Context mContext;
    private final float[] mProjectionMatrix = new float[16];
    private final float Magnify = 1.5f;
    private final float Compress = 0.7f;
    private final float[] mModelMatrix = new float[16];

    private int mProgram;
    private int mUColorLocation;
    private int mAPositionLocation;
    private int mAColorLocation;
    private int mUMatrixLocation;

    public AirHockeyRenderer(Context context){
        this.mContext = context;
        float[] tableVerticesWithTriangles = {
           //       X,    Y,     Z,    W      R,    G,    B   adding color to each vertex
                // Triangle Fan
                   0f,    0f,   0f,    1f,   1f,   1f,   1f,
                -0.5f, -0.8f,   0f,    1f, 0.7f, 0.7f, 0.7f,
                 0.5f, -0.8f,   0f,    1f, 0.7f, 0.7f, 0.7f,
                 0.5f,  0.8f,   0f,    1f, 0.7f, 0.7f, 0.7f,
                -0.5f,  0.8f,   0f,    1f, 0.7f, 0.7f, 0.7f,
                -0.5f, -0.8f,   0f,    1f, 0.7f, 0.7f, 0.7f,

                -0.5f, 0f, 0f, 1.5f, 1f, 0f, 0f,
                 0.5f, 0f, 0f, 1.5f, 1f, 0f, 0f,
              //  mallet
                   0f, -0.4f, 0f, 1.25f, 0f, 0f, 1f,
                   0f,  0.4f, 0f, 1.25f, 1f, 0f, 0f
        };
        /*float[] tableVerticesWithTriangles = {
                0f, 0f,
                -0.5f, -0.5f,
                0.5f, -0.5f,
                0.5f, 0.5f,
                -0.5f, 0.5f,
                -0.5f, -0.5f,

                -0.5f, 0f,
                0.5f, 0f,

                0f, -0.25f,
                0f, 0.25f
        };*/
        mVertexData = ByteBuffer.allocateDirect(tableVerticesWithTriangles.length*BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexData.put(tableVerticesWithTriangles);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        String vertexShaderSource = TextResourceReader.readTextFileFromResource(mContext, R.raw.simple_vertext_shader);
        String fragmentShaderSource = TextResourceReader.readTextFileFromResource(mContext, R.raw.simple_fragment_shader);

        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);

        mProgram = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        if(LoggerConfig.ON){
            ShaderHelper.validateProgram(mProgram);
        }
        glUseProgram(mProgram);

        //mUColorLocation = glGetUniformLocation(mProgram, U_COLOR);
        mAColorLocation = glGetAttribLocation(mProgram, A_COLOR);
        mAPositionLocation = glGetAttribLocation(mProgram, A_POSITION);
        mUMatrixLocation = glGetUniformLocation(mProgram, U_MATRIX);

        mVertexData.position(0);
        glVertexAttribPointer(mAPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, STRIDE,  mVertexData);
        glEnableVertexAttribArray(mAPositionLocation);


        mVertexData.position(POSITION_COMPONENT_COUNT);
        glVertexAttribPointer(mAColorLocation, COLOR_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, mVertexData);
        glEnableVertexAttribArray(mAColorLocation);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);

        // create a perspective projection with a field of vision of 45 degrees
        // frustum will begin af z of -1f and will end at a z of -10f
        MatrixHelper.perspectiveM(mProjectionMatrix, 45, (float)width / (float)height, 1f, 10f);

        // set ModelMatrix to identity matrix and translate by -2f along  z axis
        setIdentityM(mModelMatrix, 0);
        translateM(mModelMatrix, 0, 0f, 0f, -2.5f);
        rotateM(mModelMatrix, 0, -60f, 1f, 0f, 0f);

        // matrix multiplication order
        // vertex_clip = ProjectionMatrix * ModelMatrix * vertex_model
        // multiply matrices and save at temp
        // then copy the content temp back to mProjectionMatrix
        final float[] temp = new float[16];
        multiplyMM(temp, 0, mProjectionMatrix, 0, mModelMatrix, 0);
        System.arraycopy(temp, 0, mProjectionMatrix, 0, temp.length);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);

        glUniformMatrix4fv(mUMatrixLocation, 1, false, mProjectionMatrix, 0);

        // draw white table
        //glUniform4f(mUColorLocation, 1.0f, 1.0f, 1.0f, 1.0f);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);

        // drawing center red line
        //glUniform4f(mUColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_LINES, 6, 2);

        // draw blue mallet
        //glUniform4f(mUColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
        glDrawArrays(GL_POINTS, 8, 1);

        // draw red mallet
        //glUniform4f(mUColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_POINTS, 9, 1);

    }
}
