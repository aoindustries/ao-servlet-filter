/*
 * ao-servlet-filter - Reusable Java library of servlet filters.
 * Copyright (C) 2019  AO Industries, Inc.
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

import com.aoindustries.net.IRI;
import com.aoindustries.net.URI;
import com.aoindustries.net.URIParser;
import com.aoindustries.servlet.http.Canonical;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * <p>
 * Encodes the URL to either
 * <a href="https://tools.ietf.org/html/rfc3986">RFC 3986 URI</a> US-ASCII format
 * or <a href="https://tools.ietf.org/html/rfc3987">RFC 3987 IRI</a> Unicode format.
 * If the URL begins with <code>javascript:</code>, <code>cid:</code>, or <code>data:</code>,
 * (case-insensitive) it is not altered.
 * {@linkplain Canonical Canonical URLs} are always encoded to US-ASCII format.
 * </p>
 * <p>
 * IRI support is disabled by default, and only recommended for development or
 * internal systems.  IRI may result in slightly smaller output, and more readable
 * HTML source, but for interoperability should be off in production systems.
 * </p>
 * <p>
 * <a href="https://tools.ietf.org/html/rfc7231#section-7.1.2">RFC 7231 - 7.1.2.  Location</a>
 * refers only to <a href="https://tools.ietf.org/html/rfc3986">RFC 3986 URI</a> for
 * URI-reference, thus redirects are always formatted as
 * <a href="https://tools.ietf.org/html/rfc3986">RFC 3986 URI</a>.
 * </p>
 * <p>
 * This should be used for both the REQUEST and ERROR dispatchers.
 * </p>
 */
public class EncodeURIFilter implements Filter {

	private static final String REQUEST_ATTRIBUTE_KEY = EncodeURIFilter.class.getName() + ".filter_applied";

	/**
	 * Gets the filter active on the given request.
	 *
	 * @return  The currently active filter or {@code null} for none active.
	 */
	public static EncodeURIFilter getActiveFilter(ServletRequest request) {
		return (EncodeURIFilter)request.getAttribute(REQUEST_ATTRIBUTE_KEY);
	}

	private boolean enableIRI;

	@Override
	public void init(FilterConfig config) {
		enableIRI = Boolean.parseBoolean(config.getInitParameter("enableIRI"));
	}

	private static String encode(String url, boolean enableIri, String characterEncoding) {
		if(
			URIParser.isScheme(url, "javascript")
			|| URIParser.isScheme(url, "cid")
			|| URIParser.isScheme(url, "data")
		) {
			return url;
		} else {
			if(
				enableIri
				&& !Canonical.get()
				&& (
					characterEncoding.equalsIgnoreCase(StandardCharsets.UTF_8.name())
					|| Charset.forName(characterEncoding) == StandardCharsets.UTF_8
				)
			) {
				return new IRI(url).toString();
			} else {
				return new URI(url).toASCIIString();
			}
		}
	}

	/**
	 * Performs encoding on the given URL in the given response encoding.
	 */
	public String encode(String url, String characterEncoding) {
		return encode(url, enableIRI, characterEncoding);
	}

	@Override
	public void doFilter(
		ServletRequest request,
		ServletResponse response,
		FilterChain chain
	) throws IOException, ServletException {
		// Makes sure only one filter is applied per request
		if(
			request.getAttribute(REQUEST_ATTRIBUTE_KEY)==null
			&& (response instanceof HttpServletResponse)
		) {
			request.setAttribute(REQUEST_ATTRIBUTE_KEY, Boolean.TRUE);
			try {
				chain.doFilter(
					request,
					new HttpServletResponseWrapper((HttpServletResponse)response) {
						@Override
						@Deprecated
						public String encodeRedirectUrl(String url) {
							return encode(super.encodeRedirectUrl(url), false, getCharacterEncoding());
						}

						@Override
						public String encodeRedirectURL(String url) {
							return encode(super.encodeRedirectURL(url), false, getCharacterEncoding());
						}

						@Override
						@Deprecated
						public String encodeUrl(String url) {
							return encode(super.encodeUrl(url), enableIRI, getCharacterEncoding());
						}

						@Override
						public String encodeURL(String url) {
							return encode(super.encodeURL(url), enableIRI, getCharacterEncoding());
						}
					}
				);
			} finally {
				request.removeAttribute(REQUEST_ATTRIBUTE_KEY);
			}
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
		// Nothing to do
	}
}
