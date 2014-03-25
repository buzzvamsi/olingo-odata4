/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.server.core.uri.validator;

import org.apache.olingo.commons.api.ODataRuntimeException;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmReturnType;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceAction;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;

public class UriValidator {

  //@formatter:off (Eclipse formatter)
  //CHECKSTYLE:OFF (Maven checkstyle)
  private boolean[][] decisionMatrix =
      {
          /*                                          0-FILTER 1-FORMAT 2-EXPAND 3-ID     4-COUNT  5-ORDERBY 6-SEARCH 7-SELECT 8-SKIP   9-SKIPTOKEN 10-LEVELS 11-TOP */
          /*                              all  0 */ { true ,   true ,   true ,   false,   true ,   true ,    true ,   true ,   true ,   true ,      true ,    false },
          /*                            batch  1 */ { false,   false,   false,   false,   false,   false,    false,   false,   false,   false,      false,    false },
          /*                        crossjoin  2 */ { true ,   true ,   true ,   false,   true ,   true ,    true ,   true ,   true ,   true ,      true ,    true  },
          /*                         entityId  3 */ { false,   true ,   true ,   true ,   false,   false,    false,   true ,   false,   false,      true ,    false },
          /*                         metadata  4 */ { false,   true ,   false,   false,   false,   false,    false,   false,   false,   false,      false,    false },
          /*                         resource  5 */ { false,   true ,   false,   false,   false,   false,    false,   false,   false,   false,      false,    false },
          /*                          service  6 */ { false,   true ,   false,   false,   false,   false,    false,   false,   false,   false,      false,    false },
          /*                        entitySet  7 */ { true ,   true ,   true ,   false,   true ,   true ,    true ,   true ,   true ,   true ,      true ,    true  },
          /*                   entitySetCount  8 */ { false,   false,   false,   false,   false,   false,    false,   false,   false,   false,      false,    false },
          /*                           entity  9 */ { false,   true ,   true ,   false,   false,   false,    false,   true ,   false,   false,      true ,    false },
          /*                      mediaStream 10 */ { false,   true ,   false,   false,   false,   false,    false,   false,   false,   false,      false,    false },
          /*                       references 11 */ { true ,   true ,   false,   false,   false,   true ,    true ,   false,   true ,   true ,      false,    true  },
          /*                        reference 12 */ { false,   true ,   false,   false,   false,   false,    false,   false,   false,   false,      false,    false },
          /*                  propertyComplex 13 */ { false,   true ,   true ,   false,   false,   false,    false,   true ,   false,   false,      true ,    false },
          /*        propertyComplexCollection 14 */ { true ,   true ,   true ,   false,   true ,   true ,    false,   false,   true ,   true ,      true ,    true  },
          /*   propertyComplexCollectionCount 15 */ { false,   false,   false,   false,   false,   false,    false,   false,   false,   false,      false,    false },
          /*                propertyPrimitive 16 */ { false,   true ,   false,   false,   false,   false,    false,   false,   false,   false,      false,    false },
          /*      propertyPrimitiveCollection 17 */ { true ,   true ,   false,   false,   false,   true ,    false,   false,   true ,   true ,      false,    true  },
          /* propertyPrimitiveCollectionCount 18 */ { false,   false,   false,   false,   false,   false,    false,   false,   false,   false,      false,    false },
          /*           propertyPrimitiveValue 19 */ { false,   true ,   false,   false,   false,   false,    false,   false,   false,   false,      false,    false },                    
      };
  //CHECKSTYLE:ON
  //@formatter:on

  private enum RowIndex {
    all(0),
    batch(1),
    crossjoin(2),
    entityId(3),
    metadata(4),
    resource(5),
    service(6),
    entitySet(7),
    entitySetCount(8),
    entity(9),
    mediaStream(10),
    references(11),
    reference(12),
    propertyComplex(13),
    propertyComplexCollection(14),
    propertyComplexCollectionCount(15),
    propertyPrimitive(16),
    propertyPrimitiveCollection(17),
    propertyPrimitiveCollectionCount(18),
    propertyPrimitiveValue(19);

    private int idx;

    RowIndex(int i) {
      idx = i;
    }

    public int getIndex() {
      return idx;
    }
  }

  private enum ColumnIndex {
    filter(0),
    format(1),
    expand(2),
    id(3),
    count(4),
    orderby(5),
    search(6),
    select(7),
    skip(8),
    skiptoken(9),
    levels(10),
    top(11);

    private int idx;

    ColumnIndex(int i) {
      idx = i;
    }

    public int getIndex() {
      return idx;
    }

  }

  public void validate(final UriInfo uriInfo, final Edm edm) throws UriValidationException {

    validateQueryOptions(uriInfo, edm);
    validateKeyPredicateTypes(uriInfo, edm);

  }

  private ColumnIndex colIndex(SystemQueryOptionKind queryOptionKind) {
    ColumnIndex idx;
    switch (queryOptionKind) {
    case FILTER:
      idx = ColumnIndex.filter;
      break;
    case FORMAT:
      idx = ColumnIndex.format;
      break;
    case EXPAND:
      idx = ColumnIndex.expand;
      break;
    case ID:
      idx = ColumnIndex.id;
      break;
    case COUNT:
      idx = ColumnIndex.count;
      break;
    case ORDERBY:
      idx = ColumnIndex.orderby;
      break;
    case SEARCH:
      idx = ColumnIndex.search;
      break;
    case SELECT:
      idx = ColumnIndex.select;
      break;
    case SKIP:
      idx = ColumnIndex.skip;
      break;
    case SKIPTOKEN:
      idx = ColumnIndex.skiptoken;
      break;
    case LEVELS:
      idx = ColumnIndex.levels;
      break;
    case TOP:
      idx = ColumnIndex.top;
      break;
    default:
      throw new ODataRuntimeException("Unsupported option: " + queryOptionKind);
    }

    return idx;
  }

  private RowIndex rowIndex(final UriInfo uriInfo, Edm edm) throws UriValidationException {
    RowIndex idx;

    switch (uriInfo.getKind()) {
    case all:
      idx = RowIndex.all;
      break;
    case batch:
      idx = RowIndex.batch;
      break;
    case crossjoin:
      idx = RowIndex.crossjoin;
      break;
    case entityId:
      idx = RowIndex.entityId;
      break;
    case metadata:
      idx = RowIndex.metadata;
      break;
    case resource:
      idx = rowIndexForResourceKind(uriInfo, edm);
      break;
    case service:
      idx = RowIndex.service;
      break;
    default:
      throw new ODataRuntimeException("Unsupported uriInfo kind: " + uriInfo.getKind());
    }

    return idx;
  }

  private RowIndex rowIndexForResourceKind(UriInfo uriInfo, Edm edm) throws UriValidationException {
    RowIndex idx;

    int lastPathSegmentIndex = uriInfo.getUriResourceParts().size() - 1;
    UriResource lastPathSegment = uriInfo.getUriResourceParts().get(lastPathSegmentIndex);

    switch (lastPathSegment.getKind()) {
    case count:
      idx = rowIndexForCount(uriInfo, lastPathSegment);
      break;
    case action:
      idx = rowIndexForAction(lastPathSegment);
      break;
    case complexProperty:
      idx = rowIndexForComplexProperty(lastPathSegment);
      break;
    case entitySet:
      idx = rowIndexForEntitySet(lastPathSegment);
      break;
    case function:
      idx = rowIndexForFunction(lastPathSegment);
      break;
    case navigationProperty:
      idx = ((UriResourceNavigation) lastPathSegment).isCollection() ? RowIndex.entitySet : RowIndex.entity;
      break;
    case primitiveProperty:
      idx = rowIndexForPrimitiveProperty(lastPathSegment);
      break;
    case ref:
      idx = rowIndexForRef(uriInfo, lastPathSegment);
      break;
    case root:
      idx = RowIndex.service;
      break;
    case singleton:
      idx = RowIndex.entity;
      break;
    case value:
      idx = rowIndexForValue(uriInfo);
      break;
    default:
      throw new ODataRuntimeException("Unsupported uriResource kind: " + lastPathSegment.getKind());
    }

    return idx;
  }

  private RowIndex rowIndexForValue(UriInfo uriInfo) throws UriValidationException {
    RowIndex idx;
    int secondLastPathSegmentIndex = uriInfo.getUriResourceParts().size() - 2;
    UriResource secondLastPathSegment = uriInfo.getUriResourceParts().get(secondLastPathSegmentIndex);
    switch (secondLastPathSegment.getKind()) {
    case primitiveProperty:
      idx = RowIndex.propertyPrimitiveValue;
      break;
    case entitySet:
      idx = RowIndex.mediaStream;
      break;
    default:
      throw new UriValidationException("Unexpected kind in path segment before $value: "
          + secondLastPathSegment.getKind());

    }
    return idx;
  }

  private RowIndex rowIndexForRef(UriInfo uriInfo, UriResource lastPathSegment)
      throws UriValidationException {
    RowIndex idx;
    int secondLastPathSegmentIndex = uriInfo.getUriResourceParts().size() - 2;
    UriResource secondLastPathSegment = uriInfo.getUriResourceParts().get(secondLastPathSegmentIndex);

    if (secondLastPathSegment instanceof UriResourcePartTyped) {
      idx = ((UriResourcePartTyped) secondLastPathSegment).isCollection() ? RowIndex.references : RowIndex.reference;
    } else {
      throw new UriValidationException("secondLastPathSegment not a class of UriResourcePartTyped: "
          + lastPathSegment.getClass());
    }

    return idx;
  }

  private RowIndex rowIndexForPrimitiveProperty(UriResource lastPathSegment)
      throws UriValidationException {
    RowIndex idx;
    if (lastPathSegment instanceof UriResourcePartTyped) {
      idx =
          ((UriResourcePartTyped) lastPathSegment).isCollection() ? RowIndex.propertyPrimitiveCollection
              : RowIndex.propertyPrimitive;
    } else {
      throw new UriValidationException("lastPathSegment not a class of UriResourcePartTyped: "
          + lastPathSegment.getClass());
    }
    return idx;
  }

  private RowIndex rowIndexForFunction(UriResource lastPathSegment) throws UriValidationException {
    RowIndex idx;
    UriResourceFunction urf = (UriResourceFunction) lastPathSegment;
    EdmReturnType rt = urf.getFunction().getReturnType();
    switch (rt.getType().getKind()) {
    case ENTITY:
      if (((EdmEntityType) rt.getType()).hasStream()) {
        idx = RowIndex.mediaStream;
      } else {
        idx = rt.isCollection() ? RowIndex.entitySet : RowIndex.entity;
      }
      break;
    case PRIMITIVE:
      idx = rt.isCollection() ? RowIndex.propertyPrimitiveCollection : RowIndex.propertyPrimitive;
      break;
    case COMPLEX:
      idx = rt.isCollection() ? RowIndex.propertyComplexCollection : RowIndex.propertyComplex;
      break;
    default:
      throw new UriValidationException("Unsupported function return type: " + rt.getType().getKind());
    }

    return idx;
  }

  private RowIndex rowIndexForEntitySet(UriResource lastPathSegment) throws UriValidationException {
    RowIndex idx;
    if (lastPathSegment instanceof UriResourcePartTyped) {
      idx = ((UriResourcePartTyped) lastPathSegment).isCollection() ? RowIndex.entitySet : RowIndex.entity;
    } else {
      throw new UriValidationException("lastPathSegment not a class of UriResourcePartTyped: "
          + lastPathSegment.getClass());
    }
    return idx;
  }

  private RowIndex rowIndexForComplexProperty(UriResource lastPathSegment) throws UriValidationException {
    RowIndex idx;
    if (lastPathSegment instanceof UriResourcePartTyped) {
      idx =
          ((UriResourcePartTyped) lastPathSegment).isCollection() ? RowIndex.propertyComplexCollection
              : RowIndex.propertyComplex;
    } else {
      throw new UriValidationException("lastPathSegment not a class of UriResourcePartTyped: "
          + lastPathSegment.getClass());
    }
    return idx;
  }

  private RowIndex rowIndexForAction(UriResource lastPathSegment) throws UriValidationException {
    RowIndex idx;
    UriResourceAction ura = (UriResourceAction) lastPathSegment;
    EdmReturnType rt = ura.getAction().getReturnType();
    switch (rt.getType().getKind()) {
    case ENTITY:
      if (((EdmEntityType) rt.getType()).hasStream()) {
        idx = RowIndex.mediaStream;
      } else {
        idx = rt.isCollection() ? RowIndex.entitySet : RowIndex.entity;
      }
      break;
    case PRIMITIVE:
      idx = rt.isCollection() ? RowIndex.propertyPrimitiveCollection : RowIndex.propertyPrimitive;
      break;
    case COMPLEX:
      idx = rt.isCollection() ? RowIndex.propertyComplexCollection : RowIndex.propertyComplex;
      break;
    default:
      throw new UriValidationException("Unsupported action return type: " + rt.getType().getKind());
    }

    return idx;
  }

  private RowIndex rowIndexForCount(UriInfo uriInfo, UriResource lastPathSegment)
      throws UriValidationException {

    RowIndex idx;
    int secondLastPathSegmentIndex = uriInfo.getUriResourceParts().size() - 2;
    UriResource secondLastPathSegment = uriInfo.getUriResourceParts().get(secondLastPathSegmentIndex);
    switch (secondLastPathSegment.getKind()) {
    case entitySet:
      idx = RowIndex.entitySetCount;
      break;
    case complexProperty:
      idx = RowIndex.propertyComplexCollectionCount;
      break;
    case primitiveProperty:
      idx = RowIndex.propertyPrimitiveCollectionCount;
      break;
    default:
      throw new UriValidationException("Illegal path part kind: " + lastPathSegment.getKind());
    }

    return idx;
  }

  private void validateQueryOptions(final UriInfo uriInfo, Edm edm) throws UriValidationException {
    try {
      RowIndex row = rowIndex(uriInfo, edm);

      for (SystemQueryOption option : uriInfo.getSystemQueryOptions()) {
        ColumnIndex col = colIndex(option.getKind());

        System.out.print("[" + row + "][" + col + "]");

        if (!decisionMatrix[row.getIndex()][col.getIndex()]) {
          throw new UriValidationException("System query option not allowed: " + option.getName());
        }
      }
    } finally {
      System.out.println();
    }

  }

  private void validateKeyPredicateTypes(final UriInfo uriInfo, final Edm edm) throws UriValidationException {}

}
