/**
 * Copyright (c) 2023 Source Auditor Inc.
 *
 * SPDX-License-Identifier: Apache-2.0
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
 
package org.spdx.library.model.licensing;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.spdx.library.DefaultModelStore;
import org.spdx.library.InvalidSPDXAnalysisException;
import org.spdx.library.ModelCopyManager;
import org.spdx.library.model.ModelObject;
import org.spdx.storage.IModelStore;
import org.spdx.storage.IModelStore.IdType;
import org.spdx.storage.IModelStore.IModelStoreLock;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import org.spdx.library.SpdxConstants;
import org.spdx.library.model.core.ProfileIdentifierType;

/**
 * DO NOT EDIT - this file is generated by the Owl to Java Utility 
 * See: https://github.com/spdx/tools-java 
 * 
 * Often a single license can be used to represent the licensing terms of a source code 
 * or binary file, but there are situations where a single license identifier is not 
 * sufficient. A common example is when software is offered under a choice of one or more 
 * licenses (e.g., GPL-2.0-only OR BSD-3-Clause). Another example is when a set of 
 * licenses is needed to represent a binary program constructed by compiling and linking 
 * two (or more) different source files each governed by different licenses (e.g., 
 * LGPL-2.1-only AND BSD-3-Clause). SPDX License Expressions provide a way for one 
 * to construct expressions that more accurately represent the licensing terms typically 
 * found in open source software source code. A license expression could be a single 
 * license identifier found on the SPDX License List; a user defined license reference 
 * denoted by the LicenseRef-idString; a license identifier combined with an SPDX 
 * exception; or some combination of license identifiers, license references and 
 * exceptions constructed using a small set of defined operators (e.g., AND, OR, WITH 
 * and +). We provide the definition of what constitutes a valid an SPDX License Expression 
 * in this section. 
 */
public class LicenseExpression extends AnyLicenseInfo  {

	
	/**
	 * Create the LicenseExpression with default model store and generated anonymous ID
	 * @throws InvalidSPDXAnalysisException when unable to create the LicenseExpression
	 */
	public LicenseExpression() throws InvalidSPDXAnalysisException {
		this(DefaultModelStore.getDefaultModelStore().getNextId(IdType.Anonymous, null));
	}

	/**
	 * @param objectUri URI or anonymous ID for the LicenseExpression
	 * @throws InvalidSPDXAnalysisException when unable to create the LicenseExpression
	 */
	public LicenseExpression(String objectUri) throws InvalidSPDXAnalysisException {
		this(DefaultModelStore.getDefaultModelStore(), objectUri, DefaultModelStore.getDefaultCopyManager(), true);
	}

	/**
	 * @param modelStore Model store where the LicenseExpression is to be stored
	 * @param objectUri URI or anonymous ID for the LicenseExpression
	 * @param copyManager Copy manager for the LicenseExpression - can be null if copying is not required
	 * @param create true if LicenseExpression is to be created
	 * @throws InvalidSPDXAnalysisException when unable to create the LicenseExpression
	 */
	public LicenseExpression(IModelStore modelStore, String objectUri, @Nullable ModelCopyManager copyManager,
			boolean create)	throws InvalidSPDXAnalysisException {
		super(modelStore, objectUri, copyManager, create);
	}

	/**
	 * Create the LicenseExpression from the builder - used in the builder class
	 * @param builder Builder to create the LicenseExpression from
	 * @throws InvalidSPDXAnalysisException when unable to create the LicenseExpression
	 */
	protected LicenseExpression(LicenseExpressionBuilder builder) throws InvalidSPDXAnalysisException {
		super(builder);
		setLicenseExpression(builder.licenseExpression);
	}

	/* (non-Javadoc)
	 * @see org.spdx.library.model.ModelObject#getType()
	 */
	@Override
	public String getType() {
		return "Licensing.LicenseExpression";
	}
	
	// Getters and Setters
	

	/**
	 * @return the licenseExpression
	 */
	public @Nullable String getLicenseExpression() throws InvalidSPDXAnalysisException {
		Optional<String> retval = getStringPropertyValue(SpdxConstants.LICENSING_PROP_LICENSE_EXPRESSION);
		return retval.isPresent() ? retval.get() : null;
	}
		/**
	 * @param licenseExpression the licenseExpression to set
	 * @return this to chain setters
	 * @throws InvalidSPDXAnalysisException 
	 */
	public LicenseExpression setLicenseExpression(@Nullable String licenseExpression) throws InvalidSPDXAnalysisException {
		if (isStrict() && Objects.isNull(licenseExpression)) {
			throw new InvalidSPDXAnalysisException("licenseExpression is a required property");
		}
		setPropertyValue(SpdxConstants.LICENSING_PROP_LICENSE_EXPRESSION, licenseExpression);
		return this;
	}
	
	
	@Override
	public String toString() {
		return "LicenseExpression: "+getObjectUri();
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.library.model.ModelObject#_verify(java.util.List)
	 */
	@Override
	protected List<String> _verify(Set<String> verifiedIds, String specVersion, List<ProfileIdentifierType> profiles) {
		List<String> retval = new ArrayList<>();
		retval.addAll(super._verify(verifiedIds, specVersion, profiles));
		try {
			String licenseExpression = getLicenseExpression();
			if (Objects.isNull(licenseExpression) &&
					Collections.disjoint(profiles, Arrays.asList(new ProfileIdentifierType[] { ProfileIdentifierType.LICENSING }))) {
				retval.add("Missing licenseExpression in LicenseExpression");
			}
		} catch (InvalidSPDXAnalysisException e) {
			retval.add("Error getting licenseExpression for LicenseExpression: "+e.getMessage());
		}
		return retval;
	}
	
	public static class LicenseExpressionBuilder extends AnyLicenseInfoBuilder {
	
		/**
		 * Create an LicenseExpressionBuilder from another model object copying the modelStore and copyManager and using an anonymous ID
		 * @param from model object to copy the model store and copyManager from
		 * @throws InvalidSPDXAnalysisException
		 */
		public LicenseExpressionBuilder(ModelObject from) throws InvalidSPDXAnalysisException {
			this(from, from.getModelStore().getNextId(IdType.Anonymous, null));
		}
	
		/**
		 * Create an LicenseExpressionBuilder from another model object copying the modelStore and copyManager
		 * @param from model object to copy the model store and copyManager from
		 * @param objectUri URI for the object
		 * @param objectUri
		 */
		public LicenseExpressionBuilder(ModelObject from, String objectUri) {
			this(from.getModelStore(), objectUri, from.getCopyManager());
			setStrict(from.isStrict());
		}
		
		/**
		 * Creates a LicenseExpressionBuilder
		 * @param modelStore model store for the built LicenseExpression
		 * @param objectUri objectUri for the built LicenseExpression
		 * @param copyManager optional copyManager for the built LicenseExpression
		 */
		public LicenseExpressionBuilder(IModelStore modelStore, String objectUri, @Nullable ModelCopyManager copyManager) {
			super(modelStore, objectUri, copyManager);
		}
		
		String licenseExpression = null;
		
		
		/**
		 * Sets the initial value of licenseExpression
		 * @parameter licenseExpression value to set
		 * @return this for chaining
		**/
		LicenseExpressionBuilder setLicenseExpression(String licenseExpression) {
			this.licenseExpression = licenseExpression;
			return this;
		}
	
		
		/**
		 * @return the LicenseExpression
		 * @throws InvalidSPDXAnalysisException on any errors during build
		 */
		public LicenseExpression build() throws InvalidSPDXAnalysisException {
			IModelStoreLock lock = modelStore.enterCriticalSection(false);
			try {
				return new LicenseExpression(this);
			} finally {
				modelStore.leaveCriticalSection(lock);
			}
		}
	}
}
