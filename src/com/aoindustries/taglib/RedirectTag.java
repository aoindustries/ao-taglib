/*
 * aocode-public-taglib - Reusable Java taglib of general tools with minimal external dependencies.
 * Copyright (C) 2010, 2011, 2012, 2013  AO Industries, Inc.
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

import com.aoindustries.util.WildcardPatternMatcher;
import com.aoindustries.net.HttpParametersUtils;
import com.aoindustries.servlet.http.LastModifiedServlet;
import com.aoindustries.servlet.http.ServletUtil;
import com.aoindustries.servlet.jsp.LocalizedJspTagException;
import static com.aoindustries.taglib.ApplicationResources.accessor;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.SkipPageException;

/**
 * @author  AO Industries, Inc.
 */
public class RedirectTag
	extends DispatchTag
	implements HrefAttribute
{

	private static final Logger logger = Logger.getLogger(RedirectTag.class.getName());

	/**
	 * The maximum length of a URL allowed for redirect.
	 *
	 * Matching limit of Internet Explorer: http://support.microsoft.com/kb/208427
	 *
	 * @see <a href="http://www.boutell.com/newfaq/misc/urllength.html">WWW FAQs: What is the maximum length of a URL?</a>
	 */
	private static final int MAXIMUM_GET_REQUEST_LENGTH = 2048;

	public static boolean isValidStatusCode(String statusCode) {
        return
            "moved_permanently".equals(statusCode)
            || "permanent".equals(statusCode)
            || "301".equals(statusCode)
            || "moved_temporarily".equals(statusCode)
            || "found".equals(statusCode)
            || "temporary".equals(statusCode)
            || "302".equals(statusCode)
            || "see_other".equals(statusCode)
            || "303".equals(statusCode)
        ;
    }

	private String statusCode;
    private String href;
	private LastModifiedServlet.AddLastModifiedWhen addLastModified = LastModifiedServlet.AddLastModifiedWhen.AUTO;

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) throws JspTagException {
        if(!isValidStatusCode(statusCode)) throw new LocalizedJspTagException(ApplicationResources.accessor, "RedirectTag.statusCode.invalid", statusCode);
        this.statusCode = statusCode;
    }

	@Override
    public String getHref() {
        return href;
    }

    @Override
    public void setHref(String href) {
        this.href = href;
    }

	public String getAddLastModified() {
		return addLastModified.getLowerName();
	}

	public void setAddLastModified(String addLastModified) {
		this.addLastModified = LastModifiedServlet.AddLastModifiedWhen.valueOfLowerName(addLastModified);
	}

	@Override
	protected WildcardPatternMatcher getClearParamsMatcher() {
		return WildcardPatternMatcher.getMatchAll();
	}

	@Override
	protected String getDynamicAttributeExceptionKey() {
		return "error.unexpectedDynamicAttribute";
	}
	
	@Override
	protected Serializable[] getDynamicAttributeExceptionArgs(String localName) {
		return new Serializable[] {
			localName,
			ParamUtils.PARAM_ATTRIBUTE_PREFIX+"*"
		};
	}

	/**
	 * args will be set to <code>null</code> to simulate initial request conditions of a redirect
	 * as well as possible.
	 */
	@Override
	protected Map<String, Object> getArgs() {
		return null;
	}

	@Override
    protected void doTag(String servletPath) throws IOException, JspTagException, SkipPageException {
        final int status;
        if(statusCode==null) throw new AttributeRequiredException("statusCode");
        if(
			"301".equals(statusCode)
			|| "moved_permanently".equals(statusCode)
			|| "permanent".equals(statusCode)
		) {
            status = HttpServletResponse.SC_MOVED_PERMANENTLY;
        } else if(
			"302".equals(statusCode)
			|| "moved_temporarily".equals(statusCode)
			|| "found".equals(statusCode)
			|| "temporary".equals(statusCode)
		) {
            status = HttpServletResponse.SC_MOVED_TEMPORARILY;
        } else if(
			"303".equals(statusCode)
			|| "see_other".equals(statusCode)
		) {
            status = HttpServletResponse.SC_SEE_OTHER;
        } else {
            throw new AssertionError("Unexpected value for statusCode: "+statusCode);
        }

		final PageContext pageContext = (PageContext)getJspContext();
		final HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		final HttpServletResponse response = (HttpServletResponse)pageContext.getResponse();

        // Add any parameters to the URL
		String myHref = href;
		if(myHref==null) myHref = page; // Default to page when href not given
        if(myHref==null) throw new AttributeRequiredException("href");
        myHref = HttpParametersUtils.addParams(myHref, params);
		myHref = LastModifiedServlet.addLastModified(pageContext.getServletContext(), myHref, addLastModified);

		// Get the full URL that will be used for the redirect
		String location = ServletUtil.getRedirectLocation(request, response, servletPath, myHref);
		boolean isTooLong = location.length()>MAXIMUM_GET_REQUEST_LENGTH;
		if(!isTooLong || page==null) {
			if(isTooLong) {
				// Warn about location too long
				if(logger.isLoggable(Level.WARNING)) {
					logger.warning(
						accessor.getMessage(
							"RedirectTag.locationTooLongWarning",
							MAXIMUM_GET_REQUEST_LENGTH,
							location.length(),
							location.substring(0, 100)
						)
					);
				}
			}
	        ServletUtil.sendRedirect(response, location, status);
		    throw new SkipPageException();
		} else {
			// Set no-cache header for 302 and 303 redirect
			if(
				status==HttpServletResponse.SC_MOVED_TEMPORARILY
				|| status==HttpServletResponse.SC_SEE_OTHER
			) {
				response.setHeader("Cache-Control", "no-cache");
			}
		}
    }
	
	/**
	 * Dispatch as forward
	 */
    @Override
    void dispatch(RequestDispatcher dispatcher, JspWriter out, HttpServletRequest request, HttpServletResponse response) throws JspException, IOException {
		Boolean oldForwarded = requestForwarded.get();
		try {
			requestForwarded.set(Boolean.TRUE);
			try {
				// Clear the previous JSP out buffer
				out.clear();
				dispatcher.forward(request, response);
			} catch(ServletException e) {
				throw new JspTagException(e);
			}
			throw new SkipPageException();
		} finally {
			requestForwarded.set(oldForwarded);
		}
    }
}
