/*
 * ao-taglib - Making JSP be what it should have been all along.
 * Copyright (C) 2013, 2015, 2016, 2017  AO Industries, Inc.
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
import static com.aoindustries.encoding.TextInXhtmlAttributeEncoder.textInXhtmlAttributeEncoder;
import com.aoindustries.servlet.http.Dispatcher;
import com.aoindustries.servlet.http.ServletUtil;
import java.io.IOException;
import java.io.Writer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

/**
 * @author  AO Industries, Inc.
 */
public class BaseTag extends AutoEncodingNullTag {

	@Override
	public MediaType getOutputType() {
		return MediaType.XHTML;
	}

	@Override
	protected void doTag(Writer out) throws JspTagException, IOException {
		PageContext pageContext = (PageContext)getJspContext();
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		String originalPath = Dispatcher.getOriginalPagePath(request); // Before forward
		String currentPath = request.getServletPath(); // After forward
		if(originalPath != currentPath) { // Quick check for common case (string identity equals intentional)
			// When the paths do not match, request has been forwarded to a different directory and base is required
			int originalLastSlash = originalPath.lastIndexOf('/');
			int currentLastSlash = currentPath.lastIndexOf('/');
			// Only include base when in different directories
			if(
				originalLastSlash!=currentLastSlash
				|| !originalPath.regionMatches(0, currentPath, 0, originalLastSlash)
			) {
				out.write("<base href=\"");
				ServletUtil.getAbsoluteURL(request, currentPath.substring(0, currentLastSlash+1), textInXhtmlAttributeEncoder, out);
				out.write("\" />");
			}
		}
	}
}
