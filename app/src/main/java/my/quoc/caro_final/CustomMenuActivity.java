package my.quoc.caro_final;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

public class CustomMenuActivity extends Activity{
    private ImageButton btnNew;
    private ImageButton btnUndo;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.custom_menu);
		
		btnNew = (ImageButton)findViewById(R.id.NewGame1);
		btnNew.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.putExtra("NEW","1");
				setResult(RESULT_OK, intent);
				finish();
			}
		});
		btnUndo = (ImageButton)findViewById(R.id.Undo1);
		btnUndo.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.putExtra("NEW","2");
				setResult(RESULT_OK, intent);
				finish();
			}
		});
	}

}
