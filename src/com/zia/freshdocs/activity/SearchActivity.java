package com.zia.freshdocs.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.zia.freshdocs.R;
import com.zia.freshdocs.widget.CMISAdapter;

public class SearchActivity extends Activity implements OnItemClickListener
{
	ListView _listView;
	ImageButton _searchButton;
	EditText _queryText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		
		_listView = (ListView) findViewById(R.id.search_list);
		_listView.setOnItemClickListener(this);
		_searchButton = (ImageButton) findViewById(R.id.query_button);
		_queryText = (EditText) findViewById(R.id.query_text);

		_searchButton.setOnClickListener(new View.OnClickListener() 
		{
            public void onClick(View v) 
            {
                search(_queryText.getText().toString());
            }
        });
		
		CMISAdapter adapter = new CMISAdapter(this, android.R.layout.simple_list_item_1);
		_listView.setAdapter(adapter);
	}
	
	protected void search(String term)
	{
		CMISAdapter adapter = (CMISAdapter) _listView.getAdapter();
		adapter.query(term);
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		CMISAdapter adapter = (CMISAdapter) _listView.getAdapter();
		adapter.getChildren(position);
	}
}
