package org.nhnnext.android.basic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * HTTP로 외부의 데이터를 반환하는 클래스
 * JSON Data를 가져와서 Dao에 전달할 수 있는 객체로 변환해야함.
 */
public class Proxy {
	private Context context;
	
	private String serverUrl;
	private SharedPreferences pref;
	
	public Proxy(Context context) {
		this.context = context;
		String prefName = context.getResources().getString(R.string.pref_name);
		pref = context.getSharedPreferences(prefName, context.MODE_PRIVATE);
		serverUrl = pref.getString(
				context.getResources().getString(R.string.server_url), "");
	}

	public ArrayList<ArticleDTO> getArticleDTO() {
		ArrayList<ArticleDTO> articleList = new ArrayList<ArticleDTO>();

		JSONArray jArr;
		int articleNumber;
		String writer;
		String title;
		String id;
		String content;
		String writeDate;
		String imgName;

		String jsonData = getJSON();
		ArticleDTO articleDTO;
		
		try {
			jArr = new JSONArray(jsonData);

			for (int i = 0; i < jArr.length(); ++i) {
				JSONObject jObj = jArr.getJSONObject(i);

				articleNumber = jObj.getInt("ArticleNumber");
				title = jObj.getString("Title");
				writer = jObj.getString("Writer");
				id = jObj.getString("Id");
				content = jObj.getString("Content");
				writeDate = jObj.getString("WriteDate");
				imgName = jObj.getString("ImgName");

				articleDTO = new ArticleDTO(articleNumber, title, writer, id,
						content, writeDate, imgName);
				articleList.add(articleDTO);

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return articleList;
	}

	public String getJSON() {
		try {
			String prefArticleNumberKey = context.getResources().getString(
					R.string.pref_article_number);

			String articleNumber = pref.getString(prefArticleNumberKey, "0");

			String serverUrl = this.serverUrl + "loadData.php/?articleNumber="
					+ articleNumber;

			URLEncoder.encode(serverUrl, "UTF-8");
			URL url = new URL(serverUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setRequestMethod("GET");
			conn.setRequestProperty("Cache-Control", "no-cache");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Content-length", "0");
			conn.setRequestProperty("Accept", "application/json");
			conn.setUseCaches(false);
			conn.setAllowUserInteraction(false);

			conn.setDoOutput(true);
			conn.setDoInput(true);

			conn.connect();

			int status = conn.getResponseCode();

			switch (status) {
			case 200:
			case 201:
				BufferedReader br = new BufferedReader(new InputStreamReader(
						conn.getInputStream()));
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
				br.close();
				return sb.toString();
			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.i("test", "ERROR:" + e);
		}

		return null;
	}

}