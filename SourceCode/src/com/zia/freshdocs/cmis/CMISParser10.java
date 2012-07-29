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
package com.zia.freshdocs.cmis;


import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.zia.freshdocs.model.NodeRef;

public class CMISParser10 extends CMISParserBase 
{
	private static final String URN_UUID = "urn:uuid:";

	@SuppressWarnings("unchecked")
	@Override
	public NodeRef[] parseChildren(InputStream is)
	{
		NodeRef[] children = null;
		
		HashMap<String, String> nsMap = new HashMap<String, String>();
		nsMap.put("atom", ATOM_NS);
		nsMap.put("cmis", CMIS_NS);
		
		DocumentFactory factory = new DocumentFactory();
		factory.setXPathNamespaceURIs(nsMap);
		
		SAXReader reader = new SAXReader(); 
		reader.setDocumentFactory(factory);
		
		try
		{
			Document document = reader.read(is); 
			List<Element> entries = (List<Element>) document.selectNodes("/atom:feed/atom:entry");			
			int numEntries = entries.size();
			children = new NodeRef[numEntries];
			
			Element entry;
			NodeRef nodeRef;

			// Iterate over each entry element and find corresponding attrs
			for(int i = 0; i < numEntries; i++)
			{
				nodeRef = new NodeRef();
				children[i] = nodeRef;
				
				entry = entries.get(i);
				
				// Get either the node uuid or src uri and content type
				Element id = entry.element("id");
				String uuid = id.getTextTrim().replace(URN_UUID, "");
				nodeRef.setContent(uuid);

				Element content = entry.element("content");
				String contentType = content.attributeValue("type");

				if(contentType != null)
				{
					nodeRef.setContentType(contentType);
					nodeRef.setContent(content.attributeValue("src"));					
				}
				
				List<Element> cmisProperties = entry.selectNodes(".//cmis:properties/*");
				int numProperties = cmisProperties.size();
				Element cmisProperty;
				
				// Iterate over each property and populate associated field in NodeRef
				for(int j = 0; j < numProperties; j++)
				{
					cmisProperty = cmisProperties.get(j);
					String attrValue = cmisProperty.attributeValue("propertyDefinitionId");
					
					if(attrValue == null)
					{
						continue;
					}
					
					if(attrValue.equals("cmis:name"))
					{
						nodeRef.setName(cmisProperty.elementTextTrim("value"));
					}

					if(attrValue.equals("cmis:baseTypeId"))
					{
						String typeId = cmisProperty.elementTextTrim("value");
						nodeRef.setFolder(typeId != null && typeId.equals("cmis:folder"));
					}
					
					if(attrValue.equals("cmis:lastModificationDate"))
					{
						nodeRef.setLastModificationDate(cmisProperty.elementTextTrim("value"));
					}					
					
					if(attrValue.equals("cmis:lastModifiedBy"))
					{
						nodeRef.setLastModifiedBy(cmisProperty.elementTextTrim("value"));
					}					
					
					if(attrValue.equals("cmis:versionLabel"))
					{
						nodeRef.setVersion(cmisProperty.elementTextTrim("value"));
					}
					
					if(attrValue.equals("cmis:createdBy"))
					{
						nodeRef.setCreateBy(cmisProperty.elementTextTrim("value"));
					}
					
					if(attrValue.equals("cmis:objectId"))
					{
						nodeRef.setObjectId(cmisProperty.elementTextTrim("value"));
					}
					
					if(attrValue.equals("cmis:parentId"))
					{
						nodeRef.setParentId(cmisProperty.elementTextTrim("value"));
					}
					
					if(attrValue.equals("cmis:contentStreamLength"))
					{
						nodeRef.setContentLength(Long.valueOf(cmisProperty.elementTextTrim("value")));
					}					
				}
			}
		}
		catch (DocumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return children;
	}

}
