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

package org.pentaho.reporting.libraries.resourceloader.modules.cache.ehcache;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import org.pentaho.reporting.libraries.resourceloader.cache.ResourceDataCache;
import org.pentaho.reporting.libraries.resourceloader.cache.ResourceDataCacheProvider;
import org.pentaho.reporting.libraries.resourceloader.cache.ResourceFactoryCache;
import org.pentaho.reporting.libraries.resourceloader.cache.ResourceFactoryCacheProvider;
import org.pentaho.reporting.libraries.resourceloader.cache.ResourceBundleDataCacheProvider;
import org.pentaho.reporting.libraries.resourceloader.cache.ResourceBundleDataCache;

/**
 * Creation-Date: 13.04.2006, 16:32:20
 *
 * @author Thomas Morgner
 */
public class EHCacheProvider implements ResourceDataCacheProvider,
        ResourceFactoryCacheProvider, ResourceBundleDataCacheProvider
{
  private static CacheManager cacheManager;

  public static CacheManager getCacheManager() throws CacheException
  {
    if (cacheManager == null)
    {
      cacheManager = CacheManager.create();
    }
    return cacheManager;
  }

  public EHCacheProvider()
  {
  }

  public ResourceDataCache createDataCache()
  {
    try
    {
      final CacheManager manager = getCacheManager();
      synchronized(manager)
      {
        if (manager.cacheExists("libloader-data") == false)
        {
          manager.addCache("libloader-data");
        }
        return new EHResourceDataCache(manager.getCache("libloader-data"));
      }
    }
    catch (CacheException e)
    {
      return null;
    }
  }

  public ResourceBundleDataCache createBundleDataCache()
  {
    try
    {
      final CacheManager manager = getCacheManager();
      synchronized(manager)
      {
        if (manager.cacheExists("libloader-bundles") == false)
        {
          manager.addCache("libloader-bundles");
        }
        return new EHResourceBundleDataCache(manager.getCache("libloader-bundles"));
      }
    }
    catch (CacheException e)
    {
      return null;
    }
  }

  public ResourceFactoryCache createFactoryCache()
  {
    try
    {
      final CacheManager manager = getCacheManager();
      synchronized(manager)
      {
        if (manager.cacheExists("libloader-factory") == false)
        {
          manager.addCache("libloader-factory");
        }
        return new EHResourceFactoryCache(manager.getCache("libloader-factory"));
      }
    }
    catch (CacheException e)
    {
      return null;
    }
  }
}
