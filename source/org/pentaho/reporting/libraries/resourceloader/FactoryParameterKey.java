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

package org.pentaho.reporting.libraries.resourceloader;

import java.io.Serializable;

/**
 * Creation-Date: 16.05.2006, 15:24:21
 *
 * @author Thomas Morgner
 */
public final class FactoryParameterKey implements Serializable
{
  private String name;
  private transient int hashKey;
  private static final long serialVersionUID = 4574332935778105499L;

  public FactoryParameterKey(final String name)
  {
    if (name == null)
    {
      throw new NullPointerException();
    }
    this.name = name;
  }

  public boolean equals(final Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    final FactoryParameterKey that = (FactoryParameterKey) o;

    if (!name.equals(that.name))
    {
      return false;
    }

    return true;
  }

  public int hashCode()
  {
    if (hashKey == 0)
    {
      hashKey = name.hashCode();
    }
    return hashKey;
  }

  public String getName()
  {
    return name;
  }

  public String toString()
  {
    return getClass().getName() + "{name=" + getName() + "}";
  }
}
