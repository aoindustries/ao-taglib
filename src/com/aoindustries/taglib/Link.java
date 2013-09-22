/*
 * aocode-public-taglib - Reusable Java taglib of general tools with minimal external dependencies.
 * Copyright (C) 2013  AO Industries, Inc.
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

import com.aoindustries.net.HttpParameters;
import javax.servlet.jsp.JspException;

/**
 * Holds the data for a Link tag that is passed between LinkTag and any LinksAttribute parent.
 *
 * @author  AO Industries, Inc.
 */
public class Link {

    private final String href;
	private final HttpParameters params;
    private final String hreflang;
    private final String rel;
    private final String type;

    public Link(String href, HttpParameters params, String hreflang, String rel, String type) throws JspException {
        this.href = href;
		this.params = params;
        this.hreflang = hreflang;
        this.rel = rel;
        this.type = type;
    }

    public String getHref() {
        return href;
    }

	public HttpParameters getParams() {
		return params;
	}

	public String getHreflang() {
        return hreflang;
    }

    public String getRel() {
        return rel;
    }

    public String getType() {
        return type;
    }
}
