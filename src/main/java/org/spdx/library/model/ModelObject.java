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
package org.spdx.library.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.library.DefaultModelStore;
import org.spdx.library.InvalidSPDXAnalysisException;
import org.spdx.library.SpdxConstants;
import org.spdx.library.SpdxVerificationHelper;
import org.spdx.library.model.license.ListedLicenses;
import org.spdx.storage.IModelStore;
import org.spdx.storage.IModelStore.IdType;
import org.spdx.storage.IModelStore.ModelTransaction;
import org.spdx.storage.IModelStore.ModelUpdate;
import org.spdx.storage.IModelStore.ReadWrite;

/**
 * @author Gary O'Neall
 * 
 * Superclass for all SPDX model objects
 * 
 * Provides the primary interface to the storage class that access and stores the data for 
 * the model objects.
 * 
 * This class includes several helper methods to manage the storage and retrieval of properties.
 * 
 * Each model object is in itself stateless.  All state is maintained in the Model Store.  
 * The Document URI uniquely identifies the document containing the model object.
 * 
 * The concrete classes are expected to implements getters for the model class properties which translate
 * into calls to the getTYPEPropertyValue where TYPE is the type of value to be returned and the property name
 * is passed as a parameter.
 * 
 * There are 2 methods of setting values:
 *   - call the setPropertyValue, clearValueCollection or addValueToCollection methods - this will call the modelStore and store the
 *     value immediately
 *   - Gather a list of updates by calling the updatePropertyValue, updateClearValueList, or updateAddPropertyValue
 *     methods.  These methods return a ModelUpdate which can be applied later by calling the <code>apply()</code> method.
 *     A convenience method <code>Write.applyUpdatesInOneTransaction</code> will perform all updates within
 *     a single transaction. This method may result in higher performance updates for some Model Store implementations.
 *     Note that none of the updates will be applied until the storage manager update method is invoked.
 * 
 * Property values are restricted to the following types:
 *   - String - Java Strings
 *   - Booolean - Java Boolean or primitive boolean types
 *   - ModelObject - A concrete subclass of this type
 *   - Collection<T> - A Collection of type T where T is one of the supported non-collection types
 *     
 * This class also handles the conversion of a ModelObject to and from a TypeValue for storage in the ModelStore.
 *
 */
public abstract class ModelObject {
	
	static final Logger logger = LoggerFactory.getLogger(ModelObject.class);

	private IModelStore modelStore;
	private String documentUri;
	private String id;
	
	/**
	 * If set to true, a reference made to a model object stored in a different modelStore and/or
	 * document will be copied to this modelStore and documentUri
	 */
	private boolean copyOnReference = true;

	/**
	 * Create a new Model Object using an Anonomous ID with the defualt store and default document URI
	 * @throws InvalidSPDXAnalysisException 
	 */
	public ModelObject() throws InvalidSPDXAnalysisException {
		this(DefaultModelStore.getDefaultModelStore().getNextId(IdType.Anonymous, DefaultModelStore.getDefaultDocumentUri()));
	}
	
	/**
	 * Open or create a model object with the default store and default document URI
	 * @param id ID for this object - must be unique within the SPDX document
	 * @throws InvalidSPDXAnalysisException 
	 */
	public ModelObject(String id) throws InvalidSPDXAnalysisException {
		this(DefaultModelStore.getDefaultModelStore(), DefaultModelStore.getDefaultDocumentUri(), id, true);
	}
	
	/**
	 * @param modelStore Storage for the model objects
	 * @param documentUri SPDX Document URI for a document associated with this model
	 * @param id ID for this object - must be unique within the SPDX document
	 * @param create - if true, the object will be created in the store if it is not already present
	 * @throws InvalidSPDXAnalysisException
	 */
	public ModelObject(IModelStore modelStore, String documentUri, String id, boolean create) throws InvalidSPDXAnalysisException {
		Objects.requireNonNull(modelStore);
		Objects.requireNonNull(documentUri);
		Objects.requireNonNull(id);
		this.modelStore = modelStore;
		this.documentUri = documentUri;
		this.id = id;
		if (!modelStore.exists(documentUri, id)) {
			if (create) {
				modelStore.create(documentUri, id, getType());
			} else {
				throw new SpdxIdNotFoundException(id+" does not exist in document "+documentUri);
			}
		}
	}
	
	// Abstract methods that must be implemented in the subclasses
	/**
	 * @return The class name for this object.  Class names are defined in the constants file
	 */
	public abstract String getType();
	
	/**
	 * @return Any verification errors or warnings associated with this object
	 */
	public abstract List<String> verify();
	
	/**
	 * @return the Document URI for this object
	 */
	public String getDocumentUri() {
		return this.documentUri;
	}
	
	/**
	 * @return ID for the object
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * @return the model store for this object
	 */
	public IModelStore getModelStore() {
		return this.modelStore;
	}
	
	//The following methods are to manage the properties associated with the model object
	/**
	 * @return all names of property values currently associated with this object
	 * @throws InvalidSPDXAnalysisException 
	 */
	public List<String> getPropertyValueNames() throws InvalidSPDXAnalysisException {
		return modelStore.getPropertyValueNames(documentUri, id);
	}
	
	/**
	 * Get an object value for a property
	 * @param propertyName Name of the property
	 * @return value associated with a property
	 */
	public Optional<Object> getObjectPropertyValue(String propertyName) throws InvalidSPDXAnalysisException {
		return getObjectPropertyValue(modelStore, documentUri, id, propertyName);
	}
	
	/**
	 * Get an object value for a property
	 * @param stModelStore
	 * @param stDocumentUri
	 * @param stId
	 * @param propertyName
	 * @return value associated with a property
	 * @throws InvalidSPDXAnalysisException
	 */
	public static Optional<Object> getObjectPropertyValue(IModelStore stModelStore, String stDocumentUri,
			String stId, String propertyName) throws InvalidSPDXAnalysisException {
		if (!stModelStore.exists(stDocumentUri, stId)) {
			return Optional.empty();
		} else if (stModelStore.isCollectionProperty(stDocumentUri, stId, propertyName)) {
			return Optional.of(new ModelCollection<>(stModelStore, stDocumentUri, stId, propertyName));
		} else {
			return ModelStorageClassConverter.optionalStoredObjectToModelObject(stModelStore.getValue(stDocumentUri, stId, propertyName), stDocumentUri, stModelStore);
		}
	}

	/**
	 * Set a property value for a property name, creating the property if necessary
	 * @param stModelStore Model store for the properties
	 * @param stDocumentUri Unique document URI
	 * @param stId ID of the item to associate the property with
	 * @param propertyName Name of the property associated with this object
	 * @param value Value to associate with the property
	 * @param copyOnReference if true, any ModelObject property value not stored in the stModelStore under the stDocumentUri will be copied to make it available
	 * @throws InvalidSPDXAnalysisException
	 */
	public static void setPropertyValue(IModelStore stModelStore, String stDocumentUri, 
			String stId, String propertyName, Object value, boolean copyOnReference) throws InvalidSPDXAnalysisException {
		Objects.requireNonNull(stModelStore);
		Objects.requireNonNull(stDocumentUri);
		Objects.requireNonNull(stId);
		Objects.requireNonNull(propertyName);
		if (value == null) {
			// we just remove the value
			removeProperty(stModelStore, stDocumentUri, stId, propertyName);
		} else if (value instanceof Collection) {
			replacePropertyValueCollection(stModelStore, stDocumentUri, stId, propertyName, (Collection<?>)value, copyOnReference);
		} else {
			stModelStore.setValue(stDocumentUri, stId, propertyName, ModelStorageClassConverter.modelObjectToStoredObject(
					value, stDocumentUri, stModelStore, copyOnReference));
		}
	}
	
	/**
	 * Set a property value for a property name, creating the property if necessary
	 * @param propertyName Name of the property associated with this object
	 * @param value Value to associate with the property
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setPropertyValue(String propertyName, Object value) throws InvalidSPDXAnalysisException {
		if (this instanceof IndividualUriValue) {
			throw new InvalidSPDXAnalysisException("Can not set a property for the literal value "+((IndividualUriValue)this).getIndividualURI());
		}
		setPropertyValue(this.modelStore, this.documentUri, this.id, propertyName, value, copyOnReference);
	}
	
	/**
	 * Create an update when, when applied by the ModelStore, sets a property value for a property name, creating the property if necessary
	 * @param propertyName Name of the property associated with this object
	 * @param value Value to associate with the property
	 * @return an update which can be applied by invoking the apply method
	 */
	public ModelUpdate updatePropertyValue(String propertyName, Object value) {
		return () ->{
			setPropertyValue(this.modelStore, this.documentUri, this.id, propertyName, value, copyOnReference);
		};
	}
	
	/**
	 * @param propertyName Name of a property
	 * @return the Optional String value associated with a property, null if no value is present
	 * @throws SpdxInvalidTypeException
	 */
	public Optional<String> getStringPropertyValue(String propertyName) throws InvalidSPDXAnalysisException {
		Optional<Object> result = getObjectPropertyValue(propertyName);
		Optional<String> retval;
		if (result.isPresent()) {
			if (!(result.get() instanceof String)) {
				throw new SpdxInvalidTypeException("Property "+propertyName+" is not of type String");
			}
			retval = Optional.of((String)result.get());
		} else {
			retval = Optional.empty();
		}
		return retval;
	}
	
	protected Optional<Enum<?>> getEnumPropertyValue(String propertyName) throws InvalidSPDXAnalysisException {
		Optional<Object> result = getObjectPropertyValue(propertyName);
		if (!result.isPresent()) {
			return Optional.empty();
		}
		if (!(result.get() instanceof IndividualUriValue)) {
			throw new SpdxInvalidTypeException("Property "+propertyName+" is not of type Individual Value or enum");
		}
		Enum<?> retval = SpdxModelFactory.uriToEnum.get(((IndividualUriValue)result.get()).getIndividualURI());
		if (Objects.isNull(retval)) {
			logger.warn("Unknown individual value for enum: "+((IndividualUriValue)result.get()).getIndividualURI());
			return Optional.empty();
		} else {
			return Optional.of(retval);
		}
	}
	
	/**
	 * @param propertyName Name of the property
	 * @return the Optional Boolean value for a property
	 * @throws SpdxInvalidTypeException
	 */
	public Optional<Boolean> getBooleanPropertyValue(String propertyName) throws InvalidSPDXAnalysisException {
		Optional<Object> result = getObjectPropertyValue(propertyName);
		if (result.isPresent()) {
			if (result.get() instanceof Boolean) {
				return Optional.of((Boolean)result.get());
			} else if (result.get() instanceof String) {
				// try to convert
				String sResult = ((String)result.get()).toLowerCase();
				if ("true".equals(sResult)) {
					return Optional.of(new Boolean(true));
				} else if ("false".equals(sResult)) {
					return Optional.of(new Boolean(false));
				} else {
					throw new SpdxInvalidTypeException("Property "+propertyName+" is not of type Boolean");
				}
			} else {
				throw new SpdxInvalidTypeException("Property "+propertyName+" is not of type Boolean");
			}
		} else {
			return Optional.empty();
		}
	}
	
	/**
	 * Removes a property and its value from the model store if it exists
	 * @param stModelStore Model store for the properties
	 * @param stDocumentUri Unique document URI
	 * @param stId ID of the item to associate the property with
	 * @param propertyName Name of the property associated with this object to be removed
	 * @throws InvalidSPDXAnalysisException
	 */
	public static void removeProperty(IModelStore stModelStore, String stDocumentUri, String stId, String propertyName) throws InvalidSPDXAnalysisException {
		stModelStore.removeProperty(stDocumentUri, stId, propertyName);
	}
	
	/**
	 * Removes a property and its value from the model store if it exists
	 * @param propertyName Name of the property associated with this object to be removed
	 * @throws InvalidSPDXAnalysisException
	 */
	public void removeProperty(String propertyName) throws InvalidSPDXAnalysisException {
		removeProperty(modelStore, documentUri, id, propertyName);
	}
	
	/**
	 * Create an update when, when applied by the ModelStore, removes a property and its value from the model store if it exists
	 * @param propertyName Name of the property associated with this object to be removed
	 * @return  an update which can be applied by invoking the apply method
	 */
	public ModelUpdate updateRemoveProperty(String propertyName) {
		return () -> {
			removeProperty(modelStore, documentUri, id, propertyName);
		};
	}
	
	// The following methods manage collections of values associated with a property
	/**
	 * Clears a collection of values associated with a property creating the property if it does not exist
	 * @param stModelStore Model store for the properties
	 * @param stDocumentUri Unique document URI
	 * @param stId ID of the item to associate the property with
	 * @param propertyName Name of the property
	 * @throws InvalidSPDXAnalysisException
	 */
	public static void clearValueCollection(IModelStore stModelStore, String stDocumentUri, String stId, String propertyName) throws InvalidSPDXAnalysisException {
		stModelStore.clearValueCollection(stDocumentUri, stId, propertyName);
	}
	
	/**
	 * Clears a collection of values associated with a property
	 * @param propertyName Name of the property
	 */
	public void clearValueCollection(String propertyName) throws InvalidSPDXAnalysisException {
		clearValueCollection(modelStore, documentUri, id, propertyName);
	}
	
	/**
	 * Create an update when, when applied by the ModelStore, clears a collection of values associated with a property
	 * @param propertyName Name of the property
	 * @return an update which can be applied by invoking the apply method
	 */
	public ModelUpdate updateClearValueCollection(String propertyName) {
		return () ->{
			clearValueCollection(modelStore, documentUri, id, propertyName);
		};
	}
	
	/**
	 * Add a value to a collection of values associated with a property.  If a value is a ModelObject and does not
	 * belong to the document, it will be copied into the object store
	 * @param stModelStore Model store for the properties
	 * @param stDocumentUri Unique document URI
	 * @param stId ID of the item to associate the property with
	 * @param propertyName  Name of the property
	 * @param value to add
	 * @param copyOnReference if true, any ModelObject property value not stored in the stModelStore under the stDocumentUri will be copied to make it available
	 * @throws InvalidSPDXAnalysisException 
	 */
	public static void addValueToCollection(IModelStore stModelStore, String stDocumentUri, String stId, 
			String propertyName, Object value, boolean copyOnReference) throws InvalidSPDXAnalysisException {
		Objects.requireNonNull(value);
		stModelStore.addValueToCollection(stDocumentUri, stId, propertyName, 
				ModelStorageClassConverter.modelObjectToStoredObject(value, stDocumentUri, stModelStore, copyOnReference));
	}
	
	/**
	 * Add a value to a collection of values associated with a property.  If a value is a ModelObject and does not
	 * belong to the document, it will be copied into the object store
	 * @param propertyName  Name of the property
	 * @param value to add
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void addPropertyValueToCollection(String propertyName, Object value) throws InvalidSPDXAnalysisException {
		addValueToCollection(modelStore, documentUri, id, propertyName, value, copyOnReference);
	}
	
	/**
	 * Create an update when, when applied, adds a value to a collection of values associated with a property.  If a value is a ModelObject and does not
	 * belong to the document, it will be copied into the object store
	 * @param propertyName  Name of the property
	 * @param value to add
	 * @return an update which can be applied by invoking the apply method
	 */
	public ModelUpdate updateAddPropertyValueToCollection(String propertyName, Object value) {
		return () ->{
			addValueToCollection(modelStore, documentUri, id, propertyName, value, copyOnReference);
		};
	}
	
	/**
	 * Replace the entire value collection for a property.  If a value is a ModelObject and does not
	 * belong to the document, it will be copied into the object store
	 * @param stModelStore Model store for the properties
	 * @param stDocumentUri Unique document URI
	 * @param stId ID of the item to associate the property with
	 * @param propertyName name of the property
	 * @param values collection of new properties
	 * @param copyOnReference if true, any ModelObject property value not stored in the stModelStore under the stDocumentUri will be copied to make it available
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static void replacePropertyValueCollection(IModelStore stModelStore, String stDocumentUri, String stId, 
			String propertyName, Collection<?> values, boolean copyOnReference) throws InvalidSPDXAnalysisException {
		clearValueCollection(stModelStore, stDocumentUri, stId, propertyName);
		for (Object value:values) {
			addValueToCollection(stModelStore, stDocumentUri, stId, propertyName, value, copyOnReference);
		}
	}

	/**
	 * Remove a property value from a collection
	 * @param stModelStore Model store for the properties
	 * @param stDocumentUri Unique document URI
	 * @param stId ID of the item to associate the property with
	 * @param propertyName name of the property
	 * @param value Value to be removed
	 * @throws InvalidSPDXAnalysisException
	 */
	public static void removePropertyValueFromCollection(IModelStore stModelStore, String stDocumentUri, String stId, 
			String propertyName, Object value) throws InvalidSPDXAnalysisException {
		stModelStore.removeValueFromCollection(stDocumentUri, stId, propertyName, 
				ModelStorageClassConverter.modelObjectToStoredObject(value, stDocumentUri, stModelStore, false));
	}
	
	/**
	 * Remove a property value from a collection
	 * @param propertyName name of the property
	 * @param value Value to be removed
	 * @throws InvalidSPDXAnalysisException
	 */
	public void removePropertyValueFromCollection(String propertyName, Object value) throws InvalidSPDXAnalysisException {
		removePropertyValueFromCollection(modelStore, documentUri, id, propertyName, value);
	}
	
	/**
	 * Create an update when, when applied, removes a property value from a collection
	 * @param propertyName name of the property
	 * @param value Value to be removed
	 * @return an update which can be applied by invoking the apply method
	 */
	public ModelUpdate updateRemovePropertyValueFromCollection(String propertyName, Object value) {
		return () -> {
			removePropertyValueFromCollection(modelStore, documentUri, id, propertyName, value);
		};
	}
	
	/**
	 * @param propertyName Name of the property
	 * @return collection of values associated with a property
	 */
	public ModelCollection<Object> getObjectPropertyValueCollection(String propertyName) throws InvalidSPDXAnalysisException {
		return new ModelCollection<Object>(this.modelStore, this.documentUri, this.id, propertyName);
	}
	
	/**
	 * @param propertyName Name of property
	 * @return Collection of Strings associated with the property
	 * @throws SpdxInvalidTypeException
	 */
	@SuppressWarnings("unchecked")
	public Collection<String> getStringCollection(String propertyName) throws InvalidSPDXAnalysisException {
		if (!isCollectionMembersAssignableTo(propertyName, String.class)) {
			throw new SpdxInvalidTypeException("Property "+propertyName+" does not contain a collection of Strings");
		}
		return (Collection<String>)(Collection<?>)getObjectPropertyValueCollection(propertyName);
	}
	
	public boolean isCollectionMembersAssignableTo(String propertyName, Class<?> clazz) throws InvalidSPDXAnalysisException {
		return modelStore.isCollectionMembersAssignableTo(this.documentUri, this.id, propertyName, 
				ModelStorageClassConverter.modelClassToStoredClass(clazz));
	}
	
	/**
	 * @param compare
	 * @return true if all the properties have the same or equivalent values
	 */
	public boolean equivalent(ModelObject compare) throws InvalidSPDXAnalysisException {
		if (!this.getClass().equals(compare.getClass())) {
			return false;
		}
		List<String> propertyValueNames = getPropertyValueNames();
		List<String> comparePropertyValueNames = new ArrayList<String>(compare.getPropertyValueNames());	// create a copy since we're going to modify it
		for (String propertyName:propertyValueNames) {
			if (comparePropertyValueNames.contains(propertyName)) {
				Optional<Object> myValue = this.getObjectPropertyValue(propertyName);
				Optional<Object> compareValue = compare.getObjectPropertyValue(propertyName);
				if (!myValue.isPresent()) {
					if (compareValue.isPresent()) {
						return false;
					}
				} else if (!compareValue.isPresent()) {
					return false;
				} else if (myValue.get() instanceof ModelCollection && compareValue.get() instanceof ModelCollection) {
					List<?> myList = ((ModelCollection<?>)myValue.get()).toImmutableList();
					List<?> compareList = ((ModelCollection<?>)compareValue.get()).toImmutableList();
					if (!listsEquivalent(myList, compareList)) {
						return false;
					}
				} else if (myValue.get() instanceof List && compareValue.get() instanceof List) {
					if (!listsEquivalent((List<?>)myValue.get(), (List<?>)compareValue.get())) {
						return false;
					}
				} else if (myValue.get() instanceof IndividualUriValue && compareValue.get() instanceof IndividualUriValue) {
					if (!Objects.equals(((IndividualUriValue)myValue.get()).getIndividualURI(), ((IndividualUriValue)compareValue.get()).getIndividualURI())) {
						return false;
					}
					// Note: we must check the IndividualValue before the ModelObject types since the IndividualValue takes precedence
				} else if (myValue.get() instanceof ModelObject && compareValue.get() instanceof ModelObject) {
					if (!((ModelObject)myValue.get()).equivalent(((ModelObject)compareValue.get()))) {
						return false;
					}
					
				} else if (!Objects.equals(myValue, compareValue)) {	// Present, not a list, and not a TypedValue
					return false;
				}
				comparePropertyValueNames.remove(propertyName);
			} else {
				// No property value
				if (!this.getObjectPropertyValue(propertyName).isPresent()) {
					return false;
				}
			}
		}
		for (String propertyName:comparePropertyValueNames) {	// check any remaining property values
			if (!compare.getObjectPropertyValue(propertyName).isPresent()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param l1
	 * @param l2
	 * @return true if the two lists are equivalent
	 * @throws InvalidSPDXAnalysisException
	 */
	private boolean listsEquivalent(List<?> l1, List<?> l2) throws InvalidSPDXAnalysisException {
		int numRemainingComp = l2.size();
		for (Object item:l1) {
			if (l2.contains(item)) {
				numRemainingComp--;
			} else if (item instanceof IndividualUriValue && l2.contains(new SimpleUriValue((IndividualUriValue)item))) {
				numRemainingComp--;
			} else {
				if (item instanceof ModelObject) {
					// Need to check for equiv.
					boolean found = false;
					for (Object compareItem:l2) {
						if (compareItem instanceof ModelObject) {
							if (((ModelObject)item).equivalent(((ModelObject)compareItem))) {
								found = true;
								break;
							}
						}
					}
					if (found) {
						numRemainingComp--;
					} else {
						return false;
					}
				} else {
					return false;
				}
			}
		}
		return numRemainingComp <= 0;
	}

	@Override
	public int hashCode() {
		if (this.id != null) {
			return this.id.toLowerCase().hashCode() ^ this.documentUri.hashCode();
		} else {
			return 0;
		}
	}
	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof ModelObject)) {
			// covers o == null, as null is not an instance of anything
			return false;
		}
		ModelObject comp = (ModelObject)o;
		return Objects.equals(id, comp.getId()) && Objects.equals(documentUri, comp.getDocumentUri());
	}
	

	
	/**
	 * Clone a new object using a different model store
	 * @param modelStore
	 * @return
	 */
	public ModelObject clone(IModelStore modelStore) {
		try {
			return SpdxModelFactory.createModelObject(modelStore, this.documentUri, this.id, this.getType());
		} catch (InvalidSPDXAnalysisException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Copy all the properties from the source object
	 * @param source
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void copyFrom(ModelObject source) throws InvalidSPDXAnalysisException {
		ModelStorageClassConverter.copy(this.modelStore, this.documentUri, this.id, 
				source.getModelStore(), source.getDocumentUri(), source.getId(), this.getType());
	}
	
	/**
	 * @param id String for the object
	 * @return type of the ID
	 */
	IdType idToIdType(String id) {
		if (id.startsWith(SpdxConstants.NON_STD_LICENSE_ID_PRENUM)) {
			return IdType.LicenseRef;
		} else if (id.startsWith(SpdxConstants.SPDX_ELEMENT_REF_PRENUM)) {
			return IdType.SpdxId;
		} else if (id.startsWith(SpdxConstants.EXTERNAL_DOC_REF_PRENUM)) {
			return IdType.DocumentRef;
		} else if (ListedLicenses.getListedLicenses().isSpdxListedLicenseId(id)) {
			return IdType.ListedLicense;
		} else if ("none".equalsIgnoreCase(id) || "noassertion".equalsIgnoreCase(id)) {
			return IdType.Literal;
		} else {
			return IdType.Anonymous;
		}
	}
	
	TypedValue toTypedValue() throws InvalidSPDXAnalysisException {
		return new TypedValue(this.id, this.getType());
	}
	
	/**
	 * Verifies all elements in a collection
	 * @param collection
	 * @param warningPrefix String to prefix any warning messages
	 */
	protected List<String> verifyCollection(Collection<? extends ModelObject> collection, String warningPrefix) {
		List<String> retval = new ArrayList<>();
		collection.forEach(action -> {
			action.verify().forEach(warning -> {
				if (Objects.nonNull(warningPrefix)) {
					retval.add(warningPrefix + warning);
				} else {
					retval.add(warning);
				}
			});
		});
		return retval;
	}
	
	// The following methods are helper methods to create Model Object subclasses using the same model store and document as this Model Object

	/**
	 * @param annotator This field identifies the person, organization or tool that has commented on a file, package, or the entire document.
	 * @param annotationType This field describes the type of annotation.  Annotations are usually created when someone reviews the file, and if this is the case the annotation type should be REVIEW.   If the author wants to store extra information about one of the elements during creation, it is recommended to use the type of OTHER.
	 * @param date Identify when the comment was made.  This is to be specified according to the combined date and time in the UTC format, as specified in the ISO 8601 standard.
	 * @param comment
	 * @return
	 * @throws InvalidSPDXAnalysisException
	 */
	public Annotation createAnnotation(String annotator, AnnotationType annotationType, String date,
			String comment) throws InvalidSPDXAnalysisException {
		Objects.requireNonNull(annotator);
		Objects.requireNonNull(annotationType);
		Objects.requireNonNull(date);
		Objects.requireNonNull(comment);
		Annotation retval = new Annotation(this.modelStore, this.documentUri, 
				this.modelStore.getNextId(IdType.Anonymous, this.documentUri), true);
		retval.setAnnotationDate(date);
		retval.setAnnotationType(annotationType);
		retval.setAnnotator(annotator);
		retval.setComment(comment);
		return retval;
	}
	
	/**
	 * @param owningElement Source of the relationship
	 * @param relatedSpdxElement The SPDX Element that is related
	 * @param relationshipType Type of relationship - See the specification for a description of the types
	 * @param comment optional comment for the relationship
	 * @return
	 * @throws InvalidSPDXAnalysisException
	 */
	public Relationship createRelationship(@Nullable SpdxElement owningElement, SpdxElement relatedElement, 
			RelationshipType relationshipType, @Nullable String comment) throws InvalidSPDXAnalysisException {
		Objects.requireNonNull(relatedElement);
		Objects.requireNonNull(relationshipType);
		Relationship retval = new Relationship(this.modelStore, this.documentUri, 
				this.modelStore.getNextId(IdType.Anonymous, this.documentUri), true);
		if (Objects.nonNull(owningElement)) {
			retval.setOwningSpdxElement(owningElement);
		}
		retval.setRelatedSpdxElement(relatedElement);
		retval.setRelationshipType(relationshipType);
		if (Objects.nonNull(comment)) {
			retval.setComment(comment);
		}
		return retval;
	}
	
	/**
	 * @param algorithm Checksum algorithm
	 * @param value Checksum value
	 * @return Checksum using the same model store and document URI as this Model Object
	 * @throws InvalidSPDXAnalysisException
	 */
	public Checksum createChecksum(ChecksumAlgorithm algorithm, String value) throws InvalidSPDXAnalysisException {
		Objects.requireNonNull(algorithm);
		Objects.requireNonNull(value);
		Checksum retval = new Checksum(this.modelStore, this.documentUri, 
				this.modelStore.getNextId(IdType.Anonymous, this.documentUri), true);
		retval.setAlgorithm(algorithm);
		retval.setValue(value);
		return retval;
	}
	
	/**
	 * @param externalDocumentUri Document URI for the external document
	 * @param checksum Checksum of the external Document
	 * @param externalDocumentId ID to be used internally within this SPDX document
	 * @return ExternalDocumentRef using the same model store and document URI as this Model Object
	 * @throws InvalidSPDXAnalysisException 
	 */
	public ExternalDocumentRef createExternalDocumentRef(String externalDocumentId, String externalDocumentUri, 
			Checksum checksum) throws InvalidSPDXAnalysisException {
		Objects.requireNonNull(externalDocumentUri);
		Objects.requireNonNull(checksum);
		Objects.requireNonNull(externalDocumentId);
		if (!SpdxVerificationHelper.isValidExternalDocRef(externalDocumentId)) {
			throw new InvalidSPDXAnalysisException("Invalid external document reference ID "+externalDocumentId+
					".  Must be of the format "+SpdxConstants.EXTERNAL_DOC_REF_PATTERN.pattern());
		}
		if (!SpdxVerificationHelper.isValidUri(externalDocumentUri)) {
			throw new InvalidSPDXAnalysisException("Invalid external document URI: "+externalDocumentUri);
		}
		ModelTransaction transaction;
		try {
			transaction = getModelStore().beginTransaction(getDocumentUri(), ReadWrite.WRITE);
		} catch (IOException e) {
			logger.error("I/O error starting transaction",e);
			throw new InvalidSPDXAnalysisException("I/O error starting transaction",e);
		}
		try {
			if (getModelStore().exists(getDocumentUri(), externalDocumentId)) {
				return new ExternalDocumentRef(getModelStore(), getDocumentUri(), 
						externalDocumentId, false);
			} else {
				ExternalDocumentRef retval = new ExternalDocumentRef(getModelStore(), getDocumentUri(), 
						externalDocumentId, true);
				retval.setChecksum(checksum);
				retval.setSpdxDocumentNamespace(externalDocumentUri);
				// Need to add this to the list of document URI's
				ModelObject.addValueToCollection(getModelStore(), getDocumentUri(), 
						SpdxConstants.SPDX_DOCUMENT_ID, 
						SpdxConstants.PROP_SPDX_EXTERNAL_DOC_REF, retval, true);
				return retval;
			}
		} finally {
			try {
				transaction.commit();
				transaction.close();
			} catch (IOException e) {
				logger.error("I/O error committing transaction",e);
				throw new InvalidSPDXAnalysisException("I/O error committing transaction",e);
			}
		}
	}
}
