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
package com.aoindustries.taglib;

import com.aoindustries.encoding.MediaType;
import com.aoindustries.html.servlet.HtmlEE;
import java.io.IOException;
import java.io.Writer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

/**
 * @author  AO Industries, Inc.
 */
public class BrTag
	extends AutoEncodingNullTag
	implements
		IdAttribute,
		ClassAttribute,
		StyleAttribute
{

	@Override
	public MediaType getOutputType() {
		return MediaType.XHTML;
	}

	private String id;
	@Override
	public void setId(String id) throws JspTagException {
		this.id = id;
	}

	private String clazz;
	@Override
	public String getClazz() {
		return clazz;
	}
	@Override
	public void setClazz(String clazz) throws JspTagException {
		this.clazz = clazz;
	}

	private Object style;
	@Override
	public void setStyle(Object style) throws JspTagException {
		this.style = style;
	}

	@Override
	protected void doTag(Writer out) throws JspTagException, IOException {
		PageContext pageContext = (PageContext)getJspContext();
		HtmlEE.get(
			pageContext.getServletContext(),
			(HttpServletRequest)pageContext.getRequest(),
			(HttpServletResponse)pageContext.getResponse(),
			out
		).br()
			.id(id)
			.clazz(clazz)
			.style(style)
			.__();
	}
}
