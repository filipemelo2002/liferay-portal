/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.dynamic.data.mapping.form.builder.internal.util;

import com.liferay.dynamic.data.mapping.expression.DDMExpressionFunction;
import com.liferay.dynamic.data.mapping.expression.DDMExpressionFunctionTracker;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.AggregateResourceBundle;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.ResourceBundleUtil;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rafael Praxedes
 */
@Component(
	immediate = true, service = DDMExpressionFunctionMetadataHelper.class
)
public class DDMExpressionFunctionMetadataHelper {

	public Map<String, List<DDMExpressionFunctionMetadata>>
		getDDMExpressionFunctionsMetadata(Locale locale) {

		Map<String, List<DDMExpressionFunctionMetadata>>
			ddmExpressionFunctionMetadatasMap = new HashMap<>();

		populateCustomDDMExpressionFunctionsMetadata(
			ddmExpressionFunctionMetadatasMap);
		populateDDMExpressionFunctionsMetadata(
			ddmExpressionFunctionMetadatasMap, getResourceBundle(locale));

		return ddmExpressionFunctionMetadatasMap;
	}

	public static class DDMExpressionFunctionMetadata {

		public DDMExpressionFunctionMetadata(
			String name, String label, String returnClassName,
			String[] parameterClassNames) {

			_name = name;
			_label = label;
			_returnClassName = returnClassName;
			_parameterClassNames = parameterClassNames;
		}

		public String getLabel() {
			return _label;
		}

		public String getName() {
			return _name;
		}

		public String[] getParameterClassNames() {
			return _parameterClassNames;
		}

		public String getReturnClassName() {
			return _returnClassName;
		}

		private final String _label;
		private final String _name;
		private final String[] _parameterClassNames;
		private final String _returnClassName;

	}

	protected void addDDMExpressionFunctionMetadata(
		Map<String, List<DDMExpressionFunctionMetadata>>
			ddmExpressionFunctionMetadatasMap,
		DDMExpressionFunctionMetadata ddmExpressionFunctionMetadata) {

		String firstParameterClassName =
			ddmExpressionFunctionMetadata.getParameterClassNames()[0];

		List<DDMExpressionFunctionMetadata> ddmExpressionFunctionMetadatas =
			ddmExpressionFunctionMetadatasMap.get(firstParameterClassName);

		if (ddmExpressionFunctionMetadatas == null) {
			ddmExpressionFunctionMetadatas = new ArrayList<>();

			ddmExpressionFunctionMetadatasMap.put(
				firstParameterClassName, ddmExpressionFunctionMetadatas);
		}

		ddmExpressionFunctionMetadatas.add(ddmExpressionFunctionMetadata);
	}

	protected ResourceBundle getResourceBundle(Locale locale) {
		ResourceBundle portalResourceBundle = _portal.getResourceBundle(locale);

		ResourceBundle portletResourceBundle = ResourceBundleUtil.getBundle(
			"content.Language", locale, getClass());

		return new AggregateResourceBundle(
			portletResourceBundle, portalResourceBundle);
	}

	protected void populateCustomDDMExpressionFunctionsMetadata(
		Map<String, List<DDMExpressionFunctionMetadata>>
			ddmExpressionFunctionMetadatasMap) {

		Map<String, DDMExpressionFunction> customDDMExpressionFunctions =
			_ddmExpressionFunctionTracker.getCustomDDMExpressionFunctions();

		for (Map.Entry<String, DDMExpressionFunction> entry :
				customDDMExpressionFunctions.entrySet()) {

			DDMExpressionFunction ddmExpressionFunction = entry.getValue();

			Class<?> clazz = ddmExpressionFunction.getClass();

			Stream<Method> stream = Arrays.stream(clazz.getMethods());

			Optional<Method> optional = stream.filter(
				method ->
					Objects.equals(method.getName(), "apply") &&
					Objects.equals(method.getReturnType(), Boolean.class)
			).findFirst();

			if (!optional.isPresent()) {
				continue;
			}

			Method method = optional.get();

			addDDMExpressionFunctionMetadata(
				ddmExpressionFunctionMetadatasMap,
				new DDMExpressionFunctionMetadata(
					entry.getKey(), entry.getKey(), _TYPE_BOOLEAN,
					_getParameterClassNames(
						method.getParameterCount(), _TYPE_NUMBER)));
			addDDMExpressionFunctionMetadata(
				ddmExpressionFunctionMetadatasMap,
				new DDMExpressionFunctionMetadata(
					entry.getKey(), entry.getKey(), _TYPE_BOOLEAN,
					_getParameterClassNames(
						method.getParameterCount(), _TYPE_TEXT)));
		}
	}

	protected void populateDDMExpressionFunctionsMetadata(
		Map<String, List<DDMExpressionFunctionMetadata>>
			ddmExpressionFunctionMetadatasMap,
		ResourceBundle resourceBundle) {

		addDDMExpressionFunctionMetadata(
			ddmExpressionFunctionMetadatasMap,
			new DDMExpressionFunctionMetadata(
				"belongs-to", LanguageUtil.get(resourceBundle, "belongs-to"),
				_TYPE_BOOLEAN, new String[] {_TYPE_USER, _TYPE_LIST}));

		addDDMExpressionFunctionMetadata(
			ddmExpressionFunctionMetadatasMap,
			new DDMExpressionFunctionMetadata(
				"greater-than",
				LanguageUtil.get(resourceBundle, "is-greater-than"),
				_TYPE_BOOLEAN, new String[] {_TYPE_NUMBER, _TYPE_NUMBER}));

		addDDMExpressionFunctionMetadata(
			ddmExpressionFunctionMetadatasMap,
			new DDMExpressionFunctionMetadata(
				"greater-than-equals",
				LanguageUtil.get(resourceBundle, "is-greater-than-or-equal-to"),
				_TYPE_BOOLEAN, new String[] {_TYPE_NUMBER, _TYPE_NUMBER}));

		addDDMExpressionFunctionMetadata(
			ddmExpressionFunctionMetadatasMap,
			new DDMExpressionFunctionMetadata(
				"less-than", LanguageUtil.get(resourceBundle, "is-less-than"),
				_TYPE_BOOLEAN, new String[] {_TYPE_NUMBER, _TYPE_NUMBER}));

		addDDMExpressionFunctionMetadata(
			ddmExpressionFunctionMetadatasMap,
			new DDMExpressionFunctionMetadata(
				"less-than-equals",
				LanguageUtil.get(resourceBundle, "is-less-than-or-equal-to"),
				_TYPE_BOOLEAN, new String[] {_TYPE_NUMBER, _TYPE_NUMBER}));

		addDDMExpressionFunctionMetadata(
			ddmExpressionFunctionMetadatasMap,
			new DDMExpressionFunctionMetadata(
				"equals-to", LanguageUtil.get(resourceBundle, "is-equal-to"),
				_TYPE_BOOLEAN, new String[] {_TYPE_NUMBER, _TYPE_NUMBER}));

		addDDMExpressionFunctionMetadata(
			ddmExpressionFunctionMetadatasMap,
			new DDMExpressionFunctionMetadata(
				"equals-to", LanguageUtil.get(resourceBundle, "is-equal-to"),
				_TYPE_BOOLEAN, new String[] {_TYPE_TEXT, _TYPE_TEXT}));

		addDDMExpressionFunctionMetadata(
			ddmExpressionFunctionMetadatasMap,
			new DDMExpressionFunctionMetadata(
				"not-equals-to",
				LanguageUtil.get(resourceBundle, "is-not-equal-to"),
				_TYPE_BOOLEAN, new String[] {_TYPE_NUMBER, _TYPE_NUMBER}));

		addDDMExpressionFunctionMetadata(
			ddmExpressionFunctionMetadatasMap,
			new DDMExpressionFunctionMetadata(
				"not-equals-to",
				LanguageUtil.get(resourceBundle, "is-not-equal-to"),
				_TYPE_BOOLEAN, new String[] {_TYPE_TEXT, _TYPE_TEXT}));

		addDDMExpressionFunctionMetadata(
			ddmExpressionFunctionMetadatasMap,
			new DDMExpressionFunctionMetadata(
				"contains", LanguageUtil.get(resourceBundle, "contains"),
				_TYPE_BOOLEAN, new String[] {_TYPE_TEXT, _TYPE_TEXT}));

		addDDMExpressionFunctionMetadata(
			ddmExpressionFunctionMetadatasMap,
			new DDMExpressionFunctionMetadata(
				"not-contains",
				LanguageUtil.get(resourceBundle, "does-not-contain"),
				_TYPE_BOOLEAN, new String[] {_TYPE_TEXT, _TYPE_TEXT}));

		addDDMExpressionFunctionMetadata(
			ddmExpressionFunctionMetadatasMap,
			new DDMExpressionFunctionMetadata(
				"is-empty", LanguageUtil.get(resourceBundle, "is-empty"),
				_TYPE_BOOLEAN, new String[] {_TYPE_NUMBER}));

		addDDMExpressionFunctionMetadata(
			ddmExpressionFunctionMetadatasMap,
			new DDMExpressionFunctionMetadata(
				"is-empty", LanguageUtil.get(resourceBundle, "is-empty"),
				_TYPE_BOOLEAN, new String[] {_TYPE_TEXT}));

		addDDMExpressionFunctionMetadata(
			ddmExpressionFunctionMetadatasMap,
			new DDMExpressionFunctionMetadata(
				"not-is-empty",
				LanguageUtil.get(resourceBundle, "is-not-empty"), _TYPE_BOOLEAN,
				new String[] {_TYPE_NUMBER}));

		addDDMExpressionFunctionMetadata(
			ddmExpressionFunctionMetadatasMap,
			new DDMExpressionFunctionMetadata(
				"not-is-empty",
				LanguageUtil.get(resourceBundle, "is-not-empty"), _TYPE_BOOLEAN,
				new String[] {_TYPE_TEXT}));
	}

	private String[] _getParameterClassNames(
		int parameterCount, String parameterClassName) {

		String[] parameterClassNames = new String[parameterCount];

		Arrays.fill(parameterClassNames, parameterClassName);

		return parameterClassNames;
	}

	private static final String _TYPE_BOOLEAN = "boolean";

	private static final String _TYPE_LIST = "list";

	private static final String _TYPE_NUMBER = "number";

	private static final String _TYPE_TEXT = "text";

	private static final String _TYPE_USER = "user";

	@Reference
	private DDMExpressionFunctionTracker _ddmExpressionFunctionTracker;

	@Reference
	private Portal _portal;

}