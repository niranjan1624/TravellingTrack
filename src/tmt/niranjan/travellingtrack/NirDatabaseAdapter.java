package tmt.niranjan.travellingtrack;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NirDatabaseAdapter {
	NirHelper helper;
	public NirDatabaseAdapter(Context context){
		helper=new NirHelper(context);
		
	}

	public long insertDetails(String name ,String email,String url){
		SQLiteDatabase db=helper.getWritableDatabase();
		ContentValues contentvalues1=new ContentValues();
		contentvalues1.put(NirHelper.N_NAME,name);
		contentvalues1.put(NirHelper.Email, email);
		contentvalues1.put(NirHelper.PhotoUrl, url);
		long id=db.insert(NirHelper.T_NAME, null, contentvalues1);
		
		return id;
				
	}
	
	public String getName(){
		SQLiteDatabase db=helper.getWritableDatabase();
		String[] columns={NirHelper.N_NAME};
		Cursor cursor=db.query(NirHelper.T_NAME, columns, null,null,null,null,null);
		String name="";
		while(cursor.moveToNext()){
			int index1=cursor.getColumnIndex(NirHelper.N_NAME);
			name=cursor.getString(index1);
			
		}
		return name;
	}
	public String getEmail(){
		SQLiteDatabase db=helper.getWritableDatabase();
		String[] columns={NirHelper.Email};
		Cursor cursor=db.query(NirHelper.T_NAME, columns, null,null,null,null,null);
		
		String email="";
		while(cursor.moveToNext()){
			int index1=cursor.getColumnIndex(NirHelper.Email);
			email=cursor.getString(index1);
		}
		return email;
	}
	public String getphoto(){
		SQLiteDatabase db=helper.getWritableDatabase();
		String[] columns={NirHelper.PhotoUrl};
		Cursor cursor=db.query(NirHelper.T_NAME, columns, null,null,null,null,null);
		String url="";
		while(cursor.moveToNext()){
			int index1=cursor.getColumnIndex(NirHelper.PhotoUrl);
			 url=cursor.getString(index1);
			
		}
		return url;
	}
	
	public String getAddress(){
		SQLiteDatabase db=helper.getWritableDatabase();
		String[] columns={NirHelper.Address};
		Cursor cursor=db.query(NirHelper.TABLE_NAME, columns, null,null,null,null,null);
		String name="";
		while(cursor.moveToNext()){
			int index1=cursor.getColumnIndex(NirHelper.Address);
			name=cursor.getString(index1);
		}
		return name;
	}
	
	public long insertData(String activity,String date,String time,String loc,String address ){
		SQLiteDatabase db=helper.getWritableDatabase();
		ContentValues contentvalues=new ContentValues();
		contentvalues.put(NirHelper.Location,loc);
		contentvalues.put(NirHelper.Activity,activity);
		contentvalues.put(NirHelper.Date,date);
		contentvalues.put(NirHelper.Time,time);
		contentvalues.put(NirHelper.Address,address);
		long id=db.insert(NirHelper.TABLE_NAME, null, contentvalues);
		return id;
				
	}
	public String getalldata(){
		SQLiteDatabase db=helper.getWritableDatabase();
		String[] columns={NirHelper.UID,NirHelper.Date,NirHelper.Time,
				NirHelper.Activity,NirHelper.Location ,NirHelper.Address};
		Cursor cursor=db.query(NirHelper.TABLE_NAME, columns, null,null,null,null,null);
		
		StringBuffer buffer=new StringBuffer();
		while(cursor.moveToNext()){
			int index1=cursor.getColumnIndex(NirHelper.UID);
			int cid=cursor.getInt(index1);
			String date=cursor.getString(1);
			String time=cursor.getString(2);
			String act=cursor.getString(3);
			String loc=cursor.getString(4);					
			String address=cursor.getString(5);
			buffer.append(cid+" "+date+" "+time+" "+act+" "+loc+" "+address+"\n");
			
		}
		
				
		return buffer.toString();
	}
	public String getdata(String name,String uid){
		SQLiteDatabase db=helper.getWritableDatabase();
		String[] columns={NirHelper.Email};
		String[] selectionArgs={name,uid};
		Cursor cursor=db.query(NirHelper.TABLE_NAME, columns, NirHelper.NAME+"=? AND "+NirHelper.UID+"=?",selectionArgs,null,null,null,null);
		StringBuffer buffer=new StringBuffer();
		
		while(cursor.moveToNext()){
			int index0=cursor.getColumnIndex(NirHelper.Email);
			String mac=cursor.getString(index0);
			buffer.append(mac+"\n");
		}
		return buffer.toString();
	}
	static class NirHelper extends SQLiteOpenHelper{
		private static final String DATABASE_NAME="TRACKER";
		private static final String TABLE_NAME="DETAILS";
		private static final String NAME="Name";
		private static final String UID="_id";
		private static final String Email="email";
		private static final String Date ="Date";
		private static final String Activity="Activity";
		private static final String Address = "Address";
		private static final String Time="Time";
		private static final String T_NAME="Name";
		private static final String N_NAME="Name2";
		private static final String U_UID="_id1";
		private static final String PhotoUrl="Photo";
		private static final String Location="Location";

		public static final String CREATE_TABLE="CREATE TABLE "+TABLE_NAME+
				" ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
				Date+" VARCHAR(255),"+
				Time+" VARCHAR(255),"+
				Activity+" VARCHAR(255),"+
				Location+" VARCHAR(255),"+
				Address+" VARCHAR(255));";
		public static final String CREATE_TABLE1="CREATE TABLE "+T_NAME+" ("+U_UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ ""+Email+" VARCHAR(255),"+
				N_NAME+" VARCHAR(255),"+
				PhotoUrl+" VARCHAR(255));";
		private Context context;
		private static final String DROP_TABLE="DROP TABLE IF EXISTS "+TABLE_NAME;
		
		public NirHelper(Context context){
			super(context,DATABASE_NAME,null,17);
			this.context=context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			try {
				db.execSQL(CREATE_TABLE);
				db.execSQL(CREATE_TABLE1);
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Use.message(context, ""+e);
			}
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			try {
				Use.message(context, "Upgradding");
				db.execSQL(DROP_TABLE);
				onCreate(db);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Use.message(context, ""+e);
			}
			


		}
		
	}


}
