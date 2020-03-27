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
package com.aoindustries.taglib;

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
import static com.aoindustries.taglib.ApplicationResources.accessor;
import com.aoindustries.util.i18n.MarkupType;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.DynamicAttributes;

/**
 * @author  AO Industries, Inc.
 */
public class FormTag
	extends AutoEncodingBufferedTag
	implements
		DynamicAttributes,
		MethodAttribute,
		IdAttribute,
		ActionAttribute,
		ParamsAttribute,
		TargetAttribute,
		EnctypeAttribute,
		ClassAttribute,
		StyleAttribute,
		OnsubmitAttribute
{

	public static boolean isValidMethod(String method) {
		return
			"get".equals(method)
			|| "post".equals(method)
		;
	}

	private String method = "get";
	private String id;
	private String action;
	private MutableURIParameters params;
	private String target;
	private String enctype;
	private String clazz;
	private Object style;
	private Object onsubmit;

	@Override
	public MediaType getContentType() {
		return MediaType.XHTML;
	}

	@Override
	public MediaType getOutputType() {
		return MediaType.XHTML;
	}

	@Override
	public void setMethod(String method) throws JspTagException {
		method = method.trim();
		if(!isValidMethod(method)) throw new LocalizedJspTagException(ApplicationResources.accessor, "FormTag.method.invalid", method);
		this.method = method;
	}

	@Override
	public void setId(String id) throws JspTagException {
		this.id = Strings.trimNullIfEmpty(id);
	}

	@Override
	public void setAction(String action) throws JspTagException {
		this.action = AttributeUtils.nullIfEmpty(action);
	}

	@Override
	public void addParam(String name, String value) {
		if(params==null) params = new URIParametersMap();
		params.addParameter(name, value);
	}

	@Override
	public void setTarget(String target) throws JspTagException {
		this.target = Strings.trimNullIfEmpty(target);
	}

	@Override
	public void setEnctype(String enctype) throws JspTagException {
		this.enctype = Strings.trimNullIfEmpty(enctype);
	}

	@Override
	public String getClazz() {
		return clazz;
	}

	@Override
	public void setClazz(String clazz) throws JspTagException {
		this.clazz = Strings.trimNullIfEmpty(clazz);
	}

	@Override
	public void setStyle(Object style) throws JspTagException {
		this.style = AttributeUtils.trimNullIfEmpty(style);
	}

	@Override
	public void setOnsubmit(Object onsubmit) throws JspTagException {
		this.onsubmit = AttributeUtils.trimNullIfEmpty(onsubmit);
	}

	@Override
	public void setDynamicAttribute(String uri, String localName, Object value) throws JspTagException {
		if(
			uri==null
			&& localName.startsWith(ParamUtils.PARAM_ATTRIBUTE_PREFIX)
		) {
			ParamUtils.setDynamicAttribute(this, uri, localName, value);
		} else {
			throw new LocalizedJspTagException(
				accessor,
				"error.unexpectedDynamicAttribute",
				localName,
				ParamUtils.PARAM_ATTRIBUTE_PREFIX+"*"
			);
		}
	}

	@Override
	protected void doTag(BufferResult capturedBody, Writer out) throws JspTagException, IOException {
		PageContext pageContext = (PageContext)getJspContext();
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		HttpServletResponse response = (HttpServletResponse)pageContext.getResponse();
		Html html = HtmlEE.get(
			pageContext.getServletContext(),
			request,
			response,
			out
		);
		out.write("<form method=\"");
		out.write(method);
		out.write('"');
		if(id!=null) {
			out.write(" id=\"");
			encodeTextInXhtmlAttribute(id, out);
			out.write('"');
		}
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
		if(target!=null) {
			out.write(" target=\"");
			encodeTextInXhtmlAttribute(target, out);
			out.write('"');
		}
		if(enctype!=null) {
			out.write(" enctype=\"");
			encodeTextInXhtmlAttribute(enctype, out);
			out.write('"');
		}
		if(clazz!=null) {
			out.write(" class=\"");
			encodeTextInXhtmlAttribute(clazz, out);
			out.write('"');
		}
		if(style!=null) {
			out.write(" style=\"");
			Coercion.write(style, textInXhtmlAttributeEncoder, out);
			out.write('"');
		}
		if(onsubmit!=null) {
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
				for(String paramValue : entry.getValue()) {
					html.input.hidden().name(name).value(paramValue).__().nl();
				}
			}
		}
		if(didDiv) out.write("</div>");
		Coercion.write(capturedBody, MarkupType.XHTML, out);
		out.write("</form>");
	}
}
