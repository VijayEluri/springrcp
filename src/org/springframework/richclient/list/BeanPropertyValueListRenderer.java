/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.richclient.list;

import org.springframework.beans.BeanWrapperImpl;

/**
 * Renders a enumeration in a list.
 * 
 * @author Keith Donald
 */
public class BeanPropertyValueListRenderer extends TextValueListRenderer {
	private BeanWrapperImpl beanWrapper;

	private String propertyName;

	public BeanPropertyValueListRenderer(String propertyName) {
		this.propertyName = propertyName;
	}

	protected String getTextValue(Object value) {
		if (value == null) {
			return "";
		}
		if (beanWrapper == null) {
			beanWrapper = new BeanWrapperImpl(value);
		}
		else {
			beanWrapper.setWrappedInstance(value);
		}
		return String.valueOf(beanWrapper.getPropertyValue(propertyName));
	}
}