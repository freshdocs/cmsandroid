package com.zia.freshdocs.model;

import java.io.Serializable;

public class NodeRef implements Serializable
{
	private static final long serialVersionUID = 1203230939547509235L;
	
	private String _content;
	private String _contentType;
	private String _name;
	private String _lastModificationDate;
	private String _lastModifiedBy;
	private String _version;
	private long _contentLength;
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

	public long getContentLength()
	{
		return _contentLength;
	}

	public void setContentLength(long contentLength)
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
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((_content == null) ? 0 : _content.hashCode());
		result = prime * result + ((_name == null) ? 0 : _name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeRef other = (NodeRef) obj;
		if (_content == null)
		{
			if (other._content != null)
				return false;
		}
		else if (!_content.equals(other._content))
			return false;
		if (_name == null)
		{
			if (other._name != null)
				return false;
		}
		else if (!_name.equals(other._name))
			return false;
		return true;
	}
}
