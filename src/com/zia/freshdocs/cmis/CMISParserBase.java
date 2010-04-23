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


import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.zia.freshdocs.model.NodeRef;

import android.util.Log;


public class CMISParserBase implements CMISParser
{
	protected static final String ATOM_NS = "http://www.w3.org/2005/Atom";
	protected static final String CMIS_NS = "http://docs.oasis-open.org/ns/cmis/core/200908/";
	
	protected static final String CMIS_COLLECTION_TYPE = "cmis:collectionType";
	
	public CMISInfo getCMISInfo(String buf)
	{
		DocumentBuilder docBuilder = null;
		CMISInfo info = null;
		
		try
		{
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse(new ByteArrayInputStream(buf.getBytes()));

			NodeList nodes = doc.getElementsByTagName("cmis:cmisVersionSupported");
			String rawVersion = nodes.item(0).getFirstChild().getNodeValue();
		
			info = new CMISInfo();
			info.setVersion(rawVersion.split("\\s")[0]);

			nodes = doc.getElementsByTagName("collection");
			int n = nodes.getLength();

			if(n > 0)
			{
				Element node = null;

				for(int i = 0; i < n; i++)
				{
					node = (Element) nodes.item(i);

					// CMIS 0.6 uses an attr
					if(node.hasAttribute(CMIS_COLLECTION_TYPE) && 
							node.getAttribute(CMIS_COLLECTION_TYPE).equals("rootchildren"))
					{
						break;
					}
					// CMIS 1.0 has a child node
					else
					{
						NodeList collectionTypes = node.getElementsByTagName("cmisra:collectionType");

						if(collectionTypes.getLength() > 0)
						{
							String colType = collectionTypes.item(0).getFirstChild().getNodeValue(); 
							if(colType.equals("root"))
							{
								break;
							}
						}
					}
				}
				
				info.setRootURI(node.getAttribute("href"));
			}
		}
		catch (Exception e)
		{
			Log.e(CMISParserBase.class.getSimpleName(), "Error getting root folder id", e);
		}
	
		return info;
	}

	@Override
	public NodeRef[] parseChildren(String buf)
	{
		return null;
	}
}
