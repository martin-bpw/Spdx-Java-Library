/**
 * Copyright (c) 2019 Source Auditor Inc.
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
package org.spdx.library;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.core.DefaultModelStore;
import org.spdx.core.DefaultStoreNotInitialized;
import org.spdx.core.IModelCopyManager;
import org.spdx.core.InvalidSPDXAnalysisException;
import org.spdx.library.model.v2.license.AnyLicenseInfo;
import org.spdx.library.model.v2.license.InvalidLicenseStringException;
import org.spdx.library.model.v2.license.LicenseParserException;
import org.spdx.library.model.v2.license.ListedLicenseException;
import org.spdx.library.model.v2.license.SpdxListedLicense;
import org.spdx.library.model.v3.expandedlicensing.ExpandedLicensingListedLicense;
import org.spdx.library.model.v3.expandedlicensing.ExpandedLicensingListedLicenseException;
import org.spdx.library.model.v3.simplelicensing.SimpleLicensingAnyLicenseInfo;
import org.spdx.storage.IModelStore;
import org.spdx.utility.license.LicenseExpressionParser;

/**
 * Factory for creating SPDXLicenseInfo objects from a Jena model
 * @author Gary O'Neall
 */
public class LicenseInfoFactory {
	
	static final Logger logger = LoggerFactory.getLogger(LicenseInfoFactory.class.getName());
	
	public static final String NOASSERTION_LICENSE_NAME = "NOASSERTION";
	public static final String NONE_LICENSE_NAME = "NONE";
	
	/**
	 * @param licenseId SPDX Listed License ID
	 * @return SPDX listed license or null if the ID is not in the SPDX license list
	 * @throws InvalidSPDXAnalysisException
	 */
	public static ExpandedLicensingListedLicense getListedLicenseById(String licenseId)throws InvalidSPDXAnalysisException {
		return ListedLicenses.getListedLicenses().getListedLicenseById(licenseId);
	}
	
	/**
	 * @param licenseId SPDX Listed License ID
	 * @return SPDX listed license in SPDX spec version 2.X format or null if the ID is not in the SPDX license list
	 * @throws InvalidSPDXAnalysisException
	 */
	public static SpdxListedLicense getListedLicenseV2ByIdCompat(String licenseId)throws InvalidSPDXAnalysisException {
		return ListedLicenses.getListedLicenses().getListedLicenseV2ById(licenseId);
	}

	/**
	 * Parses a license string and converts it into a SPDXLicenseInfo object
	 * Syntax - A license set must start and end with a parenthesis "("
	 * 			A conjunctive license set will have and AND after the first
	 *				licenseInfo term
	 * 			A disjunctive license set will have an OR after the first 
	 *				licenseInfo term
	 *			If there is no And or Or, then it is converted to a simple
	 *				license type
	 *			A space or tab must be used between license ID's and the 
	 *				keywords AND and OR
	 *			A licenseID must NOT be "AND" or "OR"
	 * @param licenseString String conforming to the syntax
	 * @param store Store containing any extractedLicenseInfos - if any extractedLicenseInfos by ID already exist, they will be used.  If
	 * none exist for an ID, they will be added.  If null, the default model store will be used.
	 * @param documentUri Document URI for the document containing any extractedLicenseInfos - if any extractedLicenseInfos by ID already exist, they will be used.  If
	 * none exist for an ID, they will be added.  If null, the default model document URI will be used.
	 * @param copyManager if non-null, allows for copying of any properties set which use other model stores or document URI's
	 * @return an SPDXLicenseInfo created from the string
	 * @throws InvalidLicenseStringException 
	 * @throws DefaultStoreNotInitialized 
	 */
	public static AnyLicenseInfo parseSPDXLicenseStringV2(String licenseString, @Nullable IModelStore store, 
			@Nullable String documentUri, @Nullable IModelCopyManager copyManager) throws InvalidLicenseStringException, DefaultStoreNotInitialized {
		if (Objects.isNull(store)) {
			store = DefaultModelStore.getDefaultModelStore();
		}
		if (Objects.isNull(documentUri)) {
			documentUri = DefaultModelStore.getDefaultDocumentUri();
		}
		if (Objects.isNull(copyManager)) {
			copyManager = DefaultModelStore.getDefaultCopyManager();
		}
		try {
			return LicenseExpressionParser.parseLicenseExpressionCompatV2(licenseString, store, documentUri, 
					copyManager);
		} catch (LicenseParserException e) {
			throw new InvalidLicenseStringException(e.getMessage(),e);
		} catch (InvalidSPDXAnalysisException e) {
			throw new InvalidLicenseStringException("Unexpected SPDX error parsing license string");
		}
	}
	
	/**
	 * Parses a license string and converts it into a SPDXLicenseInfo object
	 * Syntax - A license set must start and end with a parenthesis "("
	 * 			A conjunctive license set will have and AND after the first
	 *				licenseInfo term
	 * 			A disjunctive license set will have an OR after the first 
	 *				licenseInfo term
	 *			If there is no And or Or, then it is converted to a simple
	 *				license type
	 *			A space or tab must be used between license ID's and the 
	 *				keywords AND and OR
	 *			A licenseID must NOT be "AND" or "OR"
	 * @param licenseString String conforming to the syntax
	 * @param store Store containing any extractedLicenseInfos - if any extractedLicenseInfos by ID already exist, they will be used.  If
	 * none exist for an ID, they will be added.  If null, the default model store will be used.
	 * @param documentUri Document URI for the document containing any extractedLicenseInfos - if any extractedLicenseInfos by ID already exist, they will be used.  If
	 * none exist for an ID, they will be added.  If null, the default model document URI will be used.
	 * @param copyManager if non-null, allows for copying of any properties set which use other model stores or document URI's
	 * @return an SPDXLicenseInfo created from the string
	 * @throws InvalidLicenseStringException 
	 * @throws DefaultStoreNotInitialized 
	 */
	public static SimpleLicensingAnyLicenseInfo parseSPDXLicenseString(String licenseString, @Nullable IModelStore store, 
			@Nullable String documentUri, @Nullable IModelCopyManager copyManager) throws InvalidLicenseStringException, DefaultStoreNotInitialized {
		if (Objects.isNull(store)) {
			store = DefaultModelStore.getDefaultModelStore();
		}
		if (Objects.isNull(documentUri)) {
			documentUri = DefaultModelStore.getDefaultDocumentUri();
		}
		if (Objects.isNull(copyManager)) {
			copyManager = DefaultModelStore.getDefaultCopyManager();
		}
		try {
			return LicenseExpressionParser.parseLicenseExpression(licenseString, store, documentUri, 
					copyManager);
		} catch (LicenseParserException e) {
			throw new InvalidLicenseStringException(e.getMessage(),e);
		} catch (InvalidSPDXAnalysisException e) {
			throw new InvalidLicenseStringException("Unexpected SPDX error parsing license string");
		}
	}

	/**
	 * Parses a license string and converts it into a SPDXLicenseInfo object
	 * Syntax - A license set must start and end with a parenthesis "("
	 * 			A conjunctive license set will have and AND after the first
	 *				licenseInfo term
	 * 			A disjunctive license set will have an OR after the first 
	 *				licenseInfo term
	 *			If there is no And or Or, then it is converted to a simple
	 *				license type
	 *			A space or tab must be used between license ID's and the 
	 *				keywords AND and OR
	 *			A licenseID must NOT be "AND" or "OR"
	 * @param licenseString String conforming to the syntax
	 * @return an SPDXLicenseInfo created from the string
	 * @throws InvalidLicenseStringException 
	 * @throws DefaultStoreNotInitialized 
	 */
	public static SimpleLicensingAnyLicenseInfo parseSPDXLicenseString(String licenseString) throws InvalidLicenseStringException, DefaultStoreNotInitialized {
		return parseSPDXLicenseString(licenseString, null, null, null);
	}
	
	/**
	 * Parses a license string and converts it into a SPDXLicenseInfo object
	 * Syntax - A license set must start and end with a parenthesis "("
	 * 			A conjunctive license set will have and AND after the first
	 *				licenseInfo term
	 * 			A disjunctive license set will have an OR after the first 
	 *				licenseInfo term
	 *			If there is no And or Or, then it is converted to a simple
	 *				license type
	 *			A space or tab must be used between license ID's and the 
	 *				keywords AND and OR
	 *			A licenseID must NOT be "AND" or "OR"
	 * @param licenseString String conforming to the syntax
	 * @return an SPDXLicenseInfo created from the string
	 * @throws InvalidLicenseStringException 
	 * @throws DefaultStoreNotInitialized 
	 */
	public static AnyLicenseInfo parseSPDXLicenseV2String(String licenseString) throws InvalidLicenseStringException, DefaultStoreNotInitialized {
		return parseSPDXLicenseStringV2(licenseString, null, null, null);
	}



	/**
	 * @param licenseID case insensitive
	 * @return true if the licenseID belongs to an SPDX listed license
	 */
	public static boolean isSpdxListedLicenseId(String licenseID)  {
		return ListedLicenses.getListedLicenses().isSpdxListedLicenseId(licenseID);
	}
	
	/**
	 * @return Array of all SPDX listed license IDs
	 */
	public static List<String> getSpdxListedLicenseIds() {
		return ListedLicenses.getListedLicenses().getSpdxListedLicenseIds();
	}
	
	/**
	 * @return Version of the license list being used by the SPDXLicenseInfoFactory
	 */
	public static String getLicenseListVersion() {
		return ListedLicenses.getListedLicenses().getLicenseListVersion();
	}

	/**
	 * @param objectUri exception ID
	 * @return true if the exception ID is a supported SPDX listed exception
	 */
	public static boolean isSpdxListedExceptionId(String id) {
		return ListedLicenses.getListedLicenses().isSpdxListedExceptionId(id);
	}

	/**
	 * @param objectUri
	 * @return the standard SPDX license exception or null if the ID is not in the SPDX license list
	 * @throws InvalidSPDXAnalysisException 
	 */
	public static ExpandedLicensingListedLicenseException getListedExceptionById(String id) throws InvalidSPDXAnalysisException {
		return ListedLicenses.getListedLicenses().getListedExceptionById(id);
	}

	/**
	 * @param objectUri
	 * @return the standard SPDX license exception in SPDX Spec V2.X format or null if the ID is not in the SPDX license list
	 * @throws InvalidSPDXAnalysisException 
	 */
	public static ListedLicenseException getListedExceptionV2ById(String id) throws InvalidSPDXAnalysisException {
		return ListedLicenses.getListedLicenses().getListedExceptionV2ById(id);
	}
	
	/**
	 * @param licenseId case insensitive license ID
	 * @return the case sensitive license ID
	 */
	public static Optional<String> listedLicenseIdCaseSensitive(String licenseId) {
		return ListedLicenses.getListedLicenses().listedLicenseIdCaseSensitive(licenseId);
	}

	/**
	 * @param exceptionId case insensitive exception ID
	 * @return case sensitive ID
	 */
	public static Optional<String> listedExceptionIdCaseSensitive(String exceptionId) {
		return ListedLicenses.getListedLicenses().listedExceptionIdCaseSensitive(exceptionId);
	}

}
