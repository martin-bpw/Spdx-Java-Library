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
package org.spdx.library.model.compat.v2.license;

import java.util.Arrays;
import java.util.List;

import org.spdx.library.InvalidSPDXAnalysisException;
import org.spdx.library.ModelCopyManager;
import org.spdx.library.SpdxConstants.SpdxMajorVersion;
import org.spdx.library.SpdxConstantsCompatV2;
import org.spdx.library.SpdxModelFactory;
import org.spdx.storage.IModelStore;
import org.spdx.storage.IModelStore.IdType;
import org.spdx.storage.simple.InMemSpdxStore;

import junit.framework.TestCase;

/**
 * @author gary
 *
 */
public class DisjunctiveLiceseSetTest extends TestCase {

	static final String DOCUMENT_URI = "https://test.document.uri";
	String[] IDS = new String[] {"LicenseRef-id1", "LicenseRef-id2", "LicenseRef-id3", "LicenseRef-id4"};
	String[] TEXTS = new String[] {"text1", "text2", "text3", "text4"};
	ExtractedLicenseInfo[] NON_STD_LICENSES;
	
	IModelStore modelStore;
	ModelCopyManager copyManager;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		modelStore = new InMemSpdxStore(SpdxMajorVersion.VERSION_2);
		copyManager = new ModelCopyManager();
		NON_STD_LICENSES = new ExtractedLicenseInfo[IDS.length];
		for (int i = 0; i < IDS.length; i++) {
			NON_STD_LICENSES[i] = new ExtractedLicenseInfo(modelStore, DOCUMENT_URI, IDS[i], copyManager, true);
			NON_STD_LICENSES[i].setExtractedText(TEXTS[i]);
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testCreateDisjunctive() throws InvalidSPDXAnalysisException {
		String id = modelStore.getNextId(IdType.Anonymous, DOCUMENT_URI);
		DisjunctiveLicenseSet cls = new DisjunctiveLicenseSet(modelStore, DOCUMENT_URI, id, copyManager, true);
		cls.setMembers(Arrays.asList(NON_STD_LICENSES));
		DisjunctiveLicenseSet cls2 = (DisjunctiveLicenseSet) SpdxModelFactory.createModelObjectV2(modelStore, DOCUMENT_URI, id, 
				SpdxConstantsCompatV2.CLASS_SPDX_DISJUNCTIVE_LICENSE_SET, copyManager);
		assertTrue(cls.equals(cls2));
		List<String> verify = cls2.verify();
		assertEquals(0, verify.size());
		verify = cls.verify();
		assertEquals(0, verify.size());
	}
	
	public void testAddMember() throws InvalidSPDXAnalysisException {
		String id = modelStore.getNextId(IdType.Anonymous, DOCUMENT_URI);
		DisjunctiveLicenseSet cls = new DisjunctiveLicenseSet(modelStore, DOCUMENT_URI, id, copyManager, true);
		cls.setMembers(Arrays.asList(NON_STD_LICENSES));
		ExtractedLicenseInfo eli = new ExtractedLicenseInfo(modelStore, DOCUMENT_URI, "LicenseRef-test", copyManager, true);
		eli.setExtractedText("test text");
		List<String> verify = eli.verify();
		assertEquals(0, verify.size());
		verify = cls.verify();
		assertEquals(0, verify.size());
		cls.addMember(eli);
		verify = cls.verify();
		assertEquals(0, verify.size());
		assertEquals(NON_STD_LICENSES.length+1, cls.getMembers().size());
		assertTrue(cls.getMembers().contains(eli));
		DisjunctiveLicenseSet cls2 = (DisjunctiveLicenseSet) SpdxModelFactory.createModelObjectV2(modelStore, DOCUMENT_URI, id, 
				SpdxConstantsCompatV2.CLASS_SPDX_DISJUNCTIVE_LICENSE_SET, copyManager);
		assertTrue(cls.equals(cls2));
		assertEquals(NON_STD_LICENSES.length+1, cls2.getMembers().size());
		assertTrue(cls2.getMembers().contains(eli));
		verify = cls.verify();
		assertEquals(0, verify.size());
		verify = cls2.verify();
		assertEquals(0, verify.size());
	}
	
	public void testRemoveMember() throws InvalidSPDXAnalysisException {
		String id = modelStore.getNextId(IdType.Anonymous, DOCUMENT_URI);
		DisjunctiveLicenseSet cls = new DisjunctiveLicenseSet(modelStore, DOCUMENT_URI, id, copyManager, true);
		cls.setMembers(Arrays.asList(NON_STD_LICENSES));
		cls.removeMember(NON_STD_LICENSES[0]);
		assertEquals(NON_STD_LICENSES.length-1, cls.getMembers().size());
		assertFalse(cls.getMembers().contains(NON_STD_LICENSES[0]));
		DisjunctiveLicenseSet cls2 = (DisjunctiveLicenseSet) SpdxModelFactory.createModelObjectV2(modelStore, DOCUMENT_URI, 
				id, SpdxConstantsCompatV2.CLASS_SPDX_DISJUNCTIVE_LICENSE_SET, copyManager);
		assertTrue(cls.equals(cls2));
		assertEquals(NON_STD_LICENSES.length-1, cls.getMembers().size());
		assertFalse(cls.getMembers().contains(NON_STD_LICENSES[0]));
		List<String> verify = cls2.verify();
		assertEquals(0, verify.size());
		verify = cls.verify();
		assertEquals(0, verify.size());
	}

}
