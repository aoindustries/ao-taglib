/*
 * ao-taglib - Making JSP be what it should have been all along.
 * Copyright (C) 2011, 2012, 2013, 2015, 2016, 2017, 2019, 2020  AO Industries, Inc.
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
package com.aoindustries.taglib.legacy;

import com.aoindustries.encoding.Coercion;
import com.aoindustries.encoding.MediaType;
import static com.aoindustries.encoding.TextInXhtmlAttributeEncoder.encodeTextInXhtmlAttribute;
import static com.aoindustries.encoding.TextInXhtmlAttributeEncoder.textInXhtmlAttributeEncoder;
import com.aoindustries.io.buffer.BufferResult;
import com.aoindustries.net.MutableURIParameters;
import com.aoindustries.net.URIParametersMap;
import com.aoindustries.servlet.lastmodified.AddLastModified;
import com.aoindustries.taglib.AttributeRequiredException;
import com.aoindustries.taglib.AttributeUtils;
import com.aoindustries.taglib.FrameborderAttribute;
import com.aoindustries.taglib.GlobalAttributesUtils;
import com.aoindustries.taglib.HeightAttribute;
import com.aoindustries.taglib.ParamUtils;
import com.aoindustries.taglib.ParamsAttribute;
import com.aoindustries.taglib.SrcAttribute;
import com.aoindustries.taglib.UrlUtils;
import com.aoindustries.taglib.WidthAttribute;
import com.aoindustries.util.i18n.MarkupType;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Locale;
import javax.servlet.jsp.JspTagException;

/**
 * @author  AO Industries, Inc.
 */
public class IframeTag extends ElementBufferedBodyTag
	implements
		// Attributes
		SrcAttribute,
		ParamsAttribute,
		WidthAttribute,
		HeightAttribute,
		FrameborderAttribute
{

	public IframeTag() {
		init();
	}

	@Override
	public MediaType getContentType() {
		return MediaType.XHTML;
	}

	@Override
	public MediaType getOutputType() {
		return MediaType.XHTML;
	}

/* BodyTag only: */
	private static final long serialVersionUID = 1L;
/**/

	private String src;
	@Override
	public void setSrc(String src) throws JspTagException {
		this.src = AttributeUtils.nullIfEmpty(src);
	}

	private MutableURIParameters params;
	@Override
	public void addParam(String name, String value) {
		if(params==null) params = new URIParametersMap();
		params.addParameter(name, value);
	}

	private boolean absolute;
	public void setAbsolute(boolean absolute) {
		this.absolute = absolute;
	}

	private boolean canonical;
	public void setCanonical(boolean canonical) {
		this.canonical = canonical;
	}

	private AddLastModified addLastModified;
	public void setAddLastModified(String addLastModified) {
		this.addLastModified = AddLastModified.valueOfLowerName(addLastModified.trim().toLowerCase(Locale.ROOT));
	}

	private Object width;
	@Override
	public void setWidth(Object width) throws JspTagException {
		this.width = AttributeUtils.trimNullIfEmpty(width);
	}

	private Object height;
	@Override
	public void setHeight(Object height) throws JspTagException {
		this.height = AttributeUtils.trimNullIfEmpty(height);
	}

	private boolean frameborder;
	@Override
	public void setFrameborder(boolean frameborder) {
		this.frameborder = frameborder;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see  ParamUtils#addDynamicAttribute(java.lang.String, java.lang.String, java.lang.Object, java.util.List, com.aoindustries.taglib.ParamsAttribute)
	 */
	@Override
	protected boolean addDynamicAttribute(String uri, String localName, Object value, List<String> expectedPatterns) throws JspTagException {
		return
			super.addDynamicAttribute(uri, localName, value, expectedPatterns)
			|| ParamUtils.addDynamicAttribute(uri, localName, value, expectedPatterns, this);
	}

/* BodyTag only: */
	private transient BufferResult capturedBody;
/**/

	private void init() {
		src = null;
		params = null;
		absolute = false;
		canonical = false;
		addLastModified = AddLastModified.AUTO;
		width = null;
		height = null;
		frameborder = true;
/* BodyTag only: */
		capturedBody = null;
/**/
	}

/* BodyTag only: */
	@Override
	protected int doAfterBody(BufferResult capturedBody, Writer out) {
		assert this.capturedBody == null;
		assert capturedBody != null;
		this.capturedBody = capturedBody;
		return SKIP_BODY;
	}
/**/

	@Override
/* BodyTag only: */
	protected int doEndTag(Writer out) throws JspTagException, IOException {
/**/
/* SimpleTag only:
	protected void doTag(BufferResult capturedBody, Writer out) throws JspTagException, IOException {
		PageContext pageContext = (PageContext)getJspContext();
/**/
		if(src == null) throw new AttributeRequiredException("src");
		out.write("<iframe");
		GlobalAttributesUtils.writeGlobalAttributes(global, out);
		// TODO: Include id/name by doctype
		String id = global.getId();
		if(id != null) {
			out.write(" name=\"");
			encodeTextInXhtmlAttribute(id, out);
			out.write('"');
		}
		UrlUtils.writeSrc(pageContext, out, src, params, addLastModified, absolute, canonical);
		if(width != null) {
			out.write(" width=\"");
			Coercion.write(width, textInXhtmlAttributeEncoder, out);
			out.write('"');
		}
		if(height != null) {
			out.write(" height=\"");
			Coercion.write(height, textInXhtmlAttributeEncoder, out);
			out.write('"');
		}
		out.write(" frameborder=\"");
		out.write(frameborder ? '1' : '0');
		out.write("\">");
		Coercion.write(capturedBody, MarkupType.XHTML, out);
		out.write("</iframe>");
/* BodyTag only: */
		return EVAL_PAGE;
/**/
	}

/* BodyTag only: */
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