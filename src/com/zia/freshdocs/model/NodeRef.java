package com.zia.freshdocs.model;

public class NodeRef
{
	private String _content;
	private String contentType;
	private String _name;
	private boolean _isFolder;

	public String getContent()
	{
		return _content;
	}

	public void setContent(String content)
	{
		this._content = content;
	}

	public String getContentType()
	{
		return contentType;
	}

	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}	
	
	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		this._name = name;
	}

	public boolean isFolder()
	{
		return _isFolder;
	}

	public void setFolder(boolean isFolder)
	{
		this._isFolder = isFolder;
	}

	@Override
	public String toString()
	{
		return _name;
	}

	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof NodeRef))
		{
			return false;
		}
		
		NodeRef other = (NodeRef) o;
		return _name.equals(other.getName()) &&
			_content.equals(other.getContent());
	}
}
