/*
 * ao-taglib - Making JSP be what it should have been all along.
 * Copyright (C) 2019, 2020, 2021  AO Industries, Inc.
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
import com.aoapps.html.servlet.AREA;
import com.aoapps.html.servlet.DocumentEE;
import com.aoapps.lang.Coercion;
import com.aoapps.lang.LocalizedIllegalArgumentException;
import com.aoapps.lang.Strings;
import com.aoapps.lang.i18n.Resources;
import com.aoapps.net.MutableURIParameters;
import com.aoapps.net.URIParametersMap;
import com.aoapps.servlet.lastmodified.AddLastModified;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

/**
 * @author  AO Industries, Inc.
 */
public class AreaTag extends ElementNullTag
	implements
		// Attributes
		HrefAttribute,
		ParamsAttribute,
		HreflangAttribute,
		RelAttribute,
		TypeAttribute,
		TargetAttribute,
		AltAttribute,
		TitleAttribute,
		// Events
		OnclickAttribute,
		OnmouseoverAttribute,
		OnmouseoutAttribute
{

/* SimpleTag only: */
	public static final Resources RESOURCES = Resources.getResources(ResourceBundle::getBundle, AreaTag.class);
/**/

	public AreaTag() {
		init();
	}

	@Override
	public MediaType getOutputType() {
		return MediaType.XHTML;
	}

/* BodyTag only:
	private static final long serialVersionUID = 1L;
/**/

	private String shape;
	public void setShape(String shape) {
		shape = shape.trim();
		if(!AreaTagTEI.isValidShape(shape)) {
			throw new LocalizedIllegalArgumentException(RESOURCES, "shape.invalid", shape);
		}
		this.shape = shape;
	}

	private String coords;
	public void setCoords(String coords) {
		this.coords = Strings.trimNullIfEmpty(coords);
	}

	private String href;
	@Override
	public void setHref(String href) {
		this.href = Strings.nullIfEmpty(href);
	}

	private MutableURIParameters params;
	@Override
	public void addParam(String name, Object value) {
		if(params == null) params = new URIParametersMap();
		params.add(name, value);
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

	private Object hreflang;
	@Override
	public void setHreflang(Object hreflang) {
		this.hreflang = hreflang;
	}

	private String rel;
	@Override
	public void setRel(String rel) {
		this.rel = rel;
	}

	private String type;
	@Override
	public void setType(String type) {
		this.type = Strings.trimNullIfEmpty(type);
	}

	private String target;
	@Override
	public void setTarget(String target) {
		this.target = Strings.trimNullIfEmpty(target);
	}

	private Object alt;
	@Override
	public void setAlt(Object alt) {
		this.alt = AttributeUtils.trim(alt);
	}

	private Object title;
	@Override
	public void setTitle(Object title) {
		this.title = AttributeUtils.trimNullIfEmpty(title);
	}

	private Object onclick;
	@Override
	public void setOnclick(Object onclick) {
		this.onclick = AttributeUtils.trimNullIfEmpty(onclick);
	}

	private Object onmouseover;
	@Override
	public void setOnmouseover(Object onmouseover) {
		this.onmouseover = AttributeUtils.trimNullIfEmpty(onmouseover);
	}

	private Object onmouseout;
	@Override
	public void setOnmouseout(Object onmouseout) {
		this.onmouseout = AttributeUtils.trimNullIfEmpty(onmouseout);
	}

	/**
	 * @see  ParamUtils#addDynamicAttribute(java.lang.String, java.lang.String, java.lang.Object, java.util.List, com.aoapps.taglib.ParamsAttribute)
	 */
	@Override
	protected boolean addDynamicAttribute(String uri, String localName, Object value, List<String> expectedPatterns) throws JspTagException {
		return
			super.addDynamicAttribute(uri, localName, value, expectedPatterns)
			|| ParamUtils.addDynamicAttribute(uri, localName, value, expectedPatterns, this);
	}

	private void init() {
		shape = null;
		coords = null;
		href = null;
		params = null;
		absolute = false;
		canonical = false;
		addLastModified = AddLastModified.AUTO;
		hreflang = null;
		rel = null;
		type = null;
		target = null;
		alt = null;
		title = null;
		onclick = null;
		onmouseover = null;
		onmouseout = null;
	}

	@Override
/* BodyTag only:
	protected int doEndTag(Writer out) throws JspException, IOException {
/**/
/* SimpleTag only: */
	protected void doTag(Writer out) throws JspException, IOException {
		PageContext pageContext = (PageContext)getJspContext();
/**/
		if(shape == null) throw new AttributeRequiredException("shape");
		if(!"default".equals(shape)) {
			if(coords == null) throw new AttributeRequiredException("coords");
		}
		if(href != null) {
			if(alt == null) throw new AttributeRequiredException("alt");
		}
		DocumentEE document = new DocumentEE(
			pageContext.getServletContext(),
			(HttpServletRequest)pageContext.getRequest(),
			(HttpServletResponse)pageContext.getResponse(),
			out,
			false, // Do not add extra newlines to JSP
			false  // Do not add extra indentation to JSP
		);
		AREA<?> area = GlobalAttributesUtils.doGlobalAttributes(global, document.area())
			.shape(shape)
			.coords(coords)
			.href(UrlUtils.getHref(pageContext, href, params, addLastModified, absolute, canonical));
		if(hreflang instanceof Locale) {
			area.hreflang((Locale)hreflang);
		} else {
			hreflang = AttributeUtils.trimNullIfEmpty(hreflang);
			area.hreflang(Coercion.toString(hreflang));
		}
		area
			.rel(rel)
			// TODO: type to Area (or remove entirely since this part of the standard is uncertain and currently unimplemented by all browsers?)
			.attribute("type", type)
			.target(target);
		// non-existing alt is OK when there is no href (with href: "" stays "")
		if(
			alt != null
			&& (href != null || !Coercion.isEmpty(alt))
		) {
			area.alt(alt);
		}
		area
			.title(title)
			.onclick(onclick)
			.onmouseover(onmouseover)
			.onmouseout(onmouseout)
			.__();
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
