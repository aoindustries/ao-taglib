/*
 * ao-taglib - Making JSP be what it should have been all along.
 * Copyright (C) 2019, 2020  AO Industries, Inc.
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
import com.aoindustries.html.Area;
import com.aoindustries.html.Html;
import com.aoindustries.html.servlet.HtmlEE;
import com.aoindustries.lang.Strings;
import com.aoindustries.net.MutableURIParameters;
import com.aoindustries.net.URIParametersMap;
import com.aoindustries.servlet.jsp.LocalizedJspTagException;
import com.aoindustries.servlet.lastmodified.AddLastModified;
import com.aoindustries.taglib.AltAttribute;
import static com.aoindustries.taglib.ApplicationResources.accessor;
import com.aoindustries.taglib.AreaTagTEI;
import com.aoindustries.taglib.AttributeRequiredException;
import com.aoindustries.taglib.AttributeUtils;
import com.aoindustries.taglib.GlobalAttributesUtils;
import com.aoindustries.taglib.HrefAttribute;
import com.aoindustries.taglib.HreflangAttribute;
import com.aoindustries.taglib.OnclickAttribute;
import com.aoindustries.taglib.OnmouseoutAttribute;
import com.aoindustries.taglib.OnmouseoverAttribute;
import com.aoindustries.taglib.ParamUtils;
import com.aoindustries.taglib.ParamsAttribute;
import com.aoindustries.taglib.RelAttribute;
import com.aoindustries.taglib.TargetAttribute;
import com.aoindustries.taglib.TitleAttribute;
import com.aoindustries.taglib.TypeAttribute;
import com.aoindustries.taglib.UrlUtils;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspTagException;

/**
 * @author  AO Industries, Inc.
 */
public class AreaTag extends ElementNullBodyTag
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

	public AreaTag() {
		init();
	}

	@Override
	public MediaType getOutputType() {
		return MediaType.XHTML;
	}

/* BodyTag only: */
	private static final long serialVersionUID = 1L;
/**/

	private String shape;
	public void setShape(String shape) throws JspTagException {
		shape = shape.trim();
		if(shape.isEmpty()) {
			throw new AttributeRequiredException("shape");
		} else if(!AreaTagTEI.isValidShape(shape)) {
			throw new LocalizedJspTagException(
				accessor,
				"AreaTag.shape.invalid",
				shape
			);
		}
		this.shape = shape;
	}

	private String coords;
	public void setCoords(String coords) {
		this.coords = AttributeUtils.trimNullIfEmpty(coords);
	}

	private String href;
	@Override
	public void setHref(String href) throws JspTagException {
		this.href = AttributeUtils.nullIfEmpty(href);
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

	private Object hreflang;
	@Override
	public void setHreflang(Object hreflang) throws JspTagException {
		this.hreflang = hreflang;
	}

	private String rel;
	@Override
	public void setRel(String rel) throws JspTagException {
		this.rel = rel;
	}

	private String type;
	@Override
	public void setType(String type) throws JspTagException {
		this.type = Strings.trimNullIfEmpty(type);
	}

	private String target;
	@Override
	public void setTarget(String target) throws JspTagException {
		this.target = Strings.trimNullIfEmpty(target);
	}

	private Object alt;
	@Override
	public void setAlt(Object alt) throws JspTagException {
		this.alt = AttributeUtils.trim(alt);
	}

	private Object title;
	@Override
	public void setTitle(Object title) throws JspTagException {
		this.title = AttributeUtils.trimNullIfEmpty(title);
	}

	private Object onclick;
	@Override
	public void setOnclick(Object onclick) throws JspTagException {
		this.onclick = AttributeUtils.trimNullIfEmpty(onclick);
	}

	private Object onmouseover;
	@Override
	public void setOnmouseover(Object onmouseover) throws JspTagException {
		this.onmouseover = AttributeUtils.trimNullIfEmpty(onmouseover);
	}

	private Object onmouseout;
	@Override
	public void setOnmouseout(Object onmouseout) throws JspTagException {
		this.onmouseout = AttributeUtils.trimNullIfEmpty(onmouseout);
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
/* BodyTag only: */
	protected int doEndTag(Writer out) throws JspTagException, IOException {
/**/
/* SimpleTag only:
	protected void doTag(Writer out) throws JspTagException, IOException {
		PageContext pageContext = (PageContext)getJspContext();
/**/
		if(shape == null) throw new AttributeRequiredException("shape");
		if(!"default".equals(shape)) {
			if(coords == null) throw new AttributeRequiredException("coords");
		}
		if(href != null) {
			if(alt == null) throw new AttributeRequiredException("alt");
		}
		Html html = HtmlEE.get(
			pageContext.getServletContext(),
			(HttpServletRequest)pageContext.getRequest(),
			(HttpServletResponse)pageContext.getResponse(),
			out
		);
		Area area = GlobalAttributesUtils.doGlobalAttributes(global, html.area())
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
			// TODO: target to Area
			.attribute("target", target);
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