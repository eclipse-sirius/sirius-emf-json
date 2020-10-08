/*******************************************************************************
 * Copyright (c) 2020 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/

package org.eclipse.sirius.emfjson.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.BasicEMap;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.SegmentSequence;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.UniqueEList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.emfjson.resource.JsonResource.URIHandler;
import org.eclipse.sirius.emfjson.resource.exception.DanglingHREFException;

/**
 * Provide a mapping between EMF classes and how serialize/deserialize JsonObjects.
 *
 * @author <a href="mailto:guillaume.coutable@obeo.fr">Guillaume Coutable</a>
 */
public class JsonHelper {

    /**
     * The URI Segment separator.
     */
    private static final String SEGMENT_SEPARATOR = "/"; //$NON-NLS-1$

    /**
     * This class is used to register uri and all prefixes that refers to this uri.
     */
    private class PrefixToURI extends BasicEMap<String, String> {

        /**
         * .
         */
        private static final long serialVersionUID = 1L;

        /**
         * Return a list of prefixes corresponding to the given uri.
         *
         * @param uri
         *            the given URI String
         * @return the list of prefixes corresponding to the given uri
         */
        protected List<String> getPrefixes(String uri) {
            List<String> result = JsonHelper.this.urisToPrefixes.get(uri);
            if (result == null) {
                result = new ArrayList<String>();
                JsonHelper.this.urisToPrefixes.put(uri, result);
            }
            return result;
        }

        @Override
        protected void didAdd(Entry<String, String> entry) {
            this.getPrefixes(entry.getValue()).add(entry.getKey());
        }

        @Override
        protected void didClear(BasicEList<Entry<String, String>>[] oldEntryData) {
            JsonHelper.this.urisToPrefixes.clear();
        }

        @Override
        protected void didModify(Entry<String, String> entry, String oldValue) {
            String key = entry.getKey();
            this.getPrefixes(oldValue).remove(key);
            this.getPrefixes(entry.getValue()).add(key);
        }

        @Override
        protected void didRemove(Entry<String, String> entry) {
            this.getPrefixes(entry.getValue()).add(entry.getKey());
        }
    }

    /**
     * Sets when there is no namespace in json file.
     */
    protected EPackage noNamespacePackage;

    /**
     * Sets when there is extended meta data.
     */
    protected ExtendedMetaData extendedMetaData;

    /**
     * Sets to <code>true</code> if displaying of prefix is forced.
     */
    protected boolean mustHavePrefix;

    /**
     * Mapping between packages and nsPrefix.
     */
    protected Map<EPackage, String> packages = new HashMap<EPackage, String>();

    /**
     * Mapping between package prefixes and package URIs.
     */
    protected EMap<String, String> prefixesToURIs;

    /**
     * Mapping between package URIs and package prefixes. Avoid URI conflict.
     */
    protected Map<String, List<String>> urisToPrefixes = new HashMap<String, List<String>>();

    /**
     * The type that it will be used, when an AnySimpleType will be encountered.
     */
    protected EClass anySimpleType;

    /**
     * The json resource.
     */
    protected Resource resource;

    /**
     * Set to <code>true</code> if URIs should be deresolved.
     */
    protected boolean deresolve;

    /**
     * the package registry.
     */
    protected Registry packageRegistry;

    /**
     * The resource URI.
     */
    protected URI resourceURI;

    /**
     * The roots EObject to serialize.
     */
    protected List<? extends EObject> roots;

    /**
     * Keep the fragment prefixes in memory.
     */
    protected String[] fragmentPrefixes;

    /**
     * the URI Handler.
     */
    private URIHandler uriHandler;

    /**
     * Store the first dangling exception.
     */
    private DanglingHREFException danglingHREFException;

    /**
     * How manage dangling references.
     *
     * @see JsonResource#OPTION_PROCESS_DANGLING_HREF
     */
    private String processDanglingReference;

    /**
     * If Fragment references are serialized like XMIResource.
     */
    private boolean serializeFragmentReferencesAsXMIResource;

    /**
     * The constructor.
     */
    public JsonHelper() {
        this.prefixesToURIs = new PrefixToURI();
    }

    /**
     * The constructor.
     *
     * @param resource
     *            the resource
     */
    public JsonHelper(Resource resource) {
        this();
        this.setResource(resource);
    }

    /**
     * Return the resource.
     *
     * @return the resource
     */
    public Resource getResource() {
        return this.resource;
    }

    /**
     * Set the resource and also its package registry and if its URI should be deresolved.
     *
     * @param resource
     *            the resource to set
     */
    public void setResource(Resource resource) {
        this.resource = resource;
        if (resource == null) {
            this.resourceURI = null;
            this.deresolve = false;
            this.packageRegistry = EPackage.Registry.INSTANCE;
        } else {
            this.resourceURI = resource.getURI();
            this.deresolve = this.resourceURI != null && !this.resourceURI.isRelative() && this.resourceURI.isHierarchical();
            if (resource.getResourceSet() == null) {
                this.packageRegistry = EPackage.Registry.INSTANCE;
            } else {
                this.packageRegistry = resource.getResourceSet().getPackageRegistry();
            }
        }
    }

    /**
     * Sets various resource options on the helper.
     *
     * @param options
     *            options to set
     */
    public void setOptions(Map<?, ?> options) {
        this.uriHandler = (JsonResource.URIHandler) options.get(JsonResource.OPTION_URI_HANDLER);
        if (this.uriHandler != null) {
            this.uriHandler.setBaseURI(this.resourceURI);
        }
        this.setMustHavePrefix(Boolean.TRUE.equals(options.get(JsonResource.OPTION_FORCE_PREFIX_ON_EMPTY_ONE)));
        @SuppressWarnings("unchecked")
        List<? extends EObject> rootsEObject = (List<? extends EObject>) options.get(JsonResource.OPTION_ROOT_OBJECTS);
        if (rootsEObject != null) {
            this.roots = rootsEObject;
            this.fragmentPrefixes = new String[rootsEObject.size()];
            int count = 0;
            for (EObject root : rootsEObject) {
                InternalEObject internalEObject = (InternalEObject) root;
                List<String> uriFragmentPath = new ArrayList<String>();
                boolean stop = false;
                for (InternalEObject container = internalEObject.eInternalContainer(); container != null && !stop; container = internalEObject.eInternalContainer()) {
                    uriFragmentPath.add(container.eURIFragmentSegment(internalEObject.eContainingFeature(), internalEObject));
                    internalEObject = container;
                    Resource directResource = container.eDirectResource();
                    if (directResource != null) {
                        int index = directResource.getContents().indexOf(container);
                        String fragmentPath = ""; //$NON-NLS-1$
                        fragmentPath = this.diffZeroIndexConverter(index, fragmentPath);
                        uriFragmentPath.add(fragmentPath);
                        stop = true;
                    }
                }

                StringBuilder result = new StringBuilder(JsonHelper.SEGMENT_SEPARATOR);
                for (int i = uriFragmentPath.size() - 1; i >= 1; --i) {
                    result.append(uriFragmentPath.get(i));
                    result.append('/');
                }
                this.fragmentPrefixes[count++] = result.toString();
            }
        }

        this.processDanglingReference = (String) options.get(JsonResource.OPTION_PROCESS_DANGLING_HREF);

        this.serializeFragmentReferencesAsXMIResource = false;
        if (options.get(JsonResource.OPTION_FORCE_DEFAULT_REFERENCE_SERIALIZATION) != null) {
            this.serializeFragmentReferencesAsXMIResource = ((Boolean) options.get(JsonResource.OPTION_FORCE_DEFAULT_REFERENCE_SERIALIZATION)).booleanValue();
        }

    }

    /**
     * Sets the package to use when there is no namespace in the json file.
     *
     * @param pkg
     *            the package to set
     */
    public void setNoNamespacePackage(EPackage pkg) {
        this.noNamespacePackage = pkg;
    }

    /**
     * Gets the package to use when there is no namespace in a json file.
     *
     * @return the package to use when there is no namespace in a json file
     */
    public EPackage getNoNamespacePackage() {
        EPackage ePackage = this.noNamespacePackage;
        if (ePackage == null && this.extendedMetaData != null) {
            ePackage = this.extendedMetaData.getPackage(null);
        }

        return ePackage;
    }

    /**
     * Sets the any simple type class.
     *
     * @param type
     *            the type that it will used when, an AnySimpleType will be encountered
     */
    public void setAnySimpleType(EClass type) {
        this.anySimpleType = type;
    }

    /**
     * Sets the ExtendedMetaData to use when serializing a json file.
     *
     * @param metaData
     *            the Extended meta data
     */
    public void setExtendedMetaData(ExtendedMetaData metaData) {
        this.extendedMetaData = metaData;
        if (metaData != null && metaData.getPackage(null) != null) {
            this.setNoNamespacePackage(metaData.getPackage(null));
        }
    }

    /**
     * Gets the ExtendedMetaData to use when serializing json file.
     *
     * @return the ExtendedMetaData to use when serializing json file
     */
    public ExtendedMetaData getExtendedMetaData() {
        return this.extendedMetaData;
    }

    /**
     * Return NsName:name of the given EClass.
     *
     * @param eClass
     *            which we want the name
     * @return NsName:name of an EClass
     */
    public String getQName(EClass eClass) {
        String name = this.getName(eClass);
        return this.getQName(eClass.getEPackage(), name);
    }

    /**
     * By default, this method returns the name of the feature.
     *
     * @param feature
     *            the feature that we want the name
     * @return the name of the given feature
     */
    public String getQName(EStructuralFeature feature) {
        String result = this.getName(feature);
        if (this.extendedMetaData != null) {
            String namespace = this.extendedMetaData.getNamespace(feature);
            String name = result;

            if (namespace != null) {
                EPackage ePackage = this.extendedMetaData.getPackage(namespace);
                if (ePackage == null) {
                    ePackage = this.extendedMetaData.demandPackage(namespace);
                }

                result = this.getQName(ePackage, name);

                if (result.length() == name.length() && this.extendedMetaData.getFeatureKind(feature) == ExtendedMetaData.ATTRIBUTE_FEATURE) {
                    result = this.getQName(ePackage, name, true);
                }
            }

        }
        return result;
    }

    /**
     * Return NsName:name from the given EPackage.
     *
     * @param ePackage
     *            the given EPackage which we want the NsName
     * @param name
     *            the name
     * @return NsName:name
     */
    protected String getQName(EPackage ePackage, String name) {
        return this.getQName(ePackage, name, this.mustHavePrefix);
    }

    /**
     * Return the QName from a name and an EPackage to get prefix, if there is one.
     *
     * @param ePackage
     *            the EPackage
     * @param name
     *            the name
     * @param mustAddPrefix
     *            if the prefix should be added to the name
     * @return a QName
     */
    protected String getQName(EPackage ePackage, String name, boolean mustAddPrefix) {
        String nsPrefix = this.getPrefix(ePackage, mustAddPrefix);
        String result = name;
        if (!"".equals(nsPrefix) && name.length() > 0) { //$NON-NLS-1$
            result = nsPrefix + ":" + name; //$NON-NLS-1$
        } else if (name.length() == 0) {
            result = nsPrefix;
        }
        return result;
    }

    /**
     * Return the EPakage prefix.
     *
     * @param ePackage
     *            the EPackage
     * @return the EPackage prefix
     */
    public String getPrefix(EPackage ePackage) {
        return this.getPrefix(ePackage, this.mustHavePrefix);
    }

    /**
     * Return the given EPackage prefix.
     *
     * @param ePackage
     *            the ePackage
     * @param mustHaveAPrefix
     *            force the return of a prefix
     * @return the given EPackage prefix
     */
    protected String getPrefix(EPackage ePackage, boolean mustHaveAPrefix) {
        String nsPrefix = this.packages.get(ePackage);
        if (nsPrefix == null || mustHaveAPrefix && nsPrefix.length() == 0) {
            String nsURI = ePackage.getNsURI();
            if (this.extendedMetaData != null) {
                nsURI = this.extendedMetaData.getNamespace(ePackage);
            }

            boolean found = false;
            List<String> prefixes = this.urisToPrefixes.get(nsURI);
            if (prefixes != null) {
                int index = 0;
                int size = prefixes.size();
                while (!found && index < size) {
                    nsPrefix = prefixes.get(index);
                    if (!mustHaveAPrefix || nsPrefix.length() > 0) {
                        found = true;
                    }
                    ++index;
                }
            }

            if (!found) {

                if (nsURI != null) {
                    nsPrefix = ePackage.getNsPrefix();
                }

                if (nsPrefix == null) {
                    nsPrefix = ""; //$NON-NLS-1$
                }
                if ("".equals(nsPrefix) && mustHaveAPrefix) { //$NON-NLS-1$
                    nsPrefix = IGsonConstants.CONSTANT_SEPARATOR;
                }

                nsPrefix = this.checkNsPrefixDuplicates(nsPrefix, nsURI);

                if (!this.packages.containsKey(ePackage)) {
                    this.packages.put(ePackage, nsPrefix);
                }

            }
        }
        return nsPrefix;
    }

    /**
     * Check if the nsPrefix is already use. if it is, add a suffix number and save the new nsPrefix to prefixesToURIs
     * and return it.
     *
     * @param nsPrefix
     *            the nsPrefix
     * @param nsURI
     *            the nsURI
     * @return the new nsPrefix
     */
    private String checkNsPrefixDuplicates(String nsPrefix, String nsURI) {
        String namespacePrefix = nsPrefix;
        if (this.prefixesToURIs.containsKey(namespacePrefix)) {
            String currentValue = this.prefixesToURIs.get(namespacePrefix);
            if (currentValue == null && nsURI != null || currentValue != null && !currentValue.equals(nsURI)) {
                int index = 1;
                while (this.prefixesToURIs.containsKey(namespacePrefix + IGsonConstants.CONSTANT_SEPARATOR + index)) {
                    ++index;
                }
                namespacePrefix += IGsonConstants.CONSTANT_SEPARATOR + index;
            }
        }
        this.prefixesToURIs.put(namespacePrefix, nsURI);
        return namespacePrefix;
    }

    /**
     * Return into a list, all the prefixes defined in the given EPackage.
     *
     * @param ePackage
     *            the EPAckage
     * @return EPackage prefixes list
     */
    public List<String> getPrefixes(EPackage ePackage) {
        List<String> result = new UniqueEList<String>();
        result.add(this.getPrefix(ePackage));
        String namespace = ePackage.getNsURI();
        if (this.extendedMetaData != null) {
            namespace = this.extendedMetaData.getNamespace(ePackage);
        }
        List<String> prefixes = this.urisToPrefixes.get(namespace);
        if (prefixes != null) {
            result.addAll(prefixes);
        }
        return result;
    }

    /**
     * Return the json name of the given element.
     *
     * @param element
     *            the given element
     * @return the json name of the given element
     */
    public String getName(ENamedElement element) {
        String name = element.getName();
        if (this.extendedMetaData != null) {
            if (element instanceof EStructuralFeature) {
                name = this.extendedMetaData.getName((EStructuralFeature) element);
            } else {
                name = this.extendedMetaData.getName((EClassifier) element);
            }
        }
        return name;
    }

    /**
     * Given an EFactory and a type name, find and return the type.
     *
     * @param eFactory
     *            the EFactory
     * @param typeName
     *            the type name
     * @return the type referenced by the type name in the EFactory
     */
    public EClassifier getType(EFactory eFactory, String typeName) {
        EClassifier eClassifier = null;
        if (eFactory != null) {
            EPackage ePackage = eFactory.getEPackage();
            if (this.extendedMetaData != null) {
                eClassifier = this.extendedMetaData.getType(ePackage, typeName);
            } else {
                eClassifier = ePackage.getEClassifier(typeName);
            }
        }
        return eClassifier;
    }

    /**
     * Sets the value of the feature for the given object.
     *
     * @param object
     *            the given object
     * @param feature
     *            the feature
     * @param value
     *            the value to set
     */
    public void setValue(EObject object, EStructuralFeature feature, Object value) {
        object.eSet(feature, value);
    }

    /**
     * Sets the value to feature for the given object at the given position.
     *
     * @param eObject
     *            the given eObject
     * @param feature
     *            the feature
     * @param value
     *            the value to set
     */
    public void setUniqueValue(EObject eObject, EStructuralFeature feature, Object value) {
        Object eGet = this.getValue(eObject, feature);
        if (eGet instanceof InternalEList) {
            @SuppressWarnings("unchecked")
            InternalEList<Object> eList = (InternalEList<Object>) eGet;
            if (!eList.contains(value)) {
                eList.addUnique(value);
            }
        }
    }

    /**
     * Returns the value of the given object for the feature.
     *
     * @param object
     *            the given object
     * @param feature
     *            the feature
     * @return the value of the given object for the feature
     */
    public Object getValue(EObject object, EStructuralFeature feature) {
        return object.eGet(feature);
    }

    /**
     * Returns the packages with their nsURI.
     *
     * @return The packages
     */
    public Map<EPackage, String> getPackages() {
        return this.packages;
    }

    /**
     * Returns the packages.
     *
     * @return the packages
     */
    public EPackage[] packages() {
        Map<String, EPackage> map = new TreeMap<String, EPackage>();

        // Sort and eliminate duplicates caused by having both a regular package and a demanded package for
        // the same nsURI.
        //
        for (EPackage ePackage : this.packages.keySet()) {
            String prefix = this.getPrefix(ePackage);
            if (prefix == null) {
                prefix = ""; //$NON-NLS-1$
            }
            EPackage conflict = map.put(prefix, ePackage);
            if (conflict != null && conflict.eResource() != null) {
                map.put(prefix, conflict);
            }
        }
        EPackage[] result = new EPackage[map.size()];
        map.values().toArray(result);
        return result;
    }

    /**
     * Returns the resourceURI.
     *
     * @return The resourceURI
     */
    public URI getResourceURI() {
        return this.resourceURI;
    }

    /**
     * Return an HREF to this obj from this resource.
     *
     * @param obj
     *            the EObject
     * @return an HREF to this obj from this resource
     */
    public String getHREF(EObject obj) {
        // TODO: when ticket on proxy handle will be checkouted look at XMLHelperImpl.getHREF(EObject) line
        // 772
        InternalEObject o = (InternalEObject) obj;

        URI objectURI = o.eProxyURI();
        if (objectURI == null) {
            Resource otherResource = obj.eResource();
            if (otherResource == null) {
                if (this.resource != null) {
                    objectURI = this.getHREF(otherResource, obj);
                } else {
                    this.handleDanglingHREF(obj);
                    return null;
                }
            } else {
                objectURI = this.getHREF(otherResource, obj);
            }
        }

        objectURI = this.deresolve(objectURI);

        return objectURI.toString();
    }

    /**
     * Return the given URI, deresolved. the URI is deresolved from the resource URI.
     *
     * @param uri
     *            the uri to deresolved
     * @return the deresolved URI
     */
    public URI deresolve(URI uri) {
        URI otherURI = uri;
        if (this.uriHandler != null) {
            otherURI = this.uriHandler.deresolve(uri);
        } else if (this.deresolve && !uri.isRelative()) {
            URI deresolvedURI = uri.deresolve(this.resourceURI, true, true, false);
            if (deresolvedURI.hasRelativePath()) {
                otherURI = deresolvedURI;
            }
        }
        return otherURI;
    }

    /**
     * Return an HREF to this obj from this resource.
     *
     * @param otherResource
     *            the Resource
     * @param obj
     *            the EObject
     * @return an HREF to this obj from this resource
     */
    private URI getHREF(Resource otherResource, EObject obj) {
        return otherResource.getURI().appendFragment(this.getURIFragment(otherResource, obj));
    }

    /**
     * Return the URIFragment corresponding to the given EObject in the given Resource.
     *
     * @param otherResource
     *            the Resource
     * @param obj
     *            the EObject
     * @return the URIFragment corresponding to the given EObject in the given Resource
     */
    private String getURIFragment(Resource otherResource, EObject obj) {
        String result = null;
        if (this.roots != null && otherResource == this.resource && !EcoreUtil.isAncestor(this.roots, obj)) {
            URI uriResult = null;
            this.handleDanglingHREF(obj);
            String uriFragment = this.extracted(uriResult);
            result = uriFragment;
        } else {
            result = otherResource.getURIFragment(obj);
            if (this.serializeFragmentReferencesAsXMIResource) {
                result = this.getURIFragment(obj);
            }
            if ("/-1".equals(result)) { //$NON-NLS-1$
                if (obj.eResource() != otherResource) {
                    URI uriResult = null;
                    this.handleDanglingHREF(obj);
                    String uriFragment = null;
                    uriFragment = this.extracted(uriResult);
                    result = uriFragment;
                }
            } else if (this.fragmentPrefixes != null) {
                for (int i = 0; i < this.fragmentPrefixes.length; i++) {
                    String fragmentPrefix = this.fragmentPrefixes[i];
                    if (result.startsWith(fragmentPrefix)) {
                        String stringIndex = ""; //$NON-NLS-1$
                        stringIndex = this.diffZeroIndexConverter(i, stringIndex);
                        result = JsonHelper.SEGMENT_SEPARATOR + stringIndex + result.substring(fragmentPrefix.length() - 1);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Same implementation as {@link org.eclipse.emf.ecore.resource.impl.ResourceImpl#getURIFragment(EObject)} .
     *
     * @See {@link org.eclipse.emf.ecore.resource.impl.ResourceImpl#getURIFragment(EObject)}
     * @param eObject
     *            the EObject which we want the fragment URI
     * @return The URI fragment that represent the given eObject
     */
    // CHECKSTYLE:OFF
    public String getURIFragment(EObject eObject) {
        String id = EcoreUtil.getID(eObject);
        if (id != null) {
            return id;
        }
        InternalEObject internalEObject = (InternalEObject) eObject;
        if (internalEObject.eDirectResource() == internalEObject.eResource()) {
            return JsonHelper.SEGMENT_SEPARATOR + this.getURIFragmentRootSegment(eObject);
        }
        SegmentSequence.Builder builder = SegmentSequence.newBuilder(JsonHelper.SEGMENT_SEPARATOR);

        boolean isContained = false;
        for (InternalEObject container = internalEObject.eInternalContainer(); container != null; container = internalEObject.eInternalContainer()) {
            builder.append(container.eURIFragmentSegment(internalEObject.eContainingFeature(), internalEObject));
            internalEObject = container;
            if (container.eDirectResource() == internalEObject.eResource()) {
                isContained = true;
                break;
            }
        }

        if (!isContained) {
            return "/-1"; //$NON-NLS-1$
        }

        builder.append(this.getURIFragmentRootSegment(internalEObject));
        builder.append(""); //$NON-NLS-1$
        builder.reverse();

        // Note that we convert it to a segment sequence because the most common use case is that
        // callers of this method will call URI.appendFragment.
        // By creating the segment sequence here, we ensure that it's found in the cache.
        //
        return builder.toSegmentSequence().toString();
    }

    /**
     * Same implementation as {@link org.eclipse.emf.ecore.resource.impl.ResourceImpl#getURIFragmentRootSegment}.
     *
     * @See {@link org.eclipse.emf.ecore.resource.impl.ResourceImpl#getURIFragmentRootSegment}
     * @param eObject
     *            the EObject
     * @return a String
     */
    protected String getURIFragmentRootSegment(EObject eObject) {
        List<EObject> contents = this.resource.getContents();
        return contents.size() > 1 ? Integer.toString(contents.indexOf(eObject)) : ""; //$NON-NLS-1$
    }

    // CHECKSTYLE:ON

    /**
     * Sets the mustHavePrefix.
     *
     * @param mustHavePrefix
     *            The mustHavePrefix to set
     */
    public void setMustHavePrefix(boolean mustHavePrefix) {
        this.mustHavePrefix = mustHavePrefix;
    }

    /**
     * Convert the given index in a String representation, only if the index is not equals to 0.
     *
     * @param index
     *            to convert
     * @param stringIndex
     *            the string version of the index
     * @return the converted String index
     */
    private String diffZeroIndexConverter(int index, String stringIndex) {
        String indexString = stringIndex;
        if (index != 0) {
            indexString = Integer.toString(index);
        }
        return indexString;
    }

    /**
     * Return the URIFragment if the given URI is not null.
     *
     * @param uri
     *            the URI
     * @return the URIFragment if the given URI is not null
     */
    private String extracted(URI uri) {
        String uriFragment = ""; //$NON-NLS-1$
        if (uri != null && uri.hasFragment()) {
            uriFragment = uri.fragment();
        }
        return uriFragment;
    }

    /**
     * Return the IDREF to this object.
     *
     * @param referenceValue
     *            the object
     * @return the IDREF to this object
     */
    public String getIDREF(EObject referenceValue) {
        String id = null;
        if (this.resource != null) {
            id = this.getURIFragment(this.resource, referenceValue);
        }
        return id;
    }

    /**
     * Add the given package to the package Registry with the extended meta-data behavior, if it is defined.
     *
     * @param ePackage
     *            the given EPackage
     */
    public void addPackage(EPackage ePackage) {
        if (this.extendedMetaData != null) {
            this.extendedMetaData.putPackage(this.extendedMetaData.getNamespace(ePackage), ePackage);
        } else {
            this.packageRegistry.put(ePackage.getNsURI(), ePackage);
        }
    }

    /**
     * Resolve the given relative URI from the given base URI.
     *
     * @param relative
     *            the given relative URI
     * @param base
     *            the given base URI
     * @return the resolved URI
     */
    public URI resolve(URI relative, URI base) {
        URI uri = relative;
        if (this.uriHandler != null) {
            uri = this.uriHandler.resolve(relative);
        } else {
            uri = relative.resolve(base);
        }
        return uri;
    }

    /**
     * Create an exception if the dangling references process is not
     * {@link JsonResource#OPTION_PROCESS_DANGLING_HREF_DISCARD "DISCARD"}.
     *
     * @param object
     *            the object without resource
     */
    private void handleDanglingHREF(EObject object) {
        if (!JsonResource.OPTION_PROCESS_DANGLING_HREF_DISCARD.equals(this.processDanglingReference)) {
            String location = "unknown"; //$NON-NLS-1$
            if (this.resource != null && this.resource.getURI() != null) {
                location = this.resource.getURI().toString();
            }
            DanglingHREFException exception = new DanglingHREFException("The Object '" + object //$NON-NLS-1$
                    + "' is not contained in a resource.", location); //$NON-NLS-1$

            if (this.danglingHREFException == null) {
                this.danglingHREFException = exception;
            }

            if (this.resource != null) {
                this.resource.getErrors().add(exception);
            }
        }
    }

    /**
     * Returns the danglingHREFException.
     *
     * @return The danglingHREFException
     */
    public DanglingHREFException getDanglingHREFException() {
        return this.danglingHREFException;
    }
}
