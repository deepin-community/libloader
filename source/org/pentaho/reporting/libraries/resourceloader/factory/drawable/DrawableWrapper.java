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

package org.pentaho.reporting.libraries.resourceloader.factory.drawable;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creation-Date: 05.12.2007, 19:15:56
 *
 * @author Thomas Morgner
 */
public class DrawableWrapper
{
  private static final Log logger = LogFactory.getLog(DrawableWrapper.class);
  private static final Map drawables = Collections.synchronizedMap(new HashMap());

  private Object backend;
  private Method drawMethod;
  private Method getPreferredSizeMethod;
  private Method isKeepAspectRatioMethod;
  private static final Object[] EMPTY_ARGS = new Object[0];
  private static final Class[] EMPTY_PARAMS = new Class[0];
  private static final Class[] PARAMETER_TYPES = new Class[]{Graphics2D.class, Rectangle2D.class};

  public DrawableWrapper(final Object maybeDrawable)
  {
    if (maybeDrawable == null)
    {
      throw new NullPointerException("Drawable must not be null");
    }
    if (maybeDrawable instanceof DrawableWrapper)
    {
      throw new IllegalArgumentException("Cannot wrap around a drawable-wrapper");
    }
    final Class aClass = maybeDrawable.getClass();
    try
    {
      drawMethod = aClass.getMethod("draw", PARAMETER_TYPES);
      final int modifiers = drawMethod.getModifiers();
      if (Modifier.isPublic(modifiers) == false ||
          Modifier.isAbstract(modifiers) ||
          Modifier.isStatic(modifiers))
      {
        if (logger.isWarnEnabled())
        {
          logger.warn("DrawMethod is not valid: " + aClass + '#' + drawMethod);
        }
        drawMethod = null;
      }
    }
    catch (NoSuchMethodException e)
    {
      // ignore exception
      if (logger.isWarnEnabled())
      {
        logger.warn("The object is not a drawable: " + aClass);
      }
      drawMethod = null;
    }

    if (drawMethod != null)
    {
      try
      {
        isKeepAspectRatioMethod = aClass.getMethod("isPreserveAspectRatio", EMPTY_PARAMS);
        final int modifiers = isKeepAspectRatioMethod.getModifiers();
        if (Modifier.isPublic(modifiers) == false ||
            Modifier.isAbstract(modifiers) ||
            Modifier.isStatic(modifiers) ||
            Boolean.TYPE.equals(isKeepAspectRatioMethod.getReturnType()) == false)
        {
          isKeepAspectRatioMethod = null;
        }
      }
      catch (NoSuchMethodException e)
      {
        // ignored ..
      }

      try
      {
        getPreferredSizeMethod = aClass.getMethod("getPreferredSize", EMPTY_PARAMS);
        final int modifiers = getPreferredSizeMethod.getModifiers();
        if (Modifier.isPublic(modifiers) == false ||
            Modifier.isAbstract(modifiers) ||
            Modifier.isStatic(modifiers) ||
            Dimension2D.class.equals(getPreferredSizeMethod.getReturnType()) == false)
        {
          isKeepAspectRatioMethod = null;
        }
      }
      catch (NoSuchMethodException e)
      {
        // ignored ..
      }
    }
    backend = maybeDrawable;
  }

  public Object getBackend()
  {
    return backend;
  }

  public void draw(final Graphics2D g2, final Rectangle2D bounds)
  {
    if (drawMethod == null)
    {
      return;
    }

    try
    {
      drawMethod.invoke(backend, new Object[]{g2, bounds});
    }
    catch (Exception e)
    {
      if (logger.isDebugEnabled())
      {
        logger.warn("Invoking draw failed:", e);
      }
    }
  }

  /**
   * Returns the preferred size of the drawable. If the drawable is aspect ratio aware, these bounds should be used to
   * compute the preferred aspect ratio for this drawable.
   *
   * @return the preferred size.
   */
  public Dimension getPreferredSize()
  {
    if (getPreferredSizeMethod == null)
    {
      return null;
    }

    try
    {
      return (Dimension) getPreferredSizeMethod.invoke(backend, EMPTY_ARGS);
    }
    catch (Exception e)
    {
      if (logger.isWarnEnabled())
      {
        logger.warn("Invoking getPreferredSize failed:", e);
      }
      return null;
    }
  }

  /**
   * Returns true, if this drawable will preserve an aspect ratio during the drawing.
   *
   * @return true, if an aspect ratio is preserved, false otherwise.
   */
  public boolean isPreserveAspectRatio()
  {
    if (isKeepAspectRatioMethod == null)
    {
      return false;
    }

    try
    {
      return Boolean.TRUE.equals(isKeepAspectRatioMethod.invoke(backend, EMPTY_ARGS));
    }
    catch (Exception e)
    {
      if (logger.isWarnEnabled())
      {
        logger.warn("Invoking isKeepAspectRatio failed:", e);
      }
      return false;
    }
  }

  public static boolean isDrawable(final Object maybeDrawable)
  {
    if (maybeDrawable == null)
    {
      throw new NullPointerException("A <null> value can never be a drawable.");
    }
    final String key = maybeDrawable.getClass().getName();
    final Boolean result = (Boolean) drawables.get(key);
    if (result != null)
    {
      return result.booleanValue();
    }
    final boolean b = computeIsDrawable(maybeDrawable);
    if (b == true)
    {
      drawables.put(key, Boolean.TRUE);
    }
    else
    {
      drawables.put(key, Boolean.FALSE);
    }
    return b;
  }

  private static boolean computeIsDrawable(final Object maybeDrawable)
  {
    final Class aClass = maybeDrawable.getClass();
    try
    {
      final Method drawMethod = aClass.getMethod("draw", PARAMETER_TYPES);
      final int modifiers = drawMethod.getModifiers();
      return (Modifier.isPublic(modifiers) &&
          Modifier.isAbstract(modifiers) == false &&
          Modifier.isStatic(modifiers) == false);
    }
    catch (NoSuchMethodException e)
    {
      // ignore exception
      return false;
    }
  }
}
