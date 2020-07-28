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
package com.aoindustries.taglib;

import com.aoindustries.encoding.Doctype;
import com.aoindustries.encoding.MediaType;
import com.aoindustries.encoding.Serialization;
import static com.aoindustries.encoding.TextInXhtmlAttributeEncoder.encodeTextInXhtmlAttribute;
import com.aoindustries.encoding.servlet.DoctypeEE;
import com.aoindustries.encoding.servlet.SerializationEE;
import com.aoindustries.html.Html;
import com.aoindustries.servlet.ServletUtil;
import com.aoindustries.web.resources.registry.Registry;
import com.aoindustries.web.resources.servlet.RegistryEE;
import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

public class HtmlTag extends AutoEncodingFilteredBodyTag {

	/**
	 * The old Struts XHTML mode page attribute.  To avoiding picking-up a big
	 * legacy dependency, we've copied the value here instead of depending on
	 * Globals.  Once we no longer have any code running on old Struts, this
	 * value may be removed.
	 */
	private static final String STRUTS_XHTML_KEY = "org.apache.struts.globals.XHTML";

	public static void beginHtmlTag(Locale locale, Appendable out, Serialization serialization, String clazz) throws IOException {
		out.append("<html");
		if(serialization == Serialization.XML) {
			out.append(" xmlns=\"http://www.w3.org/1999/xhtml\"");
		}
		if(clazz!=null) {
			out.append(" class=\"");
			encodeTextInXhtmlAttribute(clazz, out);
			out.append('"');
		}
		if(locale != null) {
			String lang = locale.toLanguageTag();
			if(!lang.isEmpty()) {
				out.append(" lang=\"");
				encodeTextInXhtmlAttribute(lang, out);
				out.append('"');
				if(serialization == Serialization.XML) {
					out.append(" xml:lang=\"");
					encodeTextInXhtmlAttribute(lang, out);
					out.append('"');
				}
			}
		}
		out.append('>');
	}

	public static void beginHtmlTag(ServletResponse response, Appendable out, Serialization serialization, String clazz) throws IOException {
		beginHtmlTag(response.getLocale(), out, serialization, clazz);
	}

	public static void endHtmlTag(Appendable out) throws IOException {
		out.append("</html>");
	}

	@Override
	public MediaType getContentType() {
		return MediaType.XHTML;
	}

	private static final long serialVersionUID = 1L;

	// TODO: charset here, along with:
	//       Page (model), Page (Servlet), PageTag, Theme, View
	//       aoweb-framework: WebPage, WebPageLayout
	//       aoweb-struts: PageAttributes, Skin

	private transient Serialization serialization;
	public void setSerialization(String serialization) {
		if(serialization == null) {
			this.serialization = null;
		} else {
			serialization = serialization.trim();
			this.serialization = (serialization.isEmpty() || "auto".equalsIgnoreCase(serialization)) ? null : Serialization.valueOf(serialization.toUpperCase(Locale.ROOT));
		}
	}

	private transient Doctype doctype;
	public void setDoctype(String doctype) {
		if(doctype == null) {
			this.doctype = null;
		} else {
			doctype = doctype.trim();
			this.doctype = (doctype.isEmpty() || "default".equalsIgnoreCase(doctype)) ? null : Doctype.valueOf(doctype.toUpperCase(Locale.ROOT));
		}
	}

	private transient String clazz;
	/**
	 * Getter required because without it, we get the exception:
	 * <pre>Unable to find setter method for attribute: class</pre>
	 */
	public String getClazz() {
		return clazz;
	}
	public void setClazz(String clazz) {
		this.clazz = AttributeUtils.trimNullIfEmpty(clazz);
	}

	// Values that are used in doFinally
	private transient Serialization oldSerialization;
	private transient Object oldStrutsXhtml;
	private transient boolean setSerialization;
	private transient Doctype oldDoctype;
	private transient boolean setDoctype;
	private transient Registry oldPageRegistry;

	@Override
	protected int doStartTag(Writer out) throws JspException, IOException {
		// Clear values that are used in doFinally
		oldSerialization = null;
		oldStrutsXhtml = null;
		setSerialization = false;
		oldDoctype = null;
		setDoctype = false;
		oldPageRegistry = null;

		try {
			ServletContext servletContext = pageContext.getServletContext();
			HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();

			Serialization currentSerialization = serialization;
			if(currentSerialization == null) {
				currentSerialization = SerializationEE.get(servletContext, request);
				oldSerialization = null;
				oldStrutsXhtml = null;
				setSerialization = false;
			} else {
				oldSerialization = SerializationEE.replace(request, currentSerialization);
				oldStrutsXhtml = pageContext.getAttribute(STRUTS_XHTML_KEY, PageContext.PAGE_SCOPE);
				pageContext.setAttribute(STRUTS_XHTML_KEY, Boolean.toString(currentSerialization == Serialization.XML), PageContext.PAGE_SCOPE);
				setSerialization = true;
			}
			Doctype currentDoctype = doctype;
			if(currentDoctype == null) {
				currentDoctype = DoctypeEE.get(servletContext, request);
				oldDoctype = null;
				setDoctype = false;
			} else {
				oldDoctype = DoctypeEE.replace(request, currentDoctype);
				setDoctype = true;
			}
			oldPageRegistry = RegistryEE.Page.get(request);
			if(oldPageRegistry == null) {
				// Create a new page-scope registry
				RegistryEE.Page.set(request, new Registry());
			}
			ServletResponse response = pageContext.getResponse();
			// Clear the output buffer
			response.resetBuffer();
			// Set the content type
			final String documentEncoding = Html.ENCODING.name();
			ServletUtil.setContentType(response, currentSerialization.getContentType(), documentEncoding);
			// Write doctype
			currentDoctype.xmlDeclaration(currentSerialization, documentEncoding, out);
			currentDoctype.doctype(currentSerialization, out);
			// Write <html>
			beginHtmlTag(response, out, currentSerialization, clazz);
			return EVAL_BODY_FILTERED;
		} catch(ServletException e) {
			throw new JspTagException(e);
		}
	}

	@Override
	protected int doEndTag(Writer out) throws JspException, IOException {
		// Write </html>
		endHtmlTag(out);
		return EVAL_PAGE;
	}

	@Override
	public void doFinally() {
		try {
			ServletRequest request = pageContext.getRequest();
			if(setSerialization) {
				SerializationEE.set(request, oldSerialization);
				pageContext.setAttribute(STRUTS_XHTML_KEY, oldStrutsXhtml, PageContext.PAGE_SCOPE);
			}
			if(setDoctype) DoctypeEE.set(request, oldDoctype);
			if(oldPageRegistry == null) {
				RegistryEE.Page.set(request, null);
			}
		} finally {
			super.doFinally();
		}
	}
}
