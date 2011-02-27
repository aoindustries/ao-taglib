/*
 * aocode-public-taglib - Reusable Java taglib of general tools with minimal external dependencies.
 * Copyright (C) 2011  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of aocode-public-taglib.
 *
 * aocode-public-taglib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aocode-public-taglib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with aocode-public-taglib.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.taglib;

import javax.servlet.jsp.JspException;

/**
 * Holds the data for a Meta tag that is passed between MetaTag and any MetasAttribute parent.
 *
 * @author  AO Industries, Inc.
 */
public class Meta {

    private final String name;
    private final String httpEquiv;
    private final String content;

    public Meta(String name, String httpEquiv, String content) throws JspException {
        this.name = name;
        this.httpEquiv = httpEquiv;
        if(content==null) throw new JspException(ApplicationResources.accessor.getMessage("Meta.contentRequired"));
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getHttpEquiv() {
        return httpEquiv;
    }

    public String getContent() {
        return content;
    }
}