/*
 * Copyright (c) 2012-2021 Geomatys and University Corporation for Atmospheric Research/Unidata
 * Distributed under the terms of the BSD 3-Clause License.
 * SPDX-License-Identifier: BSD-3-Clause
 * See LICENSE for license information.
 */
package ucar.geoapi;

import java.util.Date;
import java.util.Set;
import java.util.Collection;
import java.util.Collections;
import java.io.Serializable;

import ucar.nc2.VariableSimpleIF;

import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.ReferenceIdentifier;


/**
 * An {@link IdentifiedObject} abstract base class backed by some netCDF object.
 * All methods in this class delegate their work to the wrapped netCDF object.
 * Consequently any change in the wrapped object is immediately reflected in this
 * {@code NetcdfIdentifiedObject} instance. However users are encouraged to not
 * change the wrapped object after construction, since GeoAPI referencing objects
 * are expected to be immutable.
 *
 * <p>This base class assumes that netCDF objects have a single name and no alias.
 * This assumption allows us to implement directly the {@link Identifier} interface.
 * The netCDF object name is returned by the {@link #getCode()} method.</p>
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
abstract class NetcdfIdentifiedObject extends Wrapper implements IdentifiedObject, ReferenceIdentifier, Serializable {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 8740287489596438703L;

    /**
     * Creates a new {@code NetcdfIdentifiedObject} instance.
     */
    protected NetcdfIdentifiedObject() {
    }

    /**
     * Returns the netCDF citation.
     */
    @Override
    public Citation getAuthority() {
        return SimpleCitation.NETCDF;
    }

    /**
     * Returns the {@code "netCDF"} constant, which is used as the code space.
     */
    @Override
    public String getCodeSpace() {
        return "netCDF";
    }

    /**
     * Returns the version of the netCDF library. The default implementation
     * fetches this information from the {@code META-INF/MANIFEST.MF} file in
     * the netCDF JAR file.
     */
    @Override
    public String getVersion() {
        final Package p = VariableSimpleIF.class.getPackage();
        return (p != null) ? p.getImplementationVersion() : null;
    }

    /**
     * Returns a code which identify this instance. This is typically the value
     * returned by the {@code getName()} method of the wrapped netCDF object.
     */
    @Override
    public abstract String getCode();

    /**
     * Returns the name of this identified object. The default implementation returns
     * {@code this}, so subclasses shall returns the name in their implementation of
     * the {@link #getCode()} method.
     */
    @Override
    public ReferenceIdentifier getName() {
        return this;
    }

    /**
     * Returns identifiers which references elsewhere the object's defining information.
     * Alternatively an identifier by which this object can be referenced.
     *
     * @return this object identifiers, or an empty collection if there is none.
     */
    @Override
    public Set<ReferenceIdentifier> getIdentifiers() {
        return Collections.emptySet();
    }

    /**
     * Returns alternative names by which this object is identified.
     *
     * @return alternative names and abbreviations, or an empty collection if there is none.
     */
    @Override
    public Collection<GenericName> getAlias() {
        return Collections.emptyList();
    }

    /**
     * Returns the area or region or timeframe in which this object is valid, or {@code null} if none.
     * The default implementation returns a geographic extent for the world, since most netCDF objects
     * except {@link ucar.unidata.geoloc.Projection} are not restricted to a particular area.
     *
     * @return the valid domain, or {@code null} if not available.
     *
     * @see NetcdfCRS#getDomainOfValidity()
     * @see NetcdfProjection#getDomainOfValidity()
     */
    public Extent getDomainOfValidity() {
        return SimpleGeographicBoundingBox.WORLD;
    }

    /**
     * Returns the description of domain of usage, or limitations of usage, for which this object
     * is valid. The default implementation returns {@code null} in all cases, since netCDF objects
     * don't specify their scope.
     *
     * <p>Scope is a
     * {@link org.opengis.referencing.datum.Datum#getScope() Datum},
     * {@link org.opengis.referencing.ReferenceSystem#getScope() ReferenceSystem} and
     * {@link org.opengis.referencing.operation.CoordinateOperation#getScope() CoordinateOperation}
     * property.</p>
     *
     * @return the domain of usage, or {@code null} if none.
     */
    public InternationalString getScope() {
        return null;
    }

    /**
     * Returns a description, possibly including coordinates of an identified point or points,
     * of the relationship used to anchor the coordinate system to the Earth or alternate object.
     * The default implementation returns {@code null} since this simple implementation does not
     * define anchor point.
     *
     * <p>Anchor point is a
     * {@link org.opengis.referencing.datum.Datum#getAnchorPoint() Datum} property.</p>
     *
     * @return a description of the anchor point, or {@code null} if none.
     */
    public InternationalString getAnchorPoint() {
        return null;
    }

    /**
     * Returns The time after which this datum definition is valid. The default implementation
     * returns {@code null} since this simple implementation does not define realization epoch.
     *
     * <p>Anchor point is a
     * {@link org.opengis.referencing.datum.Datum#getRealizationEpoch() Datum} property.</p>
     *
     * @return the datum realization epoch, or {@code null} if not available.
     */
    public Date getRealizationEpoch() {
        return null;
    }

    /**
     * Returns comments on or information about this object, including data source information.
     *
     * @return the remarks, or {@code null} if none.
     */
    @Override
    public InternationalString getRemarks() {
        return null;
    }

    /**
     * Returns a string representation of this object {@linkplain #getName() name}.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder(getCodeSpace()).append(':');
        final String name = getCode().trim();
        final boolean needsQuote = (name.indexOf(' ') >= 0);
        if (needsQuote) {
            buffer.append('"');
        }
        buffer.append(name);
        if (needsQuote) {
            buffer.append('"');
        }
        return buffer.toString();
    }

    /**
     * Returns a <cite>Well-Known Text</cite> representation of this object, if this
     * operation is supported. The default implementation thrown an exception in all
     * cases.
     */
    @Override
    public String toWKT() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
