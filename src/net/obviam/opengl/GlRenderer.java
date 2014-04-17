package net.obviam.opengl;


/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.hardware.Camera.PreviewCallback;



class  GlRenderer extends Activity implements GLSurfaceView.Renderer {
	
	private static SharedPreferences.Editor editor;
	
	
    public  GlRenderer(Context context) {
        mContext = context;
        mTriangleVertices = ByteBuffer.allocateDirect(mVerticesData.length
                * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(mVerticesData).position(0);
		mTextureCoordinates = ByteBuffer.allocateDirect(TextureCoordinateData.length *4)
		.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mTextureCoordinates.put(TextureCoordinateData).position(0);

    }
    
    public void onDrawFrame(GL10 glUnused) {
        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
    	GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        
        if(mRight==true) 	
        {
           cont++;
           mRight=false;
        }
        
        if(mLeft==true) 	
        {
           cont--;
           mLeft=false;
        }
           
        if(cont<0||cont>4) 	
        {
           cont=0;
        }
        
        if(cont==0) 	
        {
         //startTime0x0=SystemClock.uptimeMillis() % 1000;
          GLES20.glUseProgram(mProgram_image);
         //endTime0x0=SystemClock.uptimeMillis() % 1000;
         //deltaTime0x0 = endTime3x3 - startTime3x3;  
         //Log.e("TIME", "TIME 0x0 KERNEL:" +  Long.toString(deltaTime0x0));  
        }
        
        if(cont==1) 	
        {
          //startTime3x3=SystemClock.uptimeMillis() % 1000;
          GLES20.glUseProgram(mProgram_3x3);
          //endTime3x3=SystemClock.uptimeMillis() % 1000;
          //deltaTime3x3 = endTime3x3 - startTime3x3;  
          //Log.e("TIME", "TIME FOR 3x3 KERNEL:" +  Long.toString(deltaTime3x3));  
        }
        
       if(cont==2) 	
       {
       	 GLES20.glUseProgram(mProgram_5x5);
       }
       
       if(cont==3) 	
       {
       	 GLES20.glUseProgram(mProgram_7x7);
       }
        
        
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);

        mTriangleVertices.position(0);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                0, mTriangleVertices);
        
        mTextureCoordinates.position(0);
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false,
                0, mTextureCoordinates);
        GLES20.glEnableVertexAttribArray(maTextureHandle);

     
        float angle = 0.090f ;
        Matrix.setRotateM(mMMatrix, 0, angle, 0, 0, 1.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        
        float array[] = {0.0f,  1.0f, 0.0f,
        				 1.0f, -4.0f, 1.0f,
						 0.0f,  1.0f, 0.0f};
        
        float hight_array[]={hight};
        float wight_array[]={wight};
        
        GLES20.glUniform1fv(muKernelMatrixHandle,9, array,0);
        
        GLES20.glUniform1fv(HightHandle,1,hight_array,0);
        GLES20.glUniform1fv(WightHandle,1,wight_array,0);
        
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVerticesData.length/3);
        checkGlError("glDrawArrays");
    }

    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
    	
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }
    
 
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
    	
        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
    	
        mProgram_image = createProgram(mVertexShader, mFragmentShader_image);
        mProgram_3x3= createProgram(mVertexShader, mFragmentShader_3x3);
        mProgram_5x5= createProgram(mVertexShader, mFragmentShader_5x5);
        mProgram_7x7= createProgram(mVertexShader, mFragmentShader_7x7);

        
        if (mProgram_image == 0||mProgram_3x3==0||mProgram_5x5==0||mProgram_7x7==0) {
        return;
        }
        maPositionHandle = GLES20.glGetAttribLocation(mProgram_image, "aPosition");
        maTextureHandle = GLES20.glGetAttribLocation(mProgram_image, "aTextureCoord");
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram_image, "uMVPMatrix");
        muKernelMatrixHandle = GLES20.glGetUniformLocation(mProgram_image, "KernelMatrix");
        HightHandle = GLES20.glGetUniformLocation(mProgram_5x5, "hight");
        WightHandle =  GLES20.glGetUniformLocation(mProgram_5x5, "wight");

        /*
         * Create our texture. This has to be done each time the
         * surface is created.
         */
 
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        mTextureID = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);

        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        
        bitmap=this.image;
       
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
             
        bitmap.recycle();
       
        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
    

    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
    
    private final float[] mVerticesData = {
			-1.0f, -1.0f,  0.0f,		// V1 - bottom left
			-1.0f,  1.0f,  0.0f,		// V2 - top left
			 1.0f, -1.0f,  0.0f,		// V3 - bottom right
			 1.0f,  1.0f,  0.0f			// V4 - top right
	};
    
    private float TextureCoordinateData[] = {    		
    		// Mapping coordinates for the vertices
    		0.0f, 1.0f,		// top left		(V2)
    		0.0f, 0.0f,		// bottom left	(V1)
    		1.0f, 1.0f,		// top right	(V4)
    		1.0f, 0.0f		// bottom right	(V3)
    };

    private FloatBuffer mTriangleVertices;
    private final FloatBuffer mTextureCoordinates;

    private final String mVertexShader =
        "uniform mat4 uMVPMatrix;\n" +
        "attribute vec4 aPosition;\n" +
        "attribute vec2 aTextureCoord;\n" +
        "varying vec2 vTextureCoord;\n" +
        "void main() {\n" +
        "gl_Position = uMVPMatrix * aPosition;\n" +
        "vTextureCoord = aTextureCoord;\n" +
        "}\n";
    
    private final String mFragmentShader_3x3 =
        "precision mediump float;\n" +
        "varying vec2 vTextureCoord;\n" +
        "float KernelMatrix[9];\n" +
        "float hight[1];\n" +
        "float wight[1];\n" +
        "float step_w = 0.01;\n" +
        "float step_h = 0.01;\n" +  
        "int KernelSize = KernelMatrix.length();\n" +
        "uniform sampler2D sTexture;\n" +
        "int i=0;\n" +        
        "void main() {\n" +
        "gl_FragColor = vec4(0.0);\n" +
        "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-step_w, -step_h))* 0.0;\n"+
        "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(0.0, -step_h))* 1.0;\n"+
        "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(step_w, -step_h))* 0.0 ;\n"+
        "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-step_w, 0.0))*1.0;\n"+
        "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(0.0, 0.0))*-4.0;\n"+
        "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(step_w, 0.0))* 1.0;\n"+
        "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-step_w,step_h))*0.0;\n"+
        "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(0.0, step_h))*1.0;\n"+
        "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(step_w, step_h))* 0.0;\n"+ 
        "}\n";
    
    private final String mFragmentShader_image =
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform sampler2D sTexture;\n" +    
            "void main() {\n" +
            "gl_FragColor = texture2D(sTexture,vTextureCoord);\n"+
            "}\n";

    private final String mFragmentShader_5x5 =
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "float KernelMatrix[9];\n" +
            "float hight[1];\n" +
            "float wight[1];\n" +  
            "int KernelSize = KernelMatrix.length();\n" +
            "uniform sampler2D sTexture;\n" +
                    
            "void main() {\n" +
             "gl_FragColor = vec4(0.0);\n" +
            
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-2.0/ 1024.0,-2.0/768.0)) * -1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-1.0/1024.0,-2.0/768.0)) *-4.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(0.0,-2.0/768.0))*-6.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(1.0/1024.0,-2.0/768.0))*-4.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(2.0/1024.0,-2.0/768.0)) *-1.0;\n"+
            
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-2.0/1024.0,-1.0/768.0))*-2.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-1.0/1024.0,-1.0/768.0))*-8.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(0.0,-1.0/768.0))*-12.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(1.0/1024.0,-1.0/768.0))*-8.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(2.0/1024.0,-1.0/768.0))*-2.0;\n"+
             
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-2.0/1024.0,0.0))* 0.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-1.0/1024.0,0.0))*0.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(0.0,0.0))* 0.0 ;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(1.0/1024.0,0.0))* 0.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(2.0/1024.0,0.0))* 0.0;\n"+
             
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-2.0/1024.0,1.0/768.0))*2.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-1.0/1024.0,1.0/768.0))*8.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(0.0,1.0/768.0))*12.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(1.0f/1024.0,1.0/768.0))*8.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(2.0/1024.0,1.0/ 768.0))*2.0;\n"+
             
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-2.0/1024.0,2.0/768.0)) *1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-1.0/1024.0,2.0/768.0)) * 4.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(0.0,2.0/768.0)) * 6.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(1.0/1024.0,2.0/768.0)) *4.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(2.0/1024.0,2.0/ 768.0)) *1.0;\n"+
             "}\n";
    

    private final String mFragmentShader_7x7 =
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "float KernelMatrix[9];\n" +
            "float step_w = 0.01;\n" +
            "float step_h = 0.01;\n" +   
            "float delta  = 0.0;\n" +   
            "int KernelSize = KernelMatrix.length();\n" +
            "uniform sampler2D sTexture;\n" +
                    
            "void main() {\n" +
             "gl_FragColor = vec4(0.0);\n" +
            
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-3.0/1024.0,-3.0/768.0)) * -1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-2.0/1024.0,-3.0/768.0)) *-1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-1.0/1024.0,-3.0/768.0))*-1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(0.0,-3.0/768.0f))*-1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(1.0/1024.0f,-3.0/768.0f)) *-1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(2.0/1024.0,-3.0/768.0)) *-1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(3.0/1024.0,-3.0/768.0)) *-1.0;\n"+
             
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-3.0f/1024.0,-2.0/768.0)) * -1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-2.0/1024.0,-2.0/768.0)) *-1.0;\n"+ 
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-1.0/1024.0,-2.0/768.0))*-1.0;\n"+ 
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(0.0,-2.0/768.0))*-1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(1.0/1024.0,-2.0/768.0)) *-1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(2.0/1024.0,-2.0/768.0)) *-1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(3.0/1024.0,-2.0/768.0)) *-1.0;\n"+
             
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-3.0/1024.0,-1.0f/768.0)) * -1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-2.0/1024.0,-1.0/768.0)) *-1.0;\n"+ 
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-1.0/1024.0,-3.0/768.0))*-1.0;\n"+ 
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(0.0,-1.0/768.0))*-1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(1.0/1024.0,-1.0/768.0)) *-1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(2.0/1024.0,-1.0/768.0)) *-1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(3.0/1024.0,-1.0/768.0)) *-1.0;\n"+
             
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-3.0/1024.0,0.0)) * -1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-2.0/1024.0,0.0)) *-1.0;\n"+ 
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-1.0/1024.0,0.0))*-1.0;\n"+ 
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(0.0,0.0))*48.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(1.0/1024.0,0.0)) *-1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(2.0/1024.0,-3.0/768.0)) *-1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(3.0/1024.0,-3.0/768.0)) *-1.0;\n"+
            
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-3.0/1024.0,1.0f/768.0)) * -1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-2.0/1024.0,1.0/768.0)) *-1.0;\n"+ 
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-1.0/1024.0,1.0/768.0))*-1.0;\n"+ 
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(0.0,1.0/768.0))*-1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(1.0/1024.0,1.0/768.0)) *-1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(2.0/1024.0,1.0/768.0)) *-1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(3.0/1024.0,1.0/768.0)) *-1.0;\n"+
           
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-3.0/1024.0,2.0/768.0)) * -1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-2.0/1024.0,2.0/768.0)) *-1.0;\n"+ 
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-1.0/1024.0,2.0/768.0))*-1.0;\n"+ 
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(0.0,2.0/768.0))*-1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(1.0/1024.0,2.0/768.0)) *-1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(2.0/1024.0,2.0/768.0)) *-1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(3.0/1024.0,2.0/768.0)) *-1.0;\n"+
             
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-3.0/1024.0,3.0/768.0)) * -1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-2.0/1024.0,3.0/768.0)) *-1.0;\n"+ 
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(-1.0/1024.0,3.0/768.0))*-1.0;\n"+ 
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(0.0,3.0/768.0))*-1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(1.0/1024.0,3.0/768.0)) *-1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(2.0/1024.0,3.0/768.0)) *-1.0;\n"+
             "gl_FragColor +=  texture2D(sTexture,vTextureCoord+vec2(3.0/1024.0,3.0f/768.0)) *-1.0;\n"+             
             "}\n";
    
    
    
    private float [] mMVPMatrix = new float[16];
    private float [] mProjMatrix = new float[16];
    private float [] mMMatrix = new float[16];
    private float [] mVMatrix = new float[16];
    public boolean mStart=false;
    public boolean mRight=false;
    public boolean mLeft=false;
 	float hight= 0f;
	float wight = 0f;

	
    private int mProgram_image;
    private int cont=0;
    private int mProgram_3x3;
    private int mProgram_5x5;
    private int mProgram_7x7;
    private int mTextureID;
    private long deltaTime0x0,startTime0x0,endTime0x0;
    private long deltaTime3x3,startTime3x3,endTime3x3;
    private int muMVPMatrixHandle;
    private int muKernelMatrixHandle;
    private int HightHandle;
    private int WightHandle;
    private int maPositionHandle;
    private int maTextureHandle;
    public Bitmap  image=null; 
    private Bitmap bitmap=null;
    private Context mContext;
    TextView textView;
    Bitmap bitmaptext;
    private static String TAG = "GLES20TriangleRenderer";
}
