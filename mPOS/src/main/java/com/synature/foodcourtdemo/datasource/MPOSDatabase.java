package com.synature.foodcourtdemo.datasource;

import java.util.UUID;

import com.synature.foodcourtdemo.MPOSApplication;
import com.synature.foodcourtdemo.datasource.table.BankTable;
import com.synature.foodcourtdemo.datasource.table.BaseColumn;
import com.synature.foodcourtdemo.datasource.table.CashInOutOrderDetailTable;
import com.synature.foodcourtdemo.datasource.table.CashInOutOrderTransTable;
import com.synature.foodcourtdemo.datasource.table.CashInOutProductTable;
import com.synature.foodcourtdemo.datasource.table.ComputerTable;
import com.synature.foodcourtdemo.datasource.table.CreditCardTable;
import com.synature.foodcourtdemo.datasource.table.GlobalPropertyTable;
import com.synature.foodcourtdemo.datasource.table.HeaderFooterReceiptTable;
import com.synature.foodcourtdemo.datasource.table.LanguageTable;
import com.synature.foodcourtdemo.datasource.table.MaxOrderIdTable;
import com.synature.foodcourtdemo.datasource.table.MaxPaymentIdTable;
import com.synature.foodcourtdemo.datasource.table.MaxTransIdTable;
import com.synature.foodcourtdemo.datasource.table.MenuFixCommentTable;
import com.synature.foodcourtdemo.datasource.table.OrderDetailTable;
import com.synature.foodcourtdemo.datasource.table.OrderTransTable;
import com.synature.foodcourtdemo.datasource.table.PayTypeFinishWasteTable;
import com.synature.foodcourtdemo.datasource.table.PayTypeTable;
import com.synature.foodcourtdemo.datasource.table.PaymentButtonTable;
import com.synature.foodcourtdemo.datasource.table.PaymentDetailTable;
import com.synature.foodcourtdemo.datasource.table.PaymentDetailWasteTable;
import com.synature.foodcourtdemo.datasource.table.PrintReceiptLogTable;
import com.synature.foodcourtdemo.datasource.table.ProductComponentGroupTable;
import com.synature.foodcourtdemo.datasource.table.ProductComponentTable;
import com.synature.foodcourtdemo.datasource.table.ProductDeptTable;
import com.synature.foodcourtdemo.datasource.table.ProductGroupTable;
import com.synature.foodcourtdemo.datasource.table.ProductPriceTable;
import com.synature.foodcourtdemo.datasource.table.ProductTable;
import com.synature.foodcourtdemo.datasource.table.ProgramFeatureTable;
import com.synature.foodcourtdemo.datasource.table.PromotionPriceGroupTable;
import com.synature.foodcourtdemo.datasource.table.PromotionProductDiscountTable;
import com.synature.foodcourtdemo.datasource.table.SessionDetailTable;
import com.synature.foodcourtdemo.datasource.table.SessionTable;
import com.synature.foodcourtdemo.datasource.table.ShopTable;
import com.synature.foodcourtdemo.datasource.table.StaffPermissionTable;
import com.synature.foodcourtdemo.datasource.table.StaffTable;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 
 * @author j1tth4
 * 
 */
public class MPOSDatabase extends BaseColumn{
	
	public static final int NOT_SEND = 0;
	public static final int ALREADY_SEND = 1;
	
	public static final int NOT_DELETE = 0;
	public static final int DELETED = 1;
	
	protected Context mContext;
	private MPOSOpenHelper mHelper;
	
	public MPOSDatabase(Context context){
		mContext = context;
		mHelper = MPOSOpenHelper.getInstance(context); 
	}
	
	public Context getContext(){
		return mContext;
	}
	
	public String getUUID(){
		return UUID.randomUUID().toString();
	}
	
	public SQLiteDatabase getWritableDatabase(){
		return mHelper.getWritableDatabase();
	}
	
	public SQLiteDatabase getReadableDatabase(){
		return mHelper.getReadableDatabase();
	}
	
	public static class MPOSOpenHelper extends SQLiteOpenHelper {

		private static MPOSOpenHelper sHelper;
        private static Context sContext;

		/**
		 * @param context
		 * @return SQLiteOpenHelper instance This singleton pattern for only get
		 *         one SQLiteOpenHelper instance for thread save
		 */
		public static synchronized MPOSOpenHelper getInstance(Context context) {
			if (sHelper == null) {
				sHelper = new MPOSOpenHelper(context.getApplicationContext());
			}
			return sHelper;
		}

		private MPOSOpenHelper(Context context) {
			super(context, MPOSApplication.DB_NAME, null, MPOSApplication.DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			MaxTransIdTable.onCreate(db);
			MaxOrderIdTable.onCreate(db);
			MaxPaymentIdTable.onCreate(db);
			BankTable.onCreate(db);
			ComputerTable.onCreate(db);
			CreditCardTable.onCreate(db);
			GlobalPropertyTable.onCreate(db);
			LanguageTable.onCreate(db);
			HeaderFooterReceiptTable.onCreate(db);
			MenuFixCommentTable.onCreate(db);
			OrderDetailTable.onCreate(db);
			OrderTransTable.onCreate(db);
			PrintReceiptLogTable.onCreate(db);
			PaymentDetailTable.onCreate(db);
			PaymentButtonTable.onCreate(db);
			PayTypeTable.onCreate(db);
			ProductDeptTable.onCreate(db);
			ProductGroupTable.onCreate(db);
			ProductComponentGroupTable.onCreate(db);
			ProductComponentTable.onCreate(db);
			ProductTable.onCreate(db);
			ProductPriceTable.onCreate(db);
			SessionTable.onCreate(db);
			SessionDetailTable.onCreate(db);
			ShopTable.onCreate(db);
			StaffPermissionTable.onCreate(db);
			StaffTable.onCreate(db);
			PromotionPriceGroupTable.onCreate(db);
			PromotionProductDiscountTable.onCreate(db);
			PayTypeFinishWasteTable.onCreate(db);
			ProgramFeatureTable.onCreate(db);
			PaymentDetailWasteTable.onCreate(db);
            CashInOutProductTable.onCreate(db);
            CashInOutOrderTransTable.onCreate(db);
            CashInOutOrderDetailTable.onCreate(db);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            MaxTransIdTable.onUpgrade(db, oldVersion, newVersion);
			MaxOrderIdTable.onUpgrade(db, oldVersion, newVersion);
			MaxPaymentIdTable.onUpgrade(db, oldVersion, newVersion);
			BankTable.onUpgrade(db, oldVersion, newVersion);
			ComputerTable.onUpgrade(db, oldVersion, newVersion);
			CreditCardTable.onUpgrade(db, oldVersion, newVersion);
			GlobalPropertyTable.onUpgrade(db, oldVersion, newVersion);
			LanguageTable.onUpgrade(db, oldVersion, newVersion);
			HeaderFooterReceiptTable.onUpgrade(db, oldVersion, newVersion);
			MenuFixCommentTable.onUpgrade(db, oldVersion, newVersion);
			PrintReceiptLogTable.onUpgrade(db, oldVersion, newVersion);
			PaymentDetailTable.onUpgrade(db, oldVersion, newVersion);
			PaymentButtonTable.onUpgrade(db, oldVersion, newVersion);
			PayTypeTable.onUpgrade(db, oldVersion, newVersion);
			ProductDeptTable.onUpgrade(db, oldVersion, newVersion);
			ProductGroupTable.onUpgrade(db, oldVersion, newVersion);
			ProductComponentGroupTable.onUpgrade(db, oldVersion, newVersion);
			ProductComponentTable.onUpgrade(db, oldVersion, newVersion);
			ProductTable.onUpgrade(db, oldVersion, newVersion);
			ProductPriceTable.onUpgrade(db, oldVersion, newVersion);
			ShopTable.onUpgrade(db, oldVersion, newVersion);
			StaffPermissionTable.onUpgrade(db, oldVersion, newVersion);
			StaffTable.onUpgrade(db, oldVersion, newVersion);
			PromotionPriceGroupTable.onUpgrade(db, oldVersion, newVersion);
			PromotionProductDiscountTable.onUpgrade(db, oldVersion, newVersion);
			OrderTransTable.onUpgrade(db, oldVersion, newVersion);
			OrderDetailTable.onUpgrade(db, oldVersion, newVersion);
			PayTypeFinishWasteTable.onUpgrade(db, oldVersion, newVersion);
			ProgramFeatureTable.onUpgrade(db, oldVersion, newVersion);
			PaymentDetailWasteTable.onUpgrade(db, oldVersion, newVersion);
            CashInOutProductTable.onUpgrade(db, oldVersion, newVersion);
            CashInOutOrderTransTable.onUpgrade(db, oldVersion, newVersion);
            CashInOutOrderDetailTable.onUpgrade(db, oldVersion, newVersion);
		}
	}
	
	public static boolean checkTableExists(SQLiteDatabase db, String tableName){
		boolean isExists = false;
		Cursor cursor = db.rawQuery("select * from sqlite_master "
				+ "where name=? "
				+ "and type=?", 
				new String[]{
						tableName,
						"table"
				});
		try {
			if(cursor.moveToFirst()){
				isExists = true;
			}
		} finally {
			if(cursor != null)
				cursor.close();
		}
		return isExists;
	}
}
