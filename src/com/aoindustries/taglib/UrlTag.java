/*
 * aocode-public-taglib - Reusable Java taglib of general tools with minimal external dependencies.
 * Copyright (C) 2009, 2010, 2011  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of aocode-public-taglib.
 *
 * aocode-public-taglib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aocode-public-taglib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with aocode-public-taglib.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.taglib;

import com.aoindustries.encoding.MediaType;
import com.aoindustries.io.AutoTempFileWriter;
import java.io.IOException;
import java.io.Writer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * TODO: Have absolute option
 * TODO: Support parameters nested
 * TODO: Allow use in JavaScript with encoding like ao:text
 * TODO: Replace uses of ao:text with this as it will then be more appropriate for sending dynamic parameters to JavaScript.
 *
 * @author  AO Industries, Inc.
 */
public class UrlTag extends AutoEncodingBufferedTag {

    @Override
    public MediaType getContentType() {
        return MediaType.URL;
    }

    @Override
    public MediaType getOutputType() {
        return MediaType.URL;
    }

    @Override
    protected void doTag(AutoTempFileWriter capturedBody, Writer out) throws JspException, IOException {
        String url = capturedBody.toString().trim();
        if(url.startsWith("/")) {
            PageContext pageContext = (PageContext)getJspContext();
            out.write(((HttpServletRequest)pageContext.getRequest()).getContextPath());
        }
        out.write(url);
    }
}
