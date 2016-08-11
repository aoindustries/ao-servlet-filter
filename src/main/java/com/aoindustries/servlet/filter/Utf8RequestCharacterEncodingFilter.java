/*
 * ao-servlet-filter - Reusable Java library of servlet filters.
 * Copyright 2007, 2008, 2009, 2010, 2011, 2015, 2016 by AO Industries, Inc.,
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-servlet-filter.
 *
 * ao-servlet-filter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-servlet-filter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-servlet-filter.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.servlet.filter;

import com.aoindustries.util.i18n.ThreadLocale;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * <p>
 * Sets the request encoding to "UTF-8".
 * Also restores the ThreadLocale to the system default upon request completion.
 * </p>
 * <p>
 * This should be first in the filter chain and used for the REQUEST dispatcher only.
 * TODO: This extra side effect should either be moved to a different filter or just done away with entirely.
 * TODO: All code in good form should restore the thread locale already anyway with try/finally.
 * </p>
 *
 * @author  AO Industries, Inc.
 */
public class Utf8RequestCharacterEncodingFilter implements Filter {

	@Override
	public void init(FilterConfig config) {
	}

	@Override
	public void doFilter(
		ServletRequest request,
		ServletResponse response,
		FilterChain chain
	) throws IOException, ServletException {
		request.setCharacterEncoding("UTF-8");
		try {
			chain.doFilter(request, response);
		} finally {
			ThreadLocale.set(Locale.getDefault());
		}
	}

	@Override
	public void destroy() {
	}
}
