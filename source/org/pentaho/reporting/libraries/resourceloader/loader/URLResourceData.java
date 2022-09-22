/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2006 - 2009 Pentaho Corporation and Contributors.  All rights reserved.
 */

package org.pentaho.reporting.libraries.resourceloader.loader;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.pentaho.reporting.libraries.base.util.IOUtils;
import org.pentaho.reporting.libraries.resourceloader.ResourceData;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceLoadingException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

/**
 * A generic read handler for URL resources.
 *
 * @author Thomas Morgner
 */
public class URLResourceData extends AbstractResourceData
{
  private long lastDateMetaDataRead;
  private long modificationDate;
  private String filename;
  private Long contentLength;
  private String contentType;
  private boolean metaDataOK;
  private URL url;
  private ResourceKey key;
  private static final long serialVersionUID = -7183025686032509509L;

  public URLResourceData(final ResourceKey key)
  {
    if (key == null)
    {
      throw new NullPointerException();
    }

    this.modificationDate = -1;
    this.key = key;
    this.url = (URL) key.getIdentifier();
    // for the ease of implementation, we take the file name from the URL.
    // Feel free to add a 'Content-Disposition' parser with all details :)
    this.filename = IOUtils.getInstance().getFileName(url);
  }

  protected URLResourceData()
  {
  }

  protected void setUrl(final URL url)
  {
    this.url = url;
  }

  protected void setKey(final ResourceKey key)
  {
    this.key = key;
  }

  protected void setFilename(final String filename)
  {
    this.filename = filename;
  }

  protected URL getUrl()
  {
    return url;
  }

  protected String getFilename()
  {
    return filename;
  }

  private void readMetaData() throws IOException
  {
    if (metaDataOK && (System.currentTimeMillis() - lastDateMetaDataRead) < 5000)
    {
      return;
    }

    final URLConnection c = url.openConnection();
    c.setDoOutput(false);
    c.setAllowUserInteraction(false);
    if (c instanceof HttpURLConnection)
    {
      final HttpURLConnection httpURLConnection = (HttpURLConnection) c;
      httpURLConnection.setRequestMethod("HEAD");
    }
    c.connect();
    modificationDate = c.getDate();
    contentLength = new Long(c.getContentLength());
    contentType = c.getHeaderField("content-type");
    c.getInputStream().close();
    metaDataOK = true;
    lastDateMetaDataRead = System.currentTimeMillis();
  }

  public InputStream getResourceAsStream(final ResourceManager caller) throws ResourceLoadingException
  {
    try
    {
      final URLConnection c = url.openConnection();
      c.setDoOutput(false);
      c.setAllowUserInteraction(false);
      return c.getInputStream();
    }
    catch (IOException e)
    {
      throw new ResourceLoadingException("Failed to open URL connection", e);
    }
  }

  public Object getAttribute(final String key)
  {
    if (key.equals(ResourceData.FILENAME))
    {
      return filename;
    }
    if (key.equals(ResourceData.CONTENT_LENGTH))
    {
      try
      {
        if (metaDataOK == false)
        {
          readMetaData();
        }
        return contentLength;
      }
      catch (IOException e)
      {
        return null;
      }
    }
    if (key.equals(ResourceData.CONTENT_TYPE))
    {
      try
      {
        if (metaDataOK == false)
        {
          readMetaData();
        }
        return contentType;
      }
      catch (IOException e)
      {
        return null;
      }
    }
    return null;
  }

  public long getVersion(final ResourceManager caller)
      throws ResourceLoadingException
  {
    try
    {
      // always read the new date .. sorry, this is expensive, but needed here
      // else the cache would not be in sync ...
      readMetaData();
      return modificationDate;
    }
    catch (IOException e)
    {
      return -1;
    }
  }

  public ResourceKey getKey()
  {
    return key;
  }
}
