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
 
package org.spdx.library.model.security;

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
 * An EpssVulnAssessmentRelationship relationship describes the likelihood or 
 * probability that a vulnerability will be exploited in the wild using the Exploit 
 * Prediction Scoring System (EPSS) as defined on [https://www.first.org/epss/model](https://www.first.org/epss/model). 
 * **Constraints** - The relationship type must be set to hasAssessmentFor. **Syntax** 
 * ```json { "@type": "EpssVulnAssessmentRelationship", "@id": "urn:spdx.dev:epss-1", 
 * "relationshipType": "hasAssessmentFor", "probability": 80, "from": "urn:spdx.dev:vuln-cve-2020-28498", 
 * "to": ["urn:product-acme-application-1.3"], "suppliedBy": ["urn:spdx.dev:agent-jane-doe"], 
 * "publishedTime": "2021-03-09T11:04:53Z" } ``` 
 */
public class EpssVulnAssessmentRelationship extends VulnAssessmentRelationship  {

	
	/**
	 * Create the EpssVulnAssessmentRelationship with default model store and generated anonymous ID
	 * @throws InvalidSPDXAnalysisException when unable to create the EpssVulnAssessmentRelationship
	 */
	public EpssVulnAssessmentRelationship() throws InvalidSPDXAnalysisException {
		this(DefaultModelStore.getDefaultModelStore().getNextId(IdType.Anonymous, null));
	}

	/**
	 * @param objectUri URI or anonymous ID for the EpssVulnAssessmentRelationship
	 * @throws InvalidSPDXAnalysisException when unable to create the EpssVulnAssessmentRelationship
	 */
	public EpssVulnAssessmentRelationship(String objectUri) throws InvalidSPDXAnalysisException {
		this(DefaultModelStore.getDefaultModelStore(), objectUri, DefaultModelStore.getDefaultCopyManager(), true);
	}

	/**
	 * @param modelStore Model store where the EpssVulnAssessmentRelationship is to be stored
	 * @param objectUri URI or anonymous ID for the EpssVulnAssessmentRelationship
	 * @param copyManager Copy manager for the EpssVulnAssessmentRelationship - can be null if copying is not required
	 * @param create true if EpssVulnAssessmentRelationship is to be created
	 * @throws InvalidSPDXAnalysisException when unable to create the EpssVulnAssessmentRelationship
	 */
	public EpssVulnAssessmentRelationship(IModelStore modelStore, String objectUri, @Nullable ModelCopyManager copyManager,
			boolean create)	throws InvalidSPDXAnalysisException {
		super(modelStore, objectUri, copyManager, create);
	}

	/**
	 * Create the EpssVulnAssessmentRelationship from the builder - used in the builder class
	 * @param builder Builder to create the EpssVulnAssessmentRelationship from
	 * @throws InvalidSPDXAnalysisException when unable to create the EpssVulnAssessmentRelationship
	 */
	protected EpssVulnAssessmentRelationship(EpssVulnAssessmentRelationshipBuilder builder) throws InvalidSPDXAnalysisException {
		super(builder);
		setProbability(builder.probability);
	}

	/* (non-Javadoc)
	 * @see org.spdx.library.model.ModelObject#getType()
	 */
	@Override
	public String getType() {
		return "Security.EpssVulnAssessmentRelationship";
	}
	
	// Getters and Setters
	
	
	/**
	 * @return the probability
	 */
	public @Nullable Integer getProbability() throws InvalidSPDXAnalysisException {
		Optional<Integer> retval = getIntegerPropertyValue(SpdxConstants.SECURITY_PROP_PROBABILITY);
		return retval.isPresent() ? retval.get() : null;
	}
	
	/**
	 * @param probability the probability to set
	 * @return this to chain setters
	 * @throws InvalidSPDXAnalysisException 
	 */
	public EpssVulnAssessmentRelationship setProbability(@Nullable Integer probability) throws InvalidSPDXAnalysisException {
		if (isStrict() && Objects.isNull(probability)) {
			throw new InvalidSPDXAnalysisException("probability is a required property");
		}
		setPropertyValue(SpdxConstants.SECURITY_PROP_PROBABILITY, probability);
		return this;
	}
	
	
	@Override
	public String toString() {
		return "EpssVulnAssessmentRelationship: "+getObjectUri();
	}
	
	/* (non-Javadoc)
	 * @see org.spdx.library.model.ModelObject#_verify(java.util.List)
	 */
	@Override
	protected List<String> _verify(Set<String> verifiedIds, String specVersion, List<ProfileIdentifierType> profiles) {
		List<String> retval = new ArrayList<>();
		retval.addAll(super._verify(verifiedIds, specVersion, profiles));
		try {
			Integer probability = getProbability();
			if (Objects.isNull(probability) &&
					Collections.disjoint(profiles, Arrays.asList(new ProfileIdentifierType[] { ProfileIdentifierType.SECURITY }))) {
				retval.add("Missing probability in EpssVulnAssessmentRelationship");
			}
		} catch (InvalidSPDXAnalysisException e) {
			retval.add("Error getting probability for EpssVulnAssessmentRelationship: "+e.getMessage());
		}
		return retval;
	}
	
	public static class EpssVulnAssessmentRelationshipBuilder extends VulnAssessmentRelationshipBuilder {
	
		/**
		 * Create an EpssVulnAssessmentRelationshipBuilder from another model object copying the modelStore and copyManager and using an anonymous ID
		 * @param from model object to copy the model store and copyManager from
		 * @throws InvalidSPDXAnalysisException
		 */
		public EpssVulnAssessmentRelationshipBuilder(ModelObject from) throws InvalidSPDXAnalysisException {
			this(from, from.getModelStore().getNextId(IdType.Anonymous, null));
		}
	
		/**
		 * Create an EpssVulnAssessmentRelationshipBuilder from another model object copying the modelStore and copyManager
		 * @param from model object to copy the model store and copyManager from
		 * @param objectUri URI for the object
		 * @param objectUri
		 */
		public EpssVulnAssessmentRelationshipBuilder(ModelObject from, String objectUri) {
			this(from.getModelStore(), objectUri, from.getCopyManager());
			setStrict(from.isStrict());
		}
		
		/**
		 * Creates a EpssVulnAssessmentRelationshipBuilder
		 * @param modelStore model store for the built EpssVulnAssessmentRelationship
		 * @param objectUri objectUri for the built EpssVulnAssessmentRelationship
		 * @param copyManager optional copyManager for the built EpssVulnAssessmentRelationship
		 */
		public EpssVulnAssessmentRelationshipBuilder(IModelStore modelStore, String objectUri, @Nullable ModelCopyManager copyManager) {
			super(modelStore, objectUri, copyManager);
		}
		
		Integer probability = null;
		
		
		/**
		 * Sets the initial value of probability
		 * @parameter probability value to set
		 * @return this for chaining
		**/
		EpssVulnAssessmentRelationshipBuilder setProbability(Integer probability) {
			this.probability = probability;
			return this;
		}
	
		
		/**
		 * @return the EpssVulnAssessmentRelationship
		 * @throws InvalidSPDXAnalysisException on any errors during build
		 */
		public EpssVulnAssessmentRelationship build() throws InvalidSPDXAnalysisException {
			IModelStoreLock lock = modelStore.enterCriticalSection(false);
			try {
				return new EpssVulnAssessmentRelationship(this);
			} finally {
				modelStore.leaveCriticalSection(lock);
			}
		}
	}
}
