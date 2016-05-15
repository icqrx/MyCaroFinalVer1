package my.quoc.caro_final;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;
	
public class ComputerActivity extends Activity implements OnClickListener{
	private myThread threadComputer = null;
	private Boolean finish=false;
	private Board board;
	private BoardUI boardUI;
	private ZoomControls zoomControls;
	private Handler handler = new Handler();
	private HumanPlayer humanPlayer;
	private AlertDialog dialog;
	private Dialog dialog1;
    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    private ImageButton btn1NewGame;
    private ImageButton btn1Back;
    int mode = NONE;
	private static final int ID_NEWGAME = 1;
	private MediaPlayer mediaX,mediaO;
	private Button btnDialogYes, btnDialogNo;
	private TextView message;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.board_layout);
		// quickAction
		ActionItem addItem 	= new ActionItem(ID_NEWGAME, "NewGame", getResources().getDrawable(R.drawable.newgame_caro));
		final QuickAction mQuickAction 	= new QuickAction(this);
		mQuickAction.addActionItem(addItem);
		mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
			
			public void onItemClick(my.quoc.caro_final.QuickAction quickAction, int pos,
					int actionId) {
				// TODO Auto-generated method stub
				ActionItem actionItem = quickAction.getActionItem(pos);
				if (actionId == ID_NEWGAME) {
					//Toast.makeText(getApplicationContext(), "Add item selected", Toast.LENGTH_SHORT).show();
					Play();	
				
				} else {
					Toast.makeText(getApplicationContext(), actionItem.getTitle() + " selected", Toast.LENGTH_SHORT).show();
				}
			}
		});
		// Sound
		mediaX = MediaPlayer.create(this, R.raw.d);
		mediaO = MediaPlayer.create(this, R.raw.go);
		//
		dialog1 = new Dialog(this,R.style.FullHeightDialog);	
		dialog1.setContentView(R.layout.custom_dialog);
		btnDialogYes = (Button)dialog1.findViewById(R.id.bmessageDialogYes);
		btnDialogNo = (Button)dialog1.findViewById(R.id.bmessageDialogNo);
		message = (TextView)dialog1.findViewById(R.id.tvmessagedialogtext);
		
		dialog1.setCancelable(true);
		
		//
		btn1NewGame = (ImageButton)findViewById(R.id.btn1NewGame);
		btn1NewGame.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mQuickAction.show(v);
			}
		});
		btn1Back = (ImageButton)findViewById(R.id.btn1Undo);
		btn1Back.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(finish==false){
			    	board.undo(2);
			    	boardUI.update();
		    	}
			}
		});
		//
		board = new BoardAI();
		boardUI = new BoardUI(this, board);
		zoomControls = new ZoomControls(this);
	
		RelativeLayout layout = (RelativeLayout)findViewById(R.id.RelativeLayout1);
		RelativeLayout.LayoutParams param1 = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,710);
		layout.addView(boardUI,param1);
		
		RelativeLayout.LayoutParams param2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		param2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		param2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		layout.addView(zoomControls,param2);
	
        //setContentView(layout);
        zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				boardUI.setZoom(boardUI.getZoom() + 0.5f);
			}
		});
        zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				boardUI.setZoom(boardUI.getZoom() - 0.5f);
			}
		});
        dialog = new AlertDialog.Builder(this).create();
        dialog.setCancelable(false);
        //Play();
        // default player first play.
    	// human player start ,...
		humanPlayer = new HumanPlayer(board, Board.BLACK, boardUI);
		humanPlayer.addMoveListener(new MoveListener() {		
			public void moveMade(Move move) {
				// TODO Auto-generated method stub
				mediaO.start();
				updateMove(move);
			}
		}) ;
		
        board.setCurrentPiece(Board.BLACK); // X
		threadComputer = new myThread(doBackgroundThreadProcessing);
		threadComputer.start();
	}
	
	void updateMove(Move m){
		boardUI.update();	
		byte victory = board.victory(); // check win
			switch (victory) {
			case Board.BLACK_WIN:
				threadComputer.requestStop();
				dialog.setTitle("");
				dialog.setMessage("X Win!");
				dialog.setButton("OK", new DialogInterface.OnClickListener() {
				      public void onClick(DialogInterface dialog, int which) {
				    	  handler.post(new Runnable() {
								public void run() {
									boardUI.update();	
								}
							});
				    } });
				dialog.show();
				// continue if x win, fix again ****** uncomplete! <====
				board.setCurrentPiece(Board.BLACK);
				//threadComputer = new myThread(doBackgroundThreadProcessing);
				//threadComputer.start();
			break;
		case Board.WHITE_WIN:
			threadComputer.requestStop();
			Log.w("O thang", "O thang");
			dialog.setTitle("");
			dialog.setMessage("O Win!");
			dialog.setButton("OK", new DialogInterface.OnClickListener() {
			      public void onClick(DialogInterface dialog, int which) {
			    	  handler.post(new Runnable() {
							public void run() {
								boardUI.update();	
							}
						});
			    } });
			handler.post(new Runnable() {
				public void run() {
					dialog.show();
				}
			});
			// to be continue, add listener of player ok !
			board.setCurrentPiece(Board.BLACK);
			threadComputer = new myThread(doBackgroundThreadProcessing);
			threadComputer.start();
			break;
		case Board.DRAW: // hoa`
			threadComputer.requestStop();
			dialog.setTitle("");
			dialog.setMessage("Equal!");
			dialog.setButton("OK", new DialogInterface.OnClickListener() {
			      public void onClick(DialogInterface dialog, int which) {
			    	  handler.post(new Runnable() {
							public void run() {
								boardUI.update();
							}
						});
			    } });
			dialog.show();
			//
			board.setCurrentPiece(Board.BLACK);
			threadComputer = new myThread(doBackgroundThreadProcessing);
			threadComputer.start();
			break;
			default:
				board.setCurrentPiece(Board.opponentPiece(board.getCurrentPiece()));
		}
	}
	
	private void Play(){		
		boardUI.center();
		finish = false;
		board.clear();
		boardUI.update();
		if (threadComputer != null)
			threadComputer.requestStop();
		/*AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle("");
		dialogBuilder.setMessage("YES to AI first play!");
		dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				board.setCurrentPiece(Board.WHITE);
				threadComputer = new myThread(doBackgroundThreadProcessing);
				threadComputer.start();
			}
		});
		dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				board.setCurrentPiece(Board.BLACK);
				threadComputer = new myThread(doBackgroundThreadProcessing);
				threadComputer.start();
			}
		});
		AlertDialog option = dialogBuilder.create();
		//option.setContentView(R.layout.custom_dialog);
		option.setCancelable(false);
		option.show();*/
		
		message.setText("YES to AI first play!");		
		btnDialogYes.setOnClickListener(this);
		btnDialogNo.setOnClickListener(this);

		dialog1.show();
	}
	
	private Runnable doBackgroundThreadProcessing = new Runnable() {
		
		public void run() {
			// TODO Auto-generated method stub
			if (board.getCurrentPiece() == Board.WHITE) {
				Move m = board.FindMove(Board.WHITE);
				// Move m=BoardAB.FindMove(Board.WHITE, board.getWidth(),
				// board.getMoveHistory());
				mediaX.start();
				board.setPiece(m.getRow(), m.getColumn(), m.getPiece());
				boardUI.update();
				updateMove(m);
				// //////////////////////////////////////////////////////////////////////////////

			}//else{
				//mediaX.start();
				
			//}
		}
	};
	class myThread extends Thread {
		// Must be volatile:
		public myThread(Runnable a) {
			super(a);
		}

		private volatile boolean stop = false;
		private volatile boolean pause = false;

		public void run() {
			while (stop == false && finish == false) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
				if (pause == true)
					continue;
				super.run();
			}
		}

		public void requestStop() {
			stop = true;
		}

		public void setPause(boolean i) {
			pause = i;
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		boardUI.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_MENU){
			Intent intent = new Intent(ComputerActivity.this, CustomMenuActivity.class);
			startActivityForResult(intent, 1);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (resultCode == RESULT_OK && requestCode == 1) {
		    	String check =  data.getExtras().getString("NEW");
		    	if(check.equals("1")){
		    		Play();		    		
		    	}else{
		    		if(check.equals("2")){
		    			if(finish==false){
					    	board.undo(2);
					    	boardUI.update();
				    	}	    			
		    		}
		    		
		    	}
		  }
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
			case R.id.bmessageDialogYes:{
				board.setCurrentPiece(Board.WHITE);
				threadComputer = new myThread(doBackgroundThreadProcessing);
				threadComputer.start();
				dialog1.dismiss();
				break;				
			}
			case R.id.bmessageDialogNo:{
				board.setCurrentPiece(Board.BLACK);
				threadComputer = new myThread(doBackgroundThreadProcessing);
				threadComputer.start();
				dialog1.dismiss();
				break;				
			}
		
		}
	}
	

	
}
