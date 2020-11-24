/*
 * ao-taglib - Making JSP be what it should have been all along.
 * Copyright (C) 2020  AO Industries, Inc.
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

import com.aoindustries.encoding.taglib.legacy.EncodingNullBodyTag;
import com.aoindustries.html.Attributes.Global;
import com.aoindustries.taglib.AttributeUtils;
import com.aoindustries.taglib.GlobalAttributesUtils;
import com.aoindustries.taglib.GlobalBufferedAttributes;
import com.aoindustries.taglib.MutableGlobalAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.DynamicAttributes;

/**
 * Implements {@linkplain Global global attributes} on {@link EncodingNullBodyTag}.
 *
 * @author  AO Industries, Inc.
 */
abstract public class ElementNullBodyTag extends EncodingNullBodyTag
	implements
		GlobalBufferedAttributes,
		DynamicAttributes
{

	private static final long serialVersionUID = 1L;

	public ElementNullBodyTag() {
		init();
	}

	protected MutableGlobalAttributes global;

	@Override
	public void setId(String id) {
		global.setId(id);
		super.setId(global.getId());
		// TODO: Validate, and TEI
	}

	@Override
	public String getClazz() {
		return global.getClazz();
	}
	@Override
	public void setClazz(String clazz) throws JspTagException {
		global.setClazz(clazz);
	}

	@Override
	public Map<String,Object> getData() {
		return global.getData();
	}
	@Override
	public void setData(Map<? extends String,?> data) throws JspTagException {
		global.setData(data);
	}
	@Override
	public void addData(String attrName, Object value) throws JspTagException {
		global.addData(attrName, value);
	}

	@Override
	public String getDir() {
		return global.getDir();
	}
	@Override
	public void setDir(String dir) throws JspTagException {
		global.setDir(dir);
	}

	@Override
	public Object getStyle() {
		return global.getStyle();
	}
	@Override
	public void setStyle(Object style) throws JspTagException {
		global.setStyle(style);
	}

	/**
	 * Adds a {@linkplain DynamicAttributes dynamic attribute}.
	 *
	 * @return  {@code true} when added, or {@code false} when attribute not expected and has not been added.
	 *
	 * @see  GlobalAttributesUtils#addDynamicAttribute(java.lang.String, java.lang.String, java.lang.Object, java.util.List, com.aoindustries.taglib.MutableGlobalAttributes)
	 * @see  #setDynamicAttribute(java.lang.String, java.lang.String, java.lang.Object)
	 */
	protected boolean addDynamicAttribute(String uri, String localName, Object value, List<String> expectedPatterns) throws JspTagException {
		return GlobalAttributesUtils.addDynamicAttribute(uri, localName, value, expectedPatterns, global);
	}

	/**
	 * Sets a {@linkplain DynamicAttributes dynamic attribute}.
	 *
	 * @deprecated  You should probably be implementing in {@link #addDynamicAttribute(java.lang.String, java.lang.String, java.lang.Object, java.util.List)}
	 *
	 * @see  #addDynamicAttribute(java.lang.String, java.lang.String, java.lang.Object, java.util.List)
	 */
	@Deprecated
	@Override
	public void setDynamicAttribute(String uri, String localName, Object value) throws JspTagException {
		List<String> expectedPatterns = new ArrayList<>();
		if(!addDynamicAttribute(uri, localName, value, expectedPatterns)) {
			throw AttributeUtils.newDynamicAttributeFailedException(uri, localName, value, expectedPatterns);
		}
	}

	private void init() {
		global = new MutableGlobalAttributes();
		super.setId(null);
	}

	@Override
	public void doFinally() {
		try {
			init();
		} finally {
			super.doFinally();
		}
	}
}