package com.zia.freshdocs.model;

public class NodeRef
{
	private String _content;
	private String _contentType;
	private String _name;
	private String _lastModificationDate;
	private String _lastModifiedBy;
	private String _version;
	private int _contentLength;
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
		return _contentType;
	}

	public void setContentType(String contentType)
	{
		this._contentType = contentType;
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

	public String getLastModificationDate()
	{
		return _lastModificationDate;
	}

	public void setLastModificationDate(String lastModificationDate)
	{
		this._lastModificationDate = lastModificationDate;
	}

	public String getLastModifiedBy()
	{
		return _lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy)
	{
		this._lastModifiedBy = lastModifiedBy;
	}

	public int getContentLength()
	{
		return _contentLength;
	}

	public void setContentLength(int contentLength)
	{
		this._contentLength = contentLength;
	}

	public String getVersion()
	{
		return _version;
	}

	public void setVersion(String version)
	{
		this._version = version;
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
