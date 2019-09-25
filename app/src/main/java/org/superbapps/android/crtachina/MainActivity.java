package org.superbapps.android.crtachina;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

	DrawingView drawingView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		drawingView = new DrawingView( this );
		setContentView( drawingView );
	}

}