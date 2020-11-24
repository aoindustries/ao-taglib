/*
 * ao-taglib - Making JSP be what it should have been all along.
 * Copyright (C) 2011, 2013, 2015, 2016, 2017, 2019, 2020  AO Industries, Inc.
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
package com.aoindustries.taglib;

import com.aoindustries.encoding.Coercion;
import com.aoindustries.encoding.MediaType;
import com.aoindustries.html.Html;
import com.aoindustries.html.servlet.HtmlEE;
import com.aoindustries.io.buffer.BufferResult;
import com.aoindustries.lang.Strings;
import com.aoindustries.servlet.jsp.tagext.JspTagUtils;
import java.io.IOException;
import java.io.Writer;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspTagException;
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
	public void setMeta(Meta meta) throws JspTagException {
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
	public void setName(String name) throws JspTagException {
		this.name = name;
	}

	private String httpEquiv;
	public void setHttpEquiv(String httpEquiv) throws JspTagException {
		this.httpEquiv = httpEquiv;
	}

	private String itemprop;
	public void setItemprop(String itemprop) throws JspTagException {
		this.itemprop = Strings.trimNullIfEmpty(itemprop);
	}

	private Object charset; // TODO: Support java Charset, too
	public void setCharset(Object charset) throws JspTagException {
		this.charset = AttributeUtils.trimNullIfEmpty(charset);
	}

	private Object content;
	@Override
	public void setContent(Object content) throws JspTagException {
		this.content = AttributeUtils.nullIfEmpty(content);
	}

/* BodyTag only:
	private transient BufferResult capturedBody;
/**/

	private void init() {
		name = null;
		httpEquiv = null;
		itemprop = null;
		charset = null;
		content = null;
/* BodyTag only:
		capturedBody = null;
/**/
	}

/* BodyTag only:
	@Override
	protected int doAfterBody(BufferResult capturedBody, Writer out) {
		assert this.capturedBody == null;
		assert capturedBody != null;
		this.capturedBody = capturedBody;
		return SKIP_BODY;
	}
/**/

	@Override
/* BodyTag only:
	protected int doEndTag(Writer out) throws JspTagException, IOException {
/**/
/* SimpleTag only: */
	protected void doTag(BufferResult capturedBody, Writer out) throws JspTagException, IOException {
		PageContext pageContext = (PageContext)getJspContext();
/**/
		Optional<MetasAttribute> parent = JspTagUtils.findAncestor(this, MetasAttribute.class);
		if(content == null) setContent((capturedBody == null) ? "" : capturedBody.trim()); // TODO: Just initialize capturedBody to EmptyResult to simplify this, all tags
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
			Html html = HtmlEE.get(
				pageContext.getServletContext(),
				(HttpServletRequest)pageContext.getRequest(),
				(HttpServletResponse)pageContext.getResponse(),
				out
			);
			GlobalAttributesUtils.doGlobalAttributes(global, html.meta())
				.name(name)
				.httpEquiv(httpEquiv)
				// TODO: Create a global "itemprop" in ao-fluent-html
				.attribute("itemprop", itemprop)
				// TOOD: charset to String via Meta.charset(String)
				.attribute("charset", charset)
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
