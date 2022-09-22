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
 * Copyright (c) 2006 - 2009 Pentaho Corporation and Contributors..  All rights reserved.
 */

package org.pentaho.reporting.libraries.resourceloader;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Test cases for the ResourceKeyUtils class
 *  
 * @author David Kincade
 */
public class TestResourceKeyUtils extends TestCase
{
  public TestResourceKeyUtils()
  {
  }

  public TestResourceKeyUtils(final String string)
  {
    super(string);
  }

  protected void setUp() throws Exception
  {
    LibLoaderBoot.getInstance().start();
  }

  public void testGetFactoryParametersAsString() throws ResourceKeyCreationException
  {
    final ResourceManager manager = new ResourceManager();
    manager.registerDefaults();
    ResourceKey key = null;
    String stringParameters = null;
    Map factoryParameters = new HashMap();

    // Test with no factory parameters specified
    key = manager.createKey("res://org/pentaho/reporting/libraries/resourceloader/test1.properties");
    assertNotNull(key);
    stringParameters = ResourceKeyUtils.convertFactoryParametersToString(key.getFactoryParameters());
    assertNull("Null parameter set should result null", stringParameters);

    // Test with empty parameter set
    key = manager.createKey("res://org/pentaho/reporting/libraries/resourceloader/test1.properties", factoryParameters);
    assertNotNull(key);
    stringParameters = ResourceKeyUtils.convertFactoryParametersToString(key.getFactoryParameters());
    assertNull("Empty parameter set should result in null", stringParameters);

    // Test with one parameter
    factoryParameters.put("this", "that");
    key = manager.createKey("res://org/pentaho/reporting/libraries/resourceloader/test1.properties", factoryParameters);
    assertNotNull(key);
    stringParameters = ResourceKeyUtils.convertFactoryParametersToString(key.getFactoryParameters());
    assertEquals("Unexpected results with one parameter", "this=that", stringParameters);

    // Test with one parameter that has a null value
    factoryParameters.clear();
    factoryParameters.put("null", null);
    key = manager.createKey("res://org/pentaho/reporting/libraries/resourceloader/test1.properties", factoryParameters);
    assertNotNull(key);
    stringParameters = ResourceKeyUtils.convertFactoryParametersToString(key.getFactoryParameters());
    assertEquals("Could not handle parameter with a null value", "null=", stringParameters);

    // Test with multiple parameters (and one has a null value)
    factoryParameters.clear();
    factoryParameters.put("this", "that");
    factoryParameters.put("test with spaces", " spaces should be preserved ");
    factoryParameters.put("this-one_null", null);
    factoryParameters.put(manager.getClass(), manager);
    key = manager.createKey("res://org/pentaho/reporting/libraries/resourceloader/test1.properties", factoryParameters);
    assertNotNull(key);
    stringParameters = ResourceKeyUtils.convertFactoryParametersToString(key.getFactoryParameters());

    // Count the number of separators (:) in the string
    int count = 0;
    String temp = stringParameters;
    while (temp.indexOf(':') > -1)
    {
      ++count;
      temp = temp.substring(temp.indexOf(':') + 1);
    }
    assertEquals("There should be 3 separators in the string", 3, count);

    // Make sure the parameters exist
    assertTrue("Could not find parameter 'this=that'", stringParameters.indexOf("this=that") > -1);
    assertTrue("Could not find parameter 'test with spaces'", stringParameters
        .indexOf("test with spaces= spaces should be preserved ") > -1);
    assertTrue("Could not find parameter that isn't a String", stringParameters.indexOf(manager.getClass() + "="
        + manager.toString()) > -1);
    assertTrue("Could not find parameter with null value", stringParameters.indexOf("this-one_null=:") > -1
        || stringParameters.endsWith("this-one_null="));
  }

  /**
   * Tests the parsing of a String into a set of parameters
   */
  public void testGetFactoryParametersFromString()
  {
    Map map = null;
    
    // Test null string
    map = ResourceKeyUtils.parseFactoryParametersFromString(null);
    assertNull("The map should be null if the source string is null", map);
    
    // Test empty string
    map = ResourceKeyUtils.parseFactoryParametersFromString("");
    assertNull("The map should be null if the source string is blank", map);
    
    // Test invalid string with no equals signs
    map = ResourceKeyUtils.parseFactoryParametersFromString("this is a test of the string : a colon : and another:one more");
    assertNull("The map should be null if the source string is invalid", map);
    
    // Test a valid string including a null value in the middle
    map = ResourceKeyUtils.parseFactoryParametersFromString("this=that:null=:one=1: with spaces = more spaces :space= :junk:one=won:nullagain=");
    assertNotNull("The map should not be null if the source string is valid", map);
    assertEquals("The map should have 6 entries - skipping the junk and not containing a duplicate", 6, map.size());
    assertEquals("Invalid value for 'this'", "that", map.get("this"));
    assertEquals("Invalid value for ' with spaces '", " more spaces ", map.get(" with spaces "));
    assertEquals("Invalid value for 'space'", " ", map.get("space"));

    assertTrue("Could not find entry for 'null'", map.containsKey("null"));
    assertNull("Invalid value for 'null'", map.get("null"));
    assertTrue("Could not find entry for 'nullagain'", map.containsKey("nullagain"));
    assertNull("Invalid value for 'nullagain'", map.get("nullagain"));

    assertTrue("Invalid value for 'one'", "1".equals(map.get("one")) || "won".equals(map.get("one")));
    
    assertTrue("The map should not contain a value for 'junk'", !map.containsKey("junk"));
  }
  
  public void testGetSchemaFromString()
  {
    assertNull(ResourceKeyUtils.readSchemaFromString(null));
    assertNull(ResourceKeyUtils.readSchemaFromString(""));
    assertNull(ResourceKeyUtils.readSchemaFromString("invalid string"));
    assertEquals("sample", ResourceKeyUtils.readSchemaFromString("sample;"));
    assertEquals("sample2", ResourceKeyUtils.readSchemaFromString("sample2;junk"));
    assertEquals("sample", ResourceKeyUtils.readSchemaFromString("resourcekey:sample;"));
    assertEquals("sample2", ResourceKeyUtils.readSchemaFromString("resourcekey:sample2;junk"));
    assertEquals("resourcekey2:sample", ResourceKeyUtils.readSchemaFromString("resourcekey2:sample;"));
    assertEquals("resourcekey2:sample2", ResourceKeyUtils.readSchemaFromString("resourcekey2:sample2;junk"));
  }
 }
