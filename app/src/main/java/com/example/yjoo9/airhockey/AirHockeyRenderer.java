package com.example.yjoo9.airhockey;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.example.yjoo9.airhockey.android.util.LoggerConfig;
import com.example.yjoo9.airhockey.android.util.ShaderHelper;
import com.example.yjoo9.firstopengl.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

public class AirHockeyRenderer implements GLSurfaceView.Renderer {
    private static final String U_COLOR = "u_Color";
    private static final String A_POSITION = "a_Position";
    private static final int POSITION_COMPONENT_COUNT=2;
    private static final int BYTES_PER_FLOAT = 4;
    private final FloatBuffer mVertexData;
    private final Context mContext;

    private int mProgram;
    private int mUColorLocation;
    private int mAPosition;

    public AirHockeyRenderer(Context context){
        this.mContext = context;
        float[] tableVerticesWithTriangles = {
                0f, 0f,
                9f, 14f,
                0f, 14,

                0f, 14f,
                9f, 0f,
                9f, 14f,

                0f, 7f,
                9f, 7f,

                4.5f, 2f,
                4.5f, 12f
        };
        mVertexData = ByteBuffer.allocateDirect(tableVerticesWithTriangles.length*BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexData.put(tableVerticesWithTriangles);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0.0f, 1.0f, 0.0f, 0.0f);
        String vertexShaderSource = TextResourceReader.readTextFileFromResource(mContext, R.raw.simple_vertext_shader);
        String fragmentShaderSource = TextResourceReader.readTextFileFromResource(mContext, R.raw.simple_fragment_shader);

        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);

        mProgram = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        if(LoggerConfig.ON){
            ShaderHelper.validateProgram(mProgram);
        }
        glUseProgram(mProgram);

        mUColorLocation = glGetUniformLocation(mProgram, U_COLOR);
        mAPosition = glGetAttribLocation(mProgram, A_POSITION);

        mVertexData.position(0);
        glVertexAttribPointer(mAPosition, POSITION_COMPONENT_COUNT, GL_FLOAT, false, 0, mVertexData);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
    }
}
