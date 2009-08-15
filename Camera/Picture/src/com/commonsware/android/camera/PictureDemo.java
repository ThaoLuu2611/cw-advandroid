/***
	Copyright (c) 2008-2009 CommonsWare, LLC
	
	Licensed under the Apache License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may obtain
	a copy of the License at
		http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package com.commonsware.android.camera;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;

public class PictureDemo extends Activity {
	private SurfaceView preview=null;
	private SurfaceHolder previewHolder=null;
	private Camera camera=null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		preview=(SurfaceView)findViewById(R.id.preview);
		previewHolder=preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode==KeyEvent.KEYCODE_CAMERA ||
				keyCode==KeyEvent.KEYCODE_SEARCH) {
			takePicture();
			
			return(true);
		}
		
		return(super.onKeyDown(keyCode, event));
	}
	
	private void takePicture() {
		camera.stopPreview();
		camera.takePicture(null, null, photoCallback);
	}
	
	SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			camera=Camera.open();
			
			try {
				camera.setPreviewDisplay(previewHolder);
			}
			catch (Throwable t) {
				Log.e("PictureDemo-surfaceCallback",
							"Exception in setPreviewDisplay()", t);
				Toast
					.makeText(PictureDemo.this, t.getMessage(), Toast.LENGTH_LONG)
					.show();
			}
		}
		
		public void surfaceChanged(SurfaceHolder holder,
															 int format, int width,
															 int height) {
			Camera.Parameters parameters=camera.getParameters();
			
			parameters.setPreviewSize(width, height);
			parameters.setPictureFormat(PixelFormat.JPEG);
			camera.setParameters(parameters);
			camera.startPreview();
		}
		
		public void surfaceDestroyed(SurfaceHolder holder) {
			camera.stopPreview();
			camera.release();
			camera=null;
		}
	};
	
	Camera.PictureCallback photoCallback=new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			new SavePhotoTask().execute(data);
			camera.startPreview();
		}
	};
	
	class SavePhotoTask extends AsyncTask<byte[], String, String> {
		@Override
		protected String doInBackground(byte[]... jpeg) {
			File photo=new File(Environment.getExternalStorageDirectory(),
													"photo.jpg");
			
			if (photo.exists()) {
				photo.delete();
			}
			
			try {
				FileOutputStream fos=new FileOutputStream(photo.getPath());
				
				fos.write(jpeg[0]);
				fos.close();
			}
			catch (java.io.IOException e) {
				Log.e("PictureDemo", "Exception in photoCallback", e);
			}
			
			return(null);
		}
	}
}
