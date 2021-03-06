/*
 * ao-taglib - Making JSP be what it should have been all along.
 * Copyright (C) 2009, 2010, 2011, 2012, 2013, 2016, 2017, 2019, 2020, 2021  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-taglib.
 *
 * ao-taglib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-taglib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-taglib.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoapps.taglib;

import com.aoapps.encoding.MediaType;
import com.aoapps.encoding.taglib.EncodingBufferedTag;
import com.aoapps.io.buffer.BufferResult;
import com.aoapps.net.MutableURIParameters;
import com.aoapps.net.URI;
import com.aoapps.net.URIParametersMap;
import java.io.IOException;
import java.io.Writer;
import javax.servlet.jsp.JspException;

/**
 * TODO: Have absolute option, canonical and lastModified, too?
 * TODO: Replace uses of encoding:text with this as it is now more appropriate for sending dynamic parameters to JavaScript since it calls encodeURL.
 *
 * @author  AO Industries, Inc.
 */
public class UrlTag extends EncodingBufferedTag implements ParamsAttribute {

	public UrlTag() {
		init();
	}

	@Override
	public MediaType getContentType() {
		return MediaType.URL; // TODO: Does this work with whitepace around the URL that will be trimmed?  Compare to ImgTag
	}

	@Override
	public MediaType getOutputType() {
		return MediaType.URL;
	}

/* BodyTag only:
	private static final long serialVersionUID = 1L;
/**/

	private MutableURIParameters params;
	@Override
	public void addParam(String name, Object value) {
		if(params == null) params = new URIParametersMap();
		params.add(name, value);
	}

	private void init() {
		params = null;
	}

	@Override
/* BodyTag only:
	protected int doEndTag(BufferResult capturedBody, Writer out) throws JspException, IOException {
/**/
/* SimpleTag only: */
	protected void doTag(BufferResult capturedBody, Writer out) throws JspException, IOException {
/**/
		URI url = new URI(capturedBody.trim().toString()).addParameters(params);
		/* TODO: Prefix context path?
		if(url.startsWith("/")) {
			HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
			out.write(URIEncoder.encodeURI(request.getContextPath()));
		}*/
		out.write(url.toString());
/* BodyTag only:
		return SKIP_PAGE;
/**/
	}

/* BodyTag only:
	@Override
	public void doFinally() {
		try {
			init();
		} finally {
			super.doFinally();
		}
	}
/**/
}
