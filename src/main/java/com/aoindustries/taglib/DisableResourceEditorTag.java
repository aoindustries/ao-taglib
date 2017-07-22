/*
 * ao-taglib - Making JSP be what it should have been all along.
 * Copyright (C) 2011, 2012, 2013, 2016, 2017  AO Industries, Inc.
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
import com.aoindustries.util.i18n.EditableResourceBundle;
import java.io.IOException;
import java.io.Writer;
import javax.servlet.jsp.JspTagException;

/**
 * Disables the resource editor.
 */
public class DisableResourceEditorTag extends AutoEncodingNullTag {

	/**
	 * No output.
	 */
	@Override
	public MediaType getOutputType() {
		return null;
	}

	@Override
	protected void doTag(Writer out) throws JspTagException, IOException {
		EditableResourceBundle.resetRequest(false, null, false);
	}
}
