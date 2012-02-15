/*
 * aocode-public-taglib - Reusable Java taglib of general tools with minimal external dependencies.
 * Copyright (C) 2011  AO Industries, Inc.
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
import com.aoindustries.encoding.NewEncodingUtils;
import com.aoindustries.io.AutoTempFileWriter;
import com.aoindustries.net.EmptyParameters;
import com.aoindustries.net.HttpParameters;
import com.aoindustries.net.HttpParametersMap;
import com.aoindustries.net.HttpParametersUtils;
import com.aoindustries.util.EncodingUtils;
import java.io.IOException;
import java.io.Writer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * @author  AO Industries, Inc.
 */
public class IframeTag extends AutoEncodingBufferedTag implements IdAttribute, SrcAttribute, ParamsAttribute, WidthAttribute, HeightAttribute, FrameborderAttribute {

    private String id;
    private String src;
    private HttpParametersMap params;
    private String width;
    private String height;
    private boolean frameborder = true;

    @Override
    public MediaType getContentType() {
        return MediaType.XHTML;
    }

    @Override
    public MediaType getOutputType() {
        return MediaType.XHTML;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getSrc() {
        return src;
    }

    @Override
    public void setSrc(String src) {
        this.src = src;
    }

    @Override
    public HttpParameters getParams() {
        return params==null ? EmptyParameters.getInstance() : params;
    }

    @Override
    public void addParam(String name, String value) {
        if(params==null) params = new HttpParametersMap();
        params.addParameter(name, value);
    }

    @Override
    public String getWidth() {
        return width;
    }

    @Override
    public void setWidth(String width) {
        this.width = width;
    }

    @Override
    public String getHeight() {
        return height;
    }

    @Override
    public void setHeight(String height) {
        this.height = height;
    }

    @Override
    public boolean isFrameborder() {
        return frameborder;
    }

    @Override
    public void setFrameborder(boolean frameborder) {
        this.frameborder = frameborder;
    }

    @Override
    protected void doTag(AutoTempFileWriter capturedBody, Writer out) throws JspException, IOException {
        PageContext pageContext = (PageContext)getJspContext();
        HttpServletResponse response = (HttpServletResponse)pageContext.getResponse();
        if(src==null) throw new JspException(ApplicationResources.accessor.getMessage("IframeTag.src.required"));
        out.write("<iframe");
        if(id!=null) {
            out.write(" id=\"");
            EncodingUtils.encodeXmlAttribute(id, out);
            out.write('"');
        }
        out.write(" src=\"");
        if(src.startsWith("/")) {
            String contextPath = ((HttpServletRequest)pageContext.getRequest()).getContextPath();
            if(contextPath.length()>0) src = contextPath+src;
        }
        src = HttpParametersUtils.addParams(src, params);
        out.write(
            EncodingUtils.encodeXmlAttribute(
                response.encodeURL(
                    NewEncodingUtils.encodeUrlPath(src)
                )
            )
        );
        out.write('"');
        if(width!=null) {
            out.write(" width=\"");
            EncodingUtils.encodeXmlAttribute(width, out);
            out.write('"');
        }
        if(height!=null) {
            out.write(" height=\"");
            EncodingUtils.encodeXmlAttribute(height, out);
            out.write('"');
        }
        out.write(" frameborder=\"");
        out.write(frameborder ? '1' : '0');
        out.write("\">");
        capturedBody.writeTo(out);
        out.write("</iframe>");
    }
}