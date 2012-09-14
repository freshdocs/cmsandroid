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
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.util.Log;

import com.zia.freshdocs.model.NodeRef;

public class CMISParser06 extends CMISParserBase
{

	@Override
	public NodeRef[] parseChildren(InputStream is)
	{
		NodeRef[] refs = new NodeRef[0];
		DocumentBuilder docBuilder = null;
		
		try
		{
			Pattern pattern = Pattern.compile("&(?![a-zA-Z0-9]+;)");
			Matcher matcher = pattern.matcher(IOUtils.toString(is));
			String sanitized = matcher.replaceAll("&amp;");
			
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse(new ByteArrayInputStream(sanitized.getBytes()));
			
			// Iterate over all the entry nodes and build NodeRefs
			NodeList nodes = doc.getElementsByTagName("entry");
			NodeList children = null;
			Element node = null;
			NodeRef nodeRef = null;
			int n = nodes.getLength();
			
			for(int i=0; i<n; i++)
			{
				if(refs.length == 0)
				{
					refs = new NodeRef[n];
				}
				
				node = (Element) nodes.item(i);
				children = node.getElementsByTagName("content");
				
				if(children.getLength() > 0)
				{
					Element contentNode = (Element) children.item(0);
					String content = null;
					nodeRef = new NodeRef();
					
					if(contentNode.hasAttribute("type"))
					{
						nodeRef.setContentType(contentNode.getAttribute("type"));
						content = contentNode.getAttribute("src");
					} 
					else
					{
						content = contentNode.getFirstChild().getNodeValue();
					}
					
					nodeRef.setContent(content);
					
					children = node.getElementsByTagName("title");
					if(children.getLength() > 0)
					{
						nodeRef.setName(children.item(0).getFirstChild().getNodeValue());
					}

					children = node.getElementsByTagName("updated");
					if(children.getLength() > 0)
					{
						nodeRef.setLastModificationDate(
								children.item(0).getFirstChild().getNodeValue()); 
					}

					children = node.getElementsByTagName("cmis:propertyString");
					int nChildren = children.getLength();
					
					if(nChildren > 0)
					{
						for(int j = 0; j < nChildren; j++)
						{
							Element child = (Element) children.item(j);
							
							if(child.getAttribute("cmis:name").equals("BaseType"))
							{
								NodeList valueNode = child.getElementsByTagName("cmis:value");
								String baseType = valueNode.item(0).getFirstChild().getNodeValue(); 
								nodeRef.setFolder(baseType.equals("folder"));
							} 
							else if(child.getAttribute("cmis:name").equals("LastModifiedBy"))
							{
								NodeList valueNode = child.getElementsByTagName("cmis:value");
								nodeRef.setLastModifiedBy(
										valueNode.item(0).getFirstChild().getNodeValue()); 
							}
							else if(child.getAttribute("cmis:name").equals("VersionLabel"))
							{
								NodeList valueNode = child.getElementsByTagName("cmis:value");
								if(valueNode.getLength() > 0)
								{
									nodeRef.setVersion(
											valueNode.item(0).getFirstChild().getNodeValue());
								}
							}
						}
					}
					
					children = node.getElementsByTagName("cmis:propertyInteger");
					nChildren = children.getLength();

					if(nChildren > 0)
					{
						for(int j = 0; j < nChildren; j++)
						{
							Element child = (Element) children.item(j);
							
							if(child.getAttribute("cmis:name").equals("ContentStreamLength"))
							{
								NodeList valueNode = child.getElementsByTagName("cmis:value");
								nodeRef.setContentLength(
										Long.valueOf(valueNode.item(0).getFirstChild().getNodeValue()));
								break;
							}
						}
					}
					
					refs[i] = nodeRef;
				}
			}
		}
		catch (Exception e)
		{
			Log.e(CMIS.class.getSimpleName(), "Error getting children", e);
		}
		
		return refs;
	}

}
