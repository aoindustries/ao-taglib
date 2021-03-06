/*
 * ao-taglib - Making JSP be what it should have been all along.
 * Copyright (C) 2011, 2013, 2015, 2016, 2017, 2019, 2020, 2021  AO Industries, Inc.
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
import com.aoapps.html.servlet.DocumentEE;
import com.aoapps.html.servlet.META;
import com.aoapps.io.buffer.BufferResult;
import com.aoapps.lang.Coercion;
import com.aoapps.lang.Strings;
import com.aoapps.servlet.jsp.tagext.JspTagUtils;
import java.io.IOException;
import java.io.Writer;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * @author  AO Industries, Inc.
 */
public class MetaTag extends ElementBufferedTag
	implements
		NameAttribute,
		ContentAttribute
{

	public MetaTag() {
		init();
	}

	@Override
	public MediaType getContentType() {
		return MediaType.TEXT;
	}

	@Override
	public MediaType getOutputType() {
		return MediaType.XHTML;
	}

	/**
	 * Copies all values from the provided meta.
	 */
	public void setMeta(Meta meta) {
		GlobalAttributesUtils.copy(meta.getGlobal(), this);
		setName(meta.getName());
		setHttpEquiv(meta.getHttpEquiv());
		setItemprop(meta.getItemprop());
		setCharset(meta.getCharset());
		setContent(meta.getContent());
	}

/* BodyTag only:
	private static final long serialVersionUID = 1L;
/**/

	private String name;
	@Override
	public void setName(String name) {
		this.name = name;
	}

	private String httpEquiv;
	public void setHttpEquiv(String httpEquiv) {
		this.httpEquiv = httpEquiv;
	}

	private String itemprop;
	public void setItemprop(String itemprop) {
		this.itemprop = Strings.trimNullIfEmpty(itemprop);
	}

	private Object charset; // TODO: Support java Charset, too
	public void setCharset(Object charset) {
		this.charset = AttributeUtils.trimNullIfEmpty(charset);
	}

	private Object content;
	@Override
	public void setContent(Object content) {
		this.content = AttributeUtils.nullIfEmpty(content);
	}

	private void init() {
		name = null;
		httpEquiv = null;
		itemprop = null;
		charset = null;
		content = null;
	}

	@Override
/* BodyTag only:
	protected int doEndTag(BufferResult capturedBody, Writer out) throws JspException, IOException {
/**/
/* SimpleTag only: */
	protected void doTag(BufferResult capturedBody, Writer out) throws JspException, IOException {
		PageContext pageContext = (PageContext)getJspContext();
/**/
		Optional<MetasAttribute> parent = JspTagUtils.findAncestor(this, MetasAttribute.class);
		if(content == null) setContent(capturedBody.trim());
		if(parent.isPresent()) {
			parent.get().addMeta(
				new Meta(
					global.freeze(),
					Strings.trimNullIfEmpty(name),
					Strings.trim(httpEquiv),
					itemprop,
					Coercion.toString(charset),
					Coercion.toString(content)
				)
			);
		} else {
			// Write the meta tag directly here
			DocumentEE document = new DocumentEE(
				pageContext.getServletContext(),
				(HttpServletRequest)pageContext.getRequest(),
				(HttpServletResponse)pageContext.getResponse(),
				out,
				false, // Do not add extra newlines to JSP
				false  // Do not add extra indentation to JSP
			);
			META<?> meta = document.meta();
			GlobalAttributesUtils.doGlobalAttributes(global, meta);
			meta.name(name)
				.httpEquiv(httpEquiv)
				// TODO: Create a global "itemprop" in ao-fluent-html
				.attribute("itemprop", itemprop);
			if(charset != null) {
				// TOOD: charset to String via Meta.charset(String)
				meta.charset(Coercion.toString(charset));
			}
			meta
				.content(content)
				.__();
		}
/* BodyTag only:
		return EVAL_PAGE;
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
