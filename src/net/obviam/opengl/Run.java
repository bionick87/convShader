package net.obviam.opengl;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.hardware.Camera.PreviewCallback;

public class Run extends Activity {
	
	/** The OpenGL view */
	private GLSurfaceView  mRenderer;
	private GlRenderer mRend;
	private static int RESULT_LOAD_IMAGE = 1;
	Bitmap thumbnail = null; 
	Bitmap img=null;
    TextView Text=null;

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main); 
       
        Text= (TextView)findViewById(R.id.dim);
    
    	 
         mRend=new GlRenderer(this);
         
        // Initiate the Open GL view and
        // create an instance with this activity
        mRenderer=(GLSurfaceView) this.findViewById(R.id.glSurface);
        
        // making it full screen
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
	    // loading texture
	     mRend.image= BitmapFactory.decodeResource(this.getResources(),
	    			R.drawable.lena);
         Text.setText("\n"+"\n"+" Image Height: 512.0"+"\n"+" Image Width: 512.0");  

        // Request an OpenGL ES 2.0 compatible context.
		 mRenderer.setEGLContextClientVersion(2);
      
        // set our renderer to be the main renderer with
        // the current activity context
		 mRenderer.setRenderer(mRend);
		 		
        (this.findViewById(R.id.leftButton)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
           	      
            mRend.mLeft=true;	
            
           }});
    
        (this.findViewById(R.id.rightButton)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
             
            mRend.mRight=true;
            
          }});
    }
  
	@SuppressWarnings("unused")
	private static Bitmap codec(Bitmap src, Bitmap.CompressFormat format,
			int quality) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		src.compress(format, quality, os);
 
		byte[] array = os.toByteArray();
		return BitmapFactory.decodeByteArray(array, 0, array.length);
	}
	
	

	/**
	 * Remember to resume the glSurface
	 */
    
	@Override
	protected void onResume() {
		super.onResume();
		 mRenderer.onResume();
	}

	/**
	 * Also pause the glSurface
	 */
	
	@Override
	protected void onPause() {
		super.onPause();
		 mRenderer.onPause();
	}

	
    // Initiating Menu XML file (menu.xml)
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.layout.menu, menu);
        return true;
    }
     
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            img=BitmapFactory.decodeFile(picturePath); 
            float height=img.getHeight();
            float width=img.getWidth();
            Text.setText("\n"+"\n"+" Image Height: "+height+"\n"+" Image Width: "+width);          
            mRend.image  = Bitmap.createScaledBitmap(img,(int)(img.getWidth()*0.8), (int)(img.getHeight()*0.8), true);
         }
      }
    
    
    /**
     * Event Handling for Individual menu item selected
     * Identify single menu item by it's id
     * */
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
         
        switch (item.getItemId())
        {
         case R.id.menu_image_load:
          Intent i = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

         startActivityForResult(i, RESULT_LOAD_IMAGE);

         
         case R.id.menu_loop:
        	
        	 
         return true;
 
         default:
            return super.onOptionsItemSelected(item);
        }
     }    
}

