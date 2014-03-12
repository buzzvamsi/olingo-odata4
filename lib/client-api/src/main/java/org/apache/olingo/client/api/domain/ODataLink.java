/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.client.api.domain;

import java.net.URI;
import org.apache.olingo.client.api.utils.URIUtils;
import org.apache.olingo.commons.api.edm.constants.ODataServiceVersion;

/**
 * OData link.
 */
public class ODataLink extends ODataItem {

  private static final long serialVersionUID = 7274966414277952124L;

  /**
   * Link type.
   */
  protected final ODataLinkType type;

  /**
   * Link rel.
   */
  protected final String rel;

  /**
   * Constructor.
   *
   * @param version OData service version.
   * @param uri URI.
   * @param type type.
   * @param title title.
   */
  public ODataLink(final ODataServiceVersion version, final URI uri, final ODataLinkType type, final String title) {
    super(title);

    this.link = uri;
    this.type = type;

    switch (this.type) {
      case ASSOCIATION:
        this.rel = version.getNamespaceMap().get(ODataServiceVersion.ASSOCIATION_LINK_REL) + title;
        break;

      case ENTITY_NAVIGATION:
      case ENTITY_SET_NAVIGATION:
        this.rel = version.getNamespaceMap().get(ODataServiceVersion.NAVIGATION_LINK_REL) + title;
        break;

      case MEDIA_EDIT:
      default:
        this.rel = version.getNamespaceMap().get(ODataServiceVersion.MEDIA_EDIT_LINK_REL) + title;
        break;
    }
  }

  /**
   * Constructor.
   *
   * @param version OData service version.
   * @param baseURI base URI.
   * @param href href.
   * @param type type.
   * @param title title.
   */
  public ODataLink(final ODataServiceVersion version,
          final URI baseURI, final String href, final ODataLinkType type, final String title) {

    this(version, URIUtils.getURI(baseURI, href), type, title);
  }

  /**
   * Gets link type.
   *
   * @return link type;
   */
  public ODataLinkType getType() {
    return type;
  }

  /**
   * Gets link rel.
   *
   * @return link rel
   */
  public String getRel() {
    return rel;
  }
}