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
package org.springframework.binding.value.support;

import org.springframework.core.Styler;
import org.springframework.core.closure.Closure;
import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public class RefreshableValueHolder extends ValueHolder {
	private final Closure refreshFunction;

	
	public RefreshableValueHolder(Closure refreshFunction) {
		this(refreshFunction, false);
	}

	public RefreshableValueHolder(Closure refreshFunction, boolean alwaysRefresh) {
		this(refreshFunction, alwaysRefresh, true);
	}

	public RefreshableValueHolder(Closure refreshFunction, boolean alwaysRefresh, boolean lazyInit) {
		super();
		Assert.notNull(refreshFunction, "The refresh callback cannot be null");
		this.refreshFunction = refreshFunction;
		if (!lazyInit) {
            refresh();
		}
	}

	public Object getValue() {
		return super.getValue();
	}

	public void refresh() {
		if (logger.isDebugEnabled()) {
			logger.debug("Refreshing held value '" + Styler.call(super.getValue()) + "'");
		}
		setValue(refreshFunction.call(null));
	}
}