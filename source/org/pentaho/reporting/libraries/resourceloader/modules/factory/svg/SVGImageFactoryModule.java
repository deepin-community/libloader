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

package org.pentaho.reporting.libraries.resourceloader.modules.factory.svg;

import java.io.IOException;
import java.awt.geom.Rectangle2D;
import java.awt.Image;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.renderer.StaticRenderer;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceData;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceLoadingException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.reporting.libraries.resourceloader.SimpleResource;
import org.w3c.dom.svg.SVGDocument;

/**
 * Creation-Date: 05.04.2006, 17:58:42
 *
 * @author Thomas Morgner
 */
public class SVGImageFactoryModule extends AbstractSVGFactoryModule
{
  public SVGImageFactoryModule()
  {
  }

  public Resource create(final ResourceManager caller,
                         final ResourceData data,
                         final ResourceKey context)
      throws ResourceLoadingException
  {
    try
    {
      final long version = data.getVersion(caller);
      final HeadlessSVGUserAgent userAgent = new HeadlessSVGUserAgent();
      final DocumentLoader loader = new DocumentLoader(userAgent);
      final SVGDocument document = (SVGDocument) loader.loadDocument
          (String.valueOf(data.getKey().getIdentifier()), data.getResourceAsStream(caller));
      final BridgeContext ctx = new BridgeContext(userAgent, loader);
      final GVTBuilder builder = new GVTBuilder();
      final GraphicsNode node = builder.build(ctx, document);
      final Rectangle2D bounds = node.getBounds();

      final StaticRenderer staticRenderer = new StaticRenderer();
      staticRenderer.setDoubleBuffered(true);
      staticRenderer.setTree(node);
      staticRenderer.updateOffScreen((int) bounds.getWidth(), (int) bounds.getHeight());
      return new SimpleResource(data.getKey(), staticRenderer.getOffScreen(), Image.class, version);
    }
    catch (IOException e)
    {
      throw new ResourceLoadingException("Failed to process SVG file", e);
    }
  }

}
