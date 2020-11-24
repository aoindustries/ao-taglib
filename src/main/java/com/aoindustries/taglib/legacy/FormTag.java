/*
 * ao-taglib - Making JSP be what it should have been all along.
 * Copyright (C) 2010, 2011, 2013, 2015, 2016, 2017, 2019, 2020  AO Industries, Inc.
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
import com.aoindustries.encoding.Doctype;
import static com.aoindustries.encoding.JavaScriptInXhtmlAttributeEncoder.javaScriptInXhtmlAttributeEncoder;
import com.aoindustries.encoding.MediaType;
import static com.aoindustries.encoding.TextInXhtmlAttributeEncoder.encodeTextInXhtmlAttribute;
import static com.aoindustries.encoding.TextInXhtmlAttributeEncoder.textInXhtmlAttributeEncoder;
import com.aoindustries.html.Html;
import com.aoindustries.html.servlet.HtmlEE;
import com.aoindustries.io.buffer.BufferResult;
import com.aoindustries.lang.Strings;
import com.aoindustries.net.AnyURI;
import com.aoindustries.net.MutableURIParameters;
import com.aoindustries.net.URIEncoder;
import com.aoindustries.net.URIParametersMap;
import com.aoindustries.net.URIParametersUtils;
import com.aoindustries.net.URIResolver;
import com.aoindustries.servlet.http.Dispatcher;
import com.aoindustries.servlet.jsp.LocalizedJspTagException;
import com.aoindustries.taglib.ActionAttribute;
import com.aoindustries.taglib.ApplicationResources;
import com.aoindustries.taglib.AttributeUtils;
import com.aoindustries.taglib.EnctypeAttribute;
import com.aoindustries.taglib.FormTagTEI;
import com.aoindustries.taglib.GlobalAttributesUtils;
import com.aoindustries.taglib.MethodAttribute;
import com.aoindustries.taglib.OnsubmitAttribute;
import com.aoindustries.taglib.ParamUtils;
import com.aoindustries.taglib.ParamsAttribute;
import com.aoindustries.taglib.TargetAttribute;
import com.aoindustries.util.i18n.MarkupType;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspTagException;

/**
 * @author  AO Industries, Inc.
 */
public class FormTag extends ElementBufferedBodyTag
	implements
		// Attributes
		ActionAttribute,
		EnctypeAttribute,
		MethodAttribute,
		ParamsAttribute,
		TargetAttribute,
		// Events
		OnsubmitAttribute
{

	public FormTag() {
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

	private String action;
	@Override
	public void setAction(String action) throws JspTagException {
		this.action = AttributeUtils.nullIfEmpty(action);
	}

	private String enctype;
	@Override
	public void setEnctype(String enctype) throws JspTagException {
		this.enctype = Strings.trimNullIfEmpty(enctype);
	}

	private String method;
	@Override
	public void setMethod(String method) throws JspTagException {
		method = Strings.trimNullIfEmpty(method);
		if(method != null && !FormTagTEI.isValidMethod(method)) throw new LocalizedJspTagException(ApplicationResources.accessor, "FormTag.method.invalid", method);
		this.method = method;
	}

	private MutableURIParameters params;
	@Override
	public void addParam(String name, String value) {
		if(params == null) params = new URIParametersMap();
		params.addParameter(name, value);
	}

	private String target;
	@Override
	public void setTarget(String target) throws JspTagException {
		this.target = Strings.trimNullIfEmpty(target);
	}

	private Object onsubmit;
	@Override
	public void setOnsubmit(Object onsubmit) throws JspTagException {
		this.onsubmit = AttributeUtils.trimNullIfEmpty(onsubmit);
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
		action = null;
		enctype = null;
		method = null;
		params = null;
		target = null;
		onsubmit = null;
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
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		HttpServletResponse response = (HttpServletResponse)pageContext.getResponse();
		Html html = HtmlEE.get(
			pageContext.getServletContext(),
			request,
			response,
			out
		);
		out.write("<form");
		GlobalAttributesUtils.writeGlobalAttributes(global, out);
		Map<String,List<String>> actionParams;
		if(action != null) {
			out.write(" action=\"");
			String encodedAction = URIResolver.getAbsolutePath(Dispatcher.getCurrentPagePath(request), action);
			if(encodedAction.startsWith("/")) {
				String contextPath = request.getContextPath();
				if(!contextPath.isEmpty()) encodedAction = contextPath + encodedAction;
			}
			encodedAction = response.encodeURL(URIEncoder.encodeURI(encodedAction));
			// The action attribute is everything up to the first question mark
			AnyURI actionURI = new AnyURI(encodedAction);
			actionParams = URIParametersUtils.of(actionURI.getQueryString()).getParameterMap();
			textInXhtmlAttributeEncoder.write(actionURI.setQueryString(null).toString(), out);
			out.write('"');
		} else {
			if(html.doctype != Doctype.HTML5) {
				// Action required before HTML 5
				out.write(" action=\"\"");
			}
			actionParams = null;
		}
		if(enctype != null) {
			out.write(" enctype=\"");
			encodeTextInXhtmlAttribute(enctype, out);
			out.write('"');
		}
		if(method != null) {
			out.write(" method=\"");
			out.write(method);
			out.write('"');
		}
		if(target != null) {
			out.write(" target=\"");
			encodeTextInXhtmlAttribute(target, out);
			out.write('"');
		}
		if(onsubmit != null) {
			out.write(" onsubmit=\"");
			Coercion.write(onsubmit, MarkupType.JAVASCRIPT, javaScriptInXhtmlAttributeEncoder, false, out);
			out.write('"');
		}
		out.write('>');
		// Automatically add URL request parameters as hidden fields to support custom URL rewritten parameters in GET requests.
		boolean didDiv = false;
		if(actionParams != null && !actionParams.isEmpty()) {
			for(Map.Entry<String,List<String>> entry : actionParams.entrySet()) {
				if(!didDiv) {
					out.write("<div>\n");
					didDiv = true;
				}
				String name = entry.getKey();
				for(String value : entry.getValue()) {
					html.input.hidden().name(name).value(value).__().nl();
				}
			}
		}
		// Write any parameters as hidden fields
		if(params != null) {
			for(Map.Entry<String,List<String>> entry : params.getParameterMap().entrySet()) {
				if(!didDiv) {
					out.write("<div>\n");
					didDiv = true;
				}
				String name = entry.getKey();
				List<String> paramValues = entry.getValue();
				assert !paramValues.isEmpty();
				for(String paramValue : paramValues) {
					html.input.hidden().name(name).value(paramValue).__().nl();
				}
			}
		}
		if(didDiv) out.write("</div>");
		Coercion.write(capturedBody, MarkupType.XHTML, out);
		out.write("</form>");
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