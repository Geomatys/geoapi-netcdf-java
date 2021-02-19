/*
 * Copyright (c) 2012-2021 Geomatys and University Corporation for Atmospheric Research/Unidata
 * Distributed under the terms of the BSD 3-Clause License.
 * SPDX-License-Identifier: BSD-3-Clause
 * See LICENSE for license information.
 */
package ucar.geoapi;

import java.util.Objects;
import java.util.logging.Logger;
import org.opengis.metadata.Metadata;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.unidata.geoloc.Earth;
import ucar.unidata.geoloc.EarthEllipsoid;


/**
 * Base class of wrappers around an object from UCAR library.
 * This class provides static methods for creating wrappers.
 * For example the following provides some ISO 19115 metadata from a netCDF file:
 *
 * <pre>
 * NetcdfFile file = ...;
 * Metadata metadata = Wrapper.metadata(file);
 * System.out.println(metadata.getIdentificationInfo());</pre>
 *
 * Most of the work done by wrappers are delegated to the wrapped UCAR object.
 * Consequently changes in the UCAR object may be immediately reflected in {@code Wrapper}s.
 * However users are encouraged to not change the UCAR object after construction, because
 * some GeoAPI objects (in particular referencing objects) are expected to be immutable.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
public abstract class Wrapper {
    /**
     * For sub-class constructors.
     */
    Wrapper() {
    }

    /**
     * Creates a metadata object as a wrapper around the given netCDF file.
     * All getter methods fetch their values from the netCDF file, so changes to the netCDF
     * file content will be immediately reflected in the {@code Metadata} object.
     *
     * @param  file  the netCDF file, or {@code null} if none.
     * @return metadata for the given file, or {@code null} if the argument was null.
     */
    public static Metadata metadata(final NetcdfFile file) {
        return (file != null) ? new NetcdfMetadata(file) : null;
    }

    /**
     * Creates an ellipsoid for the given Earth shape.
     *
     * @param  earth  the netCDF Earth shape, or {@code null} if none.
     * @return an ellipsoid wrapping the given Earth shape, or {@code null} if the argument was null.
     */
    public static Ellipsoid wrap(final Earth earth) {
        if (earth == null) return null;
        if (earth == Earth.DEFAULT) return NetcdfEllipsoid.SPHERE;
        if (earth == EarthEllipsoid.WGS84) return NetcdfEllipsoid.WGS84;
        return new NetcdfEllipsoid(earth);
    }

    /**
     * Creates a new Coordinate Reference System wrapping the given netCDF coordinate system.
     * The returned object may implement any of the
     * {@link org.opengis.referencing.crs.ProjectedCRS},
     * {@link org.opengis.referencing.crs.GeographicCRS}
     * {@link org.opengis.referencing.crs.VerticalCRS} or
     * {@link org.opengis.referencing.crs.TemporalCRS},
     * depending on the {@linkplain ucar.nc2.constants.AxisType axis types}.
     * If the netCDF object contains axes of unknown type, then the returned CRS will not implement
     * any of the above-cited interfaces. If the netCDF object contains different kind of CRS,
     * then the returned CRS will be an instance of {@link org.opengis.referencing.crs.CompoundCRS}
     * in which each component implements one of the above-cited interfaces.
     *
     * <h4>Axis order</h4>
     * The order of axes in the returned CRS is reversed compared to the order of axes in the wrapped
     * netCDF coordinate system. This is because the netCDF convention stores axes in the
     * (<var>time</var>, <var>height</var>, <var>latitude</var>, <var>longitude</var>) order,
     * while the ISO 19111 (referencing by coordinates) objects uses the
     * (<var>longitude</var>, <var>latitude</var>, <var>height</var>, <var>time</var>) order.
     *
     * <h4>Limitations</h4>
     * Current implementation has the following restrictions:
     * <ul>
     *   <li>Supports only coordinate systems with axes of kind {@link ucar.nc2.dataset.CoordinateAxis1D}.
     *       Callers can verify this condition with a call to the {@link ucar.nc2.dataset.CoordinateSystem#isProductSet()}
     *       method on the wrapped netCDF coordinate system, which shall returns {@code true}.</li>
     *
     *   <li>At the time of writing, the netCDF API doesn't specify the CRS datum. Consequently current implementation
     *       assumes that all CRS instances use a spherical reference frame. We presume a sphere rather than WGS84
     *       because the netCDF projection framework uses spherical formulas.</li>
     *
     *   <li>The wrapper assumes that the list of netCDF axes returned by {@link CoordinateSystem#getCoordinateAxes()}
     *       is stable during the lifetime of this returned CRS instance.</li>
     * </ul>
     *
     * @param  netcdfCS  the netCDF coordinate system to wrap, or {@code null} if none.
     * @return a wrapper for the given object, or {@code null} if the argument was null.
     * @throws ClassCastException if at least one axis is not an instance of the
     *         {@link ucar.nc2.dataset.CoordinateAxis1D} subclass.
     */
    public static CoordinateReferenceSystem wrap(final CoordinateSystem netcdfCS) {
        return NetcdfCRS.wrap(netcdfCS, netcdfCS.getNetcdfDataset(), Logger.getLogger("ucar.geoapi"));
    }

    /**
     * Returns the wrapped netCDF object on which operations are delegated. Unless otherwise specified,
     * all objects returned by {@code metadata(…)} and {@code wrap(…)} methods can give back the wrapped
     * netCDF object as in the following example:
     *
     * <pre>
     * NetcdfFile file = ...;
     * Metadata metadata = Wrapper.metadata(file);
     * assert ((Wrapper) metadata).delegate() == file;</pre>
     *
     * @return the wrapped netCDF object on which operations are delegated.
     */
    public abstract Object delegate();

    /**
     * Compares this object with the given object for equality. The default implementation
     * returns {@code true} if the given object is non-null, wraps an object of the same
     * class than this object and the wrapped netCDF objects are equal.
     *
     * @param  other  the other object to compare with this object.
     * @return {@code true} if both objects are equal.
     *
     * @hidden
     */
    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (other != null && other.getClass() == getClass()) {
            return Objects.equals(delegate(), ((Wrapper) other).delegate());
        }
        return false;
    }

    /**
     * Returns a hash code value for this object. The default implementation
     * derives a value from the code returned by the wrapped netCDF object.
     *
     * @hidden
     */
    @Override
    public int hashCode() {
        return ~delegate().hashCode();
    }
}
