/*
 * (C) Copyright 2017 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Arnaud Kervern
 */
package org.nuxeo.extractor.mapper.descriptors;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

@XObject("operation")
public class OperationDescriptor {

    /**
     * The operation class that must be annotated using {@link org.nuxeo.ecm.automation.core.annotations.Operation}
     * annotation.
     */
    @XNode("@class")
    public Class<?> type;

    /**
     * Put it to true to override an existing contribution having the same ID. By default overriding is not permitted
     * and an exception is thrown when this flag is on false.
     */
    @XNode("@replace")
    public boolean replace;

}