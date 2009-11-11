package com.zia.freshdocs.data;

public class NodeRef
{
	private String _uuid;
	private String _name;
	private boolean _isDocument;

	public String getUuid()
	{
		return _uuid;
	}

	public void setUuid(String uuid)
	{
		this._uuid = uuid;
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		this._name = name;
	}

	public boolean isDocument()
	{
		return _isDocument;
	}

	public void setDocument(boolean isDocument)
	{
		this._isDocument = isDocument;
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
			_uuid.equals(other.getUuid());
	}	
	
}
