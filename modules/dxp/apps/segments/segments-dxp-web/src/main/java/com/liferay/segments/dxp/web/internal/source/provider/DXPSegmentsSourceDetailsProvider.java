/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.segments.dxp.web.internal.source.provider;

import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.segments.constants.SegmentsEntryConstants;
import com.liferay.segments.source.provider.SegmentsSourceDetailsProvider;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alejandro Tardín
 */
@Component(
	property = {
		"segments.source=" + SegmentsEntryConstants.SOURCE_DEFAULT,
		"service.ranking:Integer=100"
	},
	service = SegmentsSourceDetailsProvider.class
)
public class DXPSegmentsSourceDetailsProvider
	implements SegmentsSourceDetailsProvider {

	@Override
	public String getIconSrc() {
		return _servletContext.getContextPath() + "/assets/dxp-icon.svg";
	}

	@Override
	public String getLabel(Locale locale) {
		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
			locale, getClass());

		return LanguageUtil.get(resourceBundle, "source.dxp");
	}

	@Reference(target = "(osgi.web.symbolicname=com.liferay.segments.dxp.web)")
	private ServletContext _servletContext;

}