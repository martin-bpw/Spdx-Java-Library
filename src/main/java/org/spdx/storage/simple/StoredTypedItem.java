package org.spdx.storage.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.spdx.library.InvalidSPDXAnalysisException;
import org.spdx.library.SpdxConstants;
import org.spdx.library.model.ModelObject;
import org.spdx.library.model.SpdxInvalidTypeException;
import org.spdx.library.model.TypedValue;
import org.spdx.library.model.IndividualUriValue;
import org.spdx.storage.IModelStore;

/**
 * Individual item to be stored in memory
 * 
 * @author Gary O'Neall
 *
 */
class StoredTypedItem extends TypedValue {

	
	static Set<String> SPDX_CLASSES = new HashSet<>(Arrays.asList(SpdxConstants.ALL_SPDX_CLASSES));

	private ConcurrentHashMap<String, Object> properties = new ConcurrentHashMap<>();
	
	public StoredTypedItem(String documentUri, String id, String type) throws InvalidSPDXAnalysisException {
		super(id, type);
	}
	
	/**
	 * @returnProperty names for all properties having a value
	 */
	public List<String> getPropertyValueNames() {
		Iterator<Entry<String, Object>> iter = this.properties.entrySet().iterator();
		List<String> retval = new ArrayList<>();
		while (iter.hasNext()) {
			Entry<String, Object> entry = iter.next();
			retval.add(entry.getKey());
		}
		return Collections.unmodifiableList(retval);
	}
	
	/**
	 * @param propertyName Name of the property
	 * @param value Value to be set
	 * @throws SpdxInvalidTypeException 
	 */
	public void setValue(String propertyName, Object value) throws SpdxInvalidTypeException {
		Objects.requireNonNull(propertyName);
		Objects.requireNonNull(value);
		if (value instanceof ModelObject) {
			throw new SpdxInvalidTypeException("Can not store Model Object in store.  Convert to TypedValue first");
		} else if (value instanceof List || value instanceof Collection) {
			throw new SpdxInvalidTypeException("Can not store list valudes directly.  Use addValueToCollection.");
		} else if (!value.getClass().isPrimitive() && 
				!value.getClass().isAssignableFrom(String.class) &&
				!value.getClass().isAssignableFrom(Boolean.class) &&
				!value.getClass().isAssignableFrom(TypedValue.class) &&
				!(value instanceof IndividualUriValue)) {
			throw new SpdxInvalidTypeException(value.getClass().toString()+" is not a supported class to be stored.");
		}
		properties.put(propertyName, value);
	}
	
	/**
	 * Sets the value list for the property to an empty list creating the propertyName if it does not exist
	 * @param propertyName Name of the property
	 * @throws SpdxInvalidTypeException
	 */
	public void clearPropertyValueList(String propertyName) throws SpdxInvalidTypeException {
		Objects.requireNonNull(propertyName);
		Object value = properties.getOrDefault(propertyName, new ArrayList<Object>());
		if (value == null) {
			throw new SpdxInvalidTypeException("No list for list property value for property "+propertyName);
		}
		if (!(value instanceof List)) {
			throw new SpdxInvalidTypeException("Trying to clear a list for non list type for property "+propertyName);
		}
		((List<?>)value).clear();
	}

	/**
	 * Adds a value to a property list for a String or Boolean type of value creating the propertyName if it does not exist
	 * @param propertyName Name of the property
	 * @param value Value to be set
	 * @throws SpdxInvalidTypeException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean addValueToList(String propertyName, Object value) throws SpdxInvalidTypeException {
		Objects.requireNonNull(propertyName);
		Objects.requireNonNull(value);
		if (value instanceof ModelObject) {
			throw new SpdxInvalidTypeException("Can not store Model Object in store.  Convert to TypedValue first");
		}
		Object list = properties.get(propertyName);
		if (list == null) {
			properties.putIfAbsent(propertyName,  new ArrayList<Object>());
			list = properties.get(propertyName);	
			//Note: there is a small timing window where the property could be removed
			if (list == null) {
				return true;
			}
		}
		if (!(list instanceof List)) {
			throw new SpdxInvalidTypeException("Trying to add a list for non list type for property "+propertyName);
		}
		try {
			return ((List)list).add(value);
		} catch (Exception ex) {
			throw new SpdxInvalidTypeException("Invalid list type for "+propertyName);
		}
	}
	
	/**
	 * Removes a property from a list if it exists
	 * @param propertyName
	 * @param value
	 * @throws SpdxInvalidTypeException 
	 */
	@SuppressWarnings("rawtypes")
	public boolean removeValueFromList(String propertyName, Object value) throws SpdxInvalidTypeException {
		Objects.requireNonNull(propertyName);
		Objects.requireNonNull(value);
		Object list = properties.get(propertyName);
		if (list == null) {
			return true;
		}
		if (!(list instanceof List)) {
			throw new SpdxInvalidTypeException("Trying to add a list for non list type for property "+propertyName);
		}
		try {
			return ((List)list).remove(value);
		} catch (Exception ex) {
			throw new SpdxInvalidTypeException("Invalid list type for "+propertyName);
		}
	}
	
	/**
	 * @param propertyName Name of the property
	 * @return List of values associated with the id, propertyName and document
	 * @throws SpdxInvalidTypeException
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getValueList(String propertyName) throws SpdxInvalidTypeException {
		Objects.requireNonNull(propertyName);
		Object list = properties.get(propertyName);
		if (list == null) {
			return null;
		}
		if (!(list instanceof List)) {
			throw new SpdxInvalidTypeException("Trying to get a list for non list type for property "+propertyName);
		}
		return Collections.unmodifiableList((List<Object>)list);
	}
	
	/**
	 * @param propertyName Name of the property
	 * @return the single value associated with the id, propertyName and document
	 */
	public Object getValue(String propertyName) {
		Objects.requireNonNull(propertyName);
		return properties.get(propertyName);
	}
	
	/**
	 * Removes a property from the document for the given ID if the property exists.  Does not raise any exception if the propertyName does not exist
	 * @param propertyName Name of the property
	 */
	public void removeProperty(String propertyName) {
		Objects.requireNonNull(propertyName);
		properties.remove(propertyName);
	}

	/**
	 * Copy all values for this item from another store
	 * @param fromDocumentUri 
	 * @param store
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void copyValuesFrom(String fromDocumentUri, IModelStore store) throws InvalidSPDXAnalysisException {
		Objects.requireNonNull(fromDocumentUri);
		Objects.requireNonNull(store);
		List<String> propertyNames = store.getPropertyValueNames(fromDocumentUri, this.getId());
		for (String propertyName:propertyNames) {
			Optional<Object> value = store.getValue(fromDocumentUri, getId(), propertyName);
			if (value.isPresent()) {
				this.setValue(propertyName, value.get());
			}
		}
	}

	/**
	 * @param propertyName
	 * @return Size of the collection
	 * @throws SpdxInvalidTypeException 
	 */
	@SuppressWarnings("rawtypes")
	public int collectionSize(String propertyName) throws SpdxInvalidTypeException {
		Objects.requireNonNull(propertyName);
		Object list = properties.get(propertyName);
		if (list == null) {
			properties.putIfAbsent(propertyName,  new ArrayList<Object>());
			list = properties.get(propertyName);	
			//Note: there is a small timing window where the property could be removed
			if (list == null) {
				return 0;
			}
		}
		if (!(list instanceof List)) {
			throw new SpdxInvalidTypeException("Trying to add a list for non list type for property "+propertyName);
		}
		try {
			return ((List)list).size();
		} catch (Exception ex) {
			throw new SpdxInvalidTypeException("Invalid list type for "+propertyName);
		}
	}

	@SuppressWarnings("rawtypes")
	public boolean collectionContains(String propertyName, Object value) throws SpdxInvalidTypeException {
		Objects.requireNonNull(propertyName);
		Objects.requireNonNull(value);
		Object list = properties.get(propertyName);
		if (list == null) {
			properties.putIfAbsent(propertyName,  new ArrayList<Object>());
			list = properties.get(propertyName);	
			//Note: there is a small timing window where the property could be removed
			if (list == null) {
				return false;
			}
		}
		if (!(list instanceof List)) {
			throw new SpdxInvalidTypeException("Trying to add a list for non list type for property "+propertyName);
		}
		try {
			return ((List)list).contains(value);
		} catch (Exception ex) {
			throw new SpdxInvalidTypeException("Invalid list type for "+propertyName);
		}
	}

	public boolean isCollectionMembersAssignableTo(String propertyName, Class<?> clazz) {
		Objects.requireNonNull(propertyName);
		Objects.requireNonNull(clazz);
		Object value = properties.get(propertyName);
		if (value == null) {
			return true; // It is still assignable to since it is unassigned
		}
		if (!(value instanceof List)) {
			return false;
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		List<Object> list = (List)value;
		for (Object o:list) {
			if (!o.getClass().isAssignableFrom(clazz)) {
				return false;
			}
		}
		return true;
	}

	public boolean isPropertyValueAssignableTo(String propertyName, Class<?> clazz) {
		Objects.requireNonNull(propertyName);
		Objects.requireNonNull(clazz);
		Object value = properties.get(propertyName);
		if (value == null) {
			return false;
		}
		return value.getClass().isAssignableFrom(clazz);
	}

	public boolean isCollectionProperty(String propertyName) {
		Objects.requireNonNull(propertyName);
		Object value = properties.get(propertyName);
		return value instanceof List;
	}
}