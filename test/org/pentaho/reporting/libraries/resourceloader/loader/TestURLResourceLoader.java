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
 * Copyright (c) 2009 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.reporting.libraries.resourceloader.loader;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.pentaho.reporting.libraries.resourceloader.LibLoaderBoot;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceKeyCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

public class TestURLResourceLoader extends TestCase
{
  private static final String STRING_SERIALIZATION_PREFIX = "resourcekey:"; //$NON-NLS-1$
  private static final String DESERIALIZE_PREFIX = STRING_SERIALIZATION_PREFIX + URLResourceLoader.class.getName();
  private static final String URL1 = "http://www.pentaho.com/index.html";
  private static final String URL2 = "http://www.pentaho.com/images/pentaho_logo.png";

  public TestURLResourceLoader()
  {
  }

  public TestURLResourceLoader(final String string)
  {
    super(string);
  }

  protected void setUp() throws Exception
  {
    LibLoaderBoot.getInstance().start();
  }

  /**
   * Tests the serialization of File based resource keys
   */
  public void testSerialize() throws Exception
  {
    final URLResourceLoader resourceLoader = new URLResourceLoader();
    final ResourceManager manager = new ResourceManager();
    manager.registerDefaults();
    ResourceKey key = null;
    Map factoryParameters = new HashMap();
    String serializedVersion = null;

    // Test with null parameter
    try
    {
      serializedVersion = resourceLoader.serialize(null, key);
      fail("Serialization with a null paramter should throw a NullPointerException"); //$NON-NLS-1$
    }
    catch (NullPointerException npe)
    {
      // success
    }

    // Test with a file instead of a URL
    try
    {
      final File tempFile = File.createTempFile("unittest", "test");
      tempFile.deleteOnExit();
      key = manager.createKey(tempFile);
      serializedVersion = resourceLoader.serialize(key, key);
      fail("The resource key should not handled by the URL resource loader"); //$NON-NLS-1$
    }
    catch (IllegalArgumentException iae)
    {
      // success
    }

    // Create a key from the temp file
    key = manager.createKey(new URL(URL1));
    serializedVersion = resourceLoader.serialize(key, key);
    assertNotNull("The returned key should not be null", key); //$NON-NLS-1$
    assertTrue("Serialized verison does not start with the correct header", serializedVersion //$NON-NLS-1$
        .startsWith(STRING_SERIALIZATION_PREFIX));
    assertTrue("Serialized version does not contain the correct schema information", serializedVersion //$NON-NLS-1$
        .startsWith(STRING_SERIALIZATION_PREFIX + resourceLoader.getClass().getName() + ';'));
    assertTrue("Serialized version should contain the filename", serializedVersion.endsWith(URL1)); //$NON-NLS-1$

    // Create a key as a relative path from the above key
    key = manager.deriveKey(key, "images/pentaho_logo.png");
    assertNotNull(key);
    serializedVersion = resourceLoader.serialize(key, key);
    assertNotNull(serializedVersion);
    assertTrue("Serialized verison does not start with the correct header", serializedVersion //$NON-NLS-1$
        .startsWith(STRING_SERIALIZATION_PREFIX));
    assertTrue("Serialized version does not contain the correct schema information", serializedVersion //$NON-NLS-1$
        .startsWith(STRING_SERIALIZATION_PREFIX + resourceLoader.getClass().getName() + ';'));
    assertTrue("Serialized version should contain the filename", serializedVersion.endsWith(URL2)); //$NON-NLS-1$

    // Create a key with factory parameters
    factoryParameters.put("this", "that");
    factoryParameters.put("null", null);
    key = manager.createKey(new URL(URL1), factoryParameters);
    serializedVersion = resourceLoader.serialize(key, key);
    assertNotNull("The returned key should not be null", key); //$NON-NLS-1$
    assertTrue("Serialized verison does not start with the correct header", serializedVersion //$NON-NLS-1$
        .startsWith(STRING_SERIALIZATION_PREFIX));
    assertTrue("Serialized version does not contain the correct schema information", serializedVersion //$NON-NLS-1$
        .startsWith(STRING_SERIALIZATION_PREFIX + resourceLoader.getClass().getName() + ';'));
    assertTrue("Serialized version should contain the filename", serializedVersion.indexOf(";" + URL1 + ";") > -1); //$NON-NLS-1$
    assertTrue("Serialized version should contain factory parameters", serializedVersion.indexOf("this=that") > -1);
    assertTrue("Serialized version should contain factory parameters", serializedVersion.indexOf(':') > -1);
    assertTrue("Serialized version should contain factory parameters", serializedVersion.endsWith("null=")
        || serializedVersion.contains("null=:"));
  }

  /**
   * Tests the deserialization of File based resource keys
   */
  public void testDeserializer() throws Exception
  {
    final URLResourceLoader resourceLoader = new URLResourceLoader();

    // Test deserializing invalid strings
    try
    {
      resourceLoader.deserialize(null, null);
      fail("deserialize of a null string should throw an exception");
    }
    catch (IllegalArgumentException iae)
    {
      // success
    }

    try
    {
      resourceLoader.deserialize(null, "");
      fail("deserialize of an empty string should throw an exception");
    }
    catch (ResourceKeyCreationException rkce)
    {
      // success
    }

    try
    {
      resourceLoader.deserialize(null, STRING_SERIALIZATION_PREFIX + this.getClass().getName() + ';' + URL1);
      fail("deserialize with an invalid resource class name should throw an exception");
    }
    catch (ResourceKeyCreationException rkce)
    {
      // success
    }

    try
    {
      resourceLoader.deserialize(null, DESERIALIZE_PREFIX + ":/tmp");
      fail("deserialize with an invalid file should thrown an exception");
    }
    catch (ResourceKeyCreationException rkce)
    {
      // success
    }

    final ResourceKey key1 = resourceLoader.deserialize(null, DESERIALIZE_PREFIX + ';' + URL1 + ";this=that:invalid:null=");
    assertNotNull(key1);
    assertTrue(key1.getIdentifier() instanceof URL);
    assertEquals(URLResourceLoader.class.getName(), key1.getSchema());
    assertEquals(new URL(URL1), key1.getIdentifier());
    assertEquals(2, key1.getFactoryParameters().size());
    assertTrue(!key1.getFactoryParameters().containsKey("invalid"));
    assertTrue(key1.getFactoryParameters().containsKey("null"));
    assertNull(key1.getFactoryParameters().get("null"));
    assertEquals("that", key1.getFactoryParameters().get("this"));
  }

  /**
   * This is a happy path "round-trip" test which should demonstrate the serializing and deserializing 
   * a resource key should produce the same key
   */
  public void testSerializeDeserializeRoundtrip() throws Exception
  {
    final URLResourceLoader resourceLoader = new URLResourceLoader();
    final Map factoryParams = new HashMap();
    final ResourceManager manager = new ResourceManager();
    manager.registerDefaults();

    factoryParams.put("this", "that");
    factoryParams.put("null", null);
    final ResourceKey originalKey = manager.createKey(URL1, factoryParams);

    final String serializedVersion = resourceLoader.serialize(null, originalKey);
    final ResourceKey duplicateKey = resourceLoader.deserialize(null, serializedVersion);
    assertNotNull(duplicateKey);
    assertTrue(originalKey.equals(duplicateKey));
  }
}
