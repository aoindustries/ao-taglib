/*
 * ao-taglib - Making JSP what it should have been all along.
 * Copyright (C) 2010, 2011, 2013, 2016  AO Industries, Inc.
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

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.ValidationMessage;

/**
 * @author  AO Industries, Inc.
 */
public class RedirectTagTEI extends TagExtraInfo {

	@Override
	public ValidationMessage[] validate(TagData data) {
		Object o = data.getAttribute("statusCode");
		if(
			o != null
			&& o != TagData.REQUEST_TIME_VALUE
		) {
			String statusCode = (String)o;
			if(!RedirectTag.isValidStatusCode(statusCode)) {
				return new ValidationMessage[] {
					new ValidationMessage(data.getId(), ApplicationResources.accessor.getMessage("RedirectTag.statusCode.invalid", statusCode))
				};
			}
		}
		return null;
	}
}
