package org.nhnnext.android.day5_simple;

import java.util.ArrayList;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class MainController {
	// Singleton pattern을 위한 MainController 변수
	private final static MainController instance = new MainController();
	// Database를 관리하기 위한 변수
	private SQLiteDatabase database;

	// ArticleProvider와 중복되지만.. 어디에 두어야할지 모르겠는 변수들
	public static final String AUTHORITY = "org.nhnnext.android.day5_simple.Article";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/Articles");
	private static final String TABLE_NAME = "Articles";
	// Context관리를 위한 변수
	private Context context;
	// Proxy 변수
	private Proxy proxy;

	private MainController() {

		this.proxy = new Proxy();
	}

	public static MainController getInstance() {
		return instance;
	}

	public boolean setContext(Context context) {
		try {
			this.context = context;
		} catch (Exception e) {
			this.context = null;
			return false;
		}

		return true;
	}

	// proxy로부터 Article들을 ArrayList<ContentValue> 가져온 후 DataBase에 insert함 
	public void insertDataToDatabase() {
		String imgName;
		ArrayList<ContentValues> contentValuesArr;

		try {
			contentValuesArr = proxy.getArticlesAsContentValues();
			Img_Downloader imgDownLoader = new Img_Downloader(context);
			for (ContentValues contentValues : contentValuesArr) {
				imgName = contentValues.getAsString("ImgName");
				imgDownLoader.copy_img(MainActivity.SERVER_ADDRESS + "image/"
						+ imgName, imgName);
				context.getContentResolver().insert(CONTENT_URI, contentValues);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// DataBase안에 있는 Article들을 ArrayList로 반환
	public ArrayList<Article> getArticleList() {
		ArrayList<Article> articleList = new ArrayList<Article>();

		int articleNumber;
		String title;
		String writer;
		String id;
		String content;
		String writeDate;
		String imgName;

		if (isTableExist()) {
			Cursor cursor = database.query(TABLE_NAME, null, null, null, null,
					null, "_id");
			if (cursor != null) {
				cursor.moveToFirst();
				while (!(cursor.isAfterLast())) {
					articleNumber = cursor.getInt(1);
					title = cursor.getString(2);
					writer = cursor.getString(3);
					id = cursor.getString(4);
					content = cursor.getString(5);
					writeDate = cursor.getString(6);
					imgName = cursor.getString(7);

					articleList.add(new Article(articleNumber, title, writer,
							id, content, writeDate, imgName));
					cursor.moveToNext();
				}
			}

			cursor.close();
		}

		return articleList;
	}

	// Database안에있는 Article 한개를 articleNumber를 기반으로하여 반
	public Article getArticle(int articleNumber) {

		Article article = null;

		String title;
		String writer;
		String id;
		String content;
		String writeDate;
		String imgName;

		if (isTableExist()) {
			Cursor cursor = database.query(TABLE_NAME, null, "ArticleNumber="
					+ articleNumber, null, null, null, "_id");
			if (cursor != null) {
				cursor.moveToFirst();

				articleNumber = cursor.getInt(1);
				title = cursor.getString(2);
				writer = cursor.getString(3);
				id = cursor.getString(4);
				content = cursor.getString(5);
				writeDate = cursor.getString(6);
				imgName = cursor.getString(7);

				article = new Article(articleNumber, title, writer, id,
						content, writeDate, imgName);
			}

			cursor.close();
		}

		return article;
	}

	/*
	 * Database 관련한 메서드들
	 */
	public boolean initializeDatabase() {
		try {
			createDatabase();
			if (!isTableExist()) {
				createTable();
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean isTableExist() {
		try {
			String searchTable = "select DISTINCT tbl_name from sqlite_master where tbl_name = '"
					+ TABLE_NAME + "';";
			Cursor cursor = database.rawQuery(searchTable, null);

			if (cursor.getCount() == 0) {
				return false;
			}

			cursor.close();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return true;
	}

	private void createDatabase() {
		try {

			database = context.openOrCreateDatabase("sqliteTest.db",
					SQLiteDatabase.CREATE_IF_NECESSARY, null);
			database.setLocale(Locale.getDefault());
			database.setVersion(1);
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	private void createTable() {
		String sql = "create table "
				+ TABLE_NAME
				+ "(_id integer primary key autoincrement, " +
				"ArticleNumber integer UNIQUE not null, " +
				"Title text not null, " +
				"Writer text not null, " +
				"Id text not null, " +
				"Content text not null, " +
				"WriteDate text not null, " +
				"ImgName text UNIQUE not null);";
		database.execSQL(sql);
	}
}
