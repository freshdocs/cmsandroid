/*******************************************************************************
 * The MIT License
 * 
 * Copyright (c) 2010 Zia Consulting, Inc
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package com.zia.freshdocs.model;

import java.io.Serializable;

/**
 * Represents a node in the repo accessed by CMIS.getChildren calls.
 * @author jsimpson
 *
 */
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
