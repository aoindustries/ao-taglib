/*
 * aocode-public-taglib - Reusable Java taglib of general tools with minimal external dependencies.
 * Copyright (C) 2012, 2013  AO Industries, Inc.
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

import com.aoindustries.lang.LocalizedIllegalArgumentException;
import com.aoindustries.net.EmptyParameters;
import com.aoindustries.net.HttpParameters;
import com.aoindustries.net.HttpParametersMap;
import com.aoindustries.servlet.http.ServletUtil;
import com.aoindustries.servlet.jsp.LocalizedJspException;
import static com.aoindustries.taglib.ApplicationResources.accessor;
import com.aoindustries.util.StringUtility;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * The parent implementation for both the forward and include tags.
 *
 * @author  AO Industries, Inc.
 */
abstract class DispatchTag
	extends SimpleTagSupport
	implements
		DynamicAttributes,
		PageAttribute,
		ParamsAttribute,
		ArgsAttribute
{

	private static final String ARG_ATTRIBUTE_NAME = "arg";

	/**
	 * Tracks the dispatch of pages for correct page-relative paths.
	 */
	private static final ThreadLocal<List<String>> dispatchedPages = new ThreadLocal<List<String>>() {
		@Override
		protected List<String> initialValue() {
			return new ArrayList<String>();
		}
	};

	private String page;
    private String clearParams = null;
    private HttpParametersMap params;
	private Map<String,Object> args;

    @Override
    public String getPage() {
        return page;
    }

    @Override
    public void setPage(String page) {
        this.page = page;
    }

    public String getClearParams() {
        return clearParams;
    }

    public void setClearParams(String clearParams) {
        this.clearParams = clearParams;
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
	public Map<String, Object> getArgs() {
		if(args==null) return Collections.emptyMap();
		return args;
	}

	@Override
	public void addArg(String name, Object value) throws IllegalArgumentException {
		if(args==null) {
			args = new LinkedHashMap<String,Object>();
		} else if(args.containsKey(name)) {
			throw new LocalizedIllegalArgumentException(accessor, "DispatchTag.addArg.duplicateArgument", name);
		}
		args.put(name, value);
	}

	@Override
	public void setDynamicAttribute(String uri, String localName, Object value) throws JspException {
		if(uri==null) {
			addArg(localName, value);
		} else {
			throw new LocalizedJspException(accessor, "MessageTag.unexpectedDynamicAttribute", localName);
		}
	}

	/**
	 * Determines if a parameter name is filtered by the set of filter rules.
	 * Supports:
	 *   *            Match all
	 *   *suffix      Suffix match
	 *   prefix*      Prefix match
	 *   exact_value  Exact match
	 */
	private static boolean isFiltered(List<String> clearParamNames, String paramName) {
		for(String filter : clearParamNames) {
			final int filterLen = filter.length();
			if(filterLen>0) {
				char chFirst = filter.charAt(0);
				if(filterLen==1 && chFirst=='*') {
					// Match all
					return true;
				}
				char chLast = filter.charAt(filterLen-1);
				if(chFirst=='*') {
					if(chLast=='*') {
						// *error*
						throw new LocalizedIllegalArgumentException(accessor, "DispatchTag.invalidParameterFilter", filter);
					} else {
						// Suffix match
						final int paramNameLen = paramName.length();
						if(
							paramNameLen >= (filterLen-1)
							&& paramName.regionMatches(
								paramNameLen-(filterLen-1),
								filter,
								1,
								filterLen-1
							)
							//paramName.endsWith(filter.substring(1))
						) return true;
					}
				} else {
					if(chLast=='*') {
						// Prefix match
						if(
							paramName.length() >= (filterLen-1)
							&& paramName.regionMatches(
								0,
								filter,
								0,
								filterLen-1
							)
							//paramName.startsWith(filter.substring(0, filterLen-1))
						) return true;
					} else {
						// Exact match
						if(paramName.equals(filter)) return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
    final public void doTag() throws IOException, JspException {
		PageContext pageContext = (PageContext)getJspContext();
		JspWriter out = pageContext.getOut();
		JspFragment body = getJspBody();
		if(body!=null) {
			Writer newOut = getJspFragmentWriter(out);
			// Check for JspWriter to avoid a JspWriter wrapping a JspWriter
			body.invoke(
				newOut==out
				? null
				: newOut
			);
		}
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		HttpServletResponse response = (HttpServletResponse)pageContext.getResponse();
		if(page==null) throw new AttributeRequiredException("page");
		List<String> myDispatchedPages = DispatchTag.dispatchedPages.get();
		// Find the current JSP page
		final String currentPage;
		// Make relative to current JSP page
		final String contextRelativePath = ServletUtil.getAbsolutePath(
			myDispatchedPages.isEmpty()
				? request.getServletPath()
				: myDispatchedPages.get(myDispatchedPages.size()-1),
			page
		);
		// Store as new relative path source
		myDispatchedPages.add(contextRelativePath);
		try {
			RequestDispatcher dispatcher = pageContext.getServletContext().getRequestDispatcher(contextRelativePath);

			// Keep old arguments to restore
			final Object oldArgs = request.getAttribute(ARG_ATTRIBUTE_NAME);
			try {
				// Set new arguments
				request.setAttribute(
					ARG_ATTRIBUTE_NAME,
					args==null
						? Collections.emptyMap()
						: Collections.unmodifiableMap(args)
				);

				// Determine parameters to clear
				final List<String> clearParamNames;
				if(clearParams==null || clearParams.isEmpty()) clearParamNames = Collections.emptyList();
				else clearParamNames = StringUtility.splitStringCommaSpace(clearParams);

				Map<String,String[]> oldMap = null; // Obtained when first needed

				// If no parameters have been added
				if(params==null) {
					// And if there is no clearParamNames, then no need to wrap
					if(clearParamNames.isEmpty()) {
						// No need to wrap, use old request
						dispatch(dispatcher, out, request, response);
						return;
					}

					// And if there are no parameters on the request, then no need to wrap
					if(oldMap==null) oldMap = request.getParameterMap();
					if(oldMap.isEmpty()) {
						// No need to wrap, use old request
						dispatch(dispatcher, out, request, response);
						return;
					}
				}

				// Filter and merge all parameters
				final Map<String,List<String>> newMap;
				if(params==null) {
					newMap = Collections.emptyMap();
				} else {
					newMap = params.getParameterMap();
				}
				if(oldMap==null) oldMap = request.getParameterMap();
				Map<String,String[]> newParameters = new LinkedHashMap<String,String[]>(
					(
						newMap.size()
						+ oldMap.size()
					)*4/3+1
				);
				for(Map.Entry<String,List<String>> entry : newMap.entrySet()) {
					String name = entry.getKey();
					List<String> newValues = entry.getValue();
					String[] oldValues = isFiltered(clearParamNames, name) ? null : oldMap.get(name);
					String[] merged;
					if(oldValues==null) {
						// No need to merge values
						merged = newValues.toArray(new String[newValues.size()]);
					} else {
						// Merge values into single String[]
						merged = new String[newValues.size() + oldValues.length];
						String[] result = newValues.toArray(merged);
						assert merged==result;
						System.arraycopy(oldValues, 0, merged, newValues.size(), oldValues.length);
					}
					newParameters.put(name, merged);
				}
				// Add any old parameters that were not merged
				for(Map.Entry<String,String[]> entry : oldMap.entrySet()) {
					String name = entry.getKey();
					if(
						!newMap.containsKey(name)
						&& !isFiltered(clearParamNames, name)
					) newParameters.put(name, entry.getValue());
				}
				final Map<String,String[]> parameters = Collections.unmodifiableMap(newParameters);
				dispatch(
					dispatcher,
					out,
					new HttpServletRequestWrapper(request) {
						@Override
						public String getParameter(String name) {
							String[] values = parameters.get(name);
							return values==null || values.length==0 ? null : values[0];
						}

						@Override
						public Map getParameterMap() {
							return parameters;
						}

						@Override
						public Enumeration getParameterNames() {
							return Collections.enumeration(parameters.keySet());
						}

						@Override
						public String[] getParameterValues(String name) {
							return parameters.get(name);
						}
					},
					response
				);
			} finally {
				// Restore any previous args
				request.setAttribute(ARG_ATTRIBUTE_NAME, oldArgs);
			}
		} finally {
			myDispatchedPages.remove(myDispatchedPages.size()-1);
		}
    }

    /**
     * Gets the writer to use for the JspFragment.  By default it uses the same
     * writer as this tag.
     */
    Writer getJspFragmentWriter(JspWriter out) {
        return out;
    }

    abstract void dispatch(RequestDispatcher dispatcher, JspWriter out, HttpServletRequest request, HttpServletResponse response) throws IOException, JspException;
}
