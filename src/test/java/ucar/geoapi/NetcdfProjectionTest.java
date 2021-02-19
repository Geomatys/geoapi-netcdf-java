/*
 * Copyright (c) 2012-2021 Geomatys and University Corporation for Atmospheric Research/Unidata
 * Distributed under the terms of the BSD 3-Clause License.
 * SPDX-License-Identifier: BSD-3-Clause
 * See LICENSE for license information.
 */
package ucar.geoapi;

import java.util.Random;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.projection.Mercator;

import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.operation.SingleOperation;
import org.opengis.referencing.operation.TransformException;
import org.opengis.test.referencing.TransformTestCase;

import org.junit.Test;

import static org.opengis.test.Assert.*;


/**
 * Tests the {@link NetcdfProjection} class using the
 * <code><a href="http://www.geoapi.org/conformance/index.html">geoapi-conformance</a></code> module.
 * The projected values correctness (external consistency) is not verified - only internal consistency is verified.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
public final strictfp class NetcdfProjectionTest extends TransformTestCase {
    /**
     * The coordinate operation wrapping the netCDF projection. This field is initialized
     * to the value returned by {@link #wrap(Projection)} before a test is executed.
     *
     * <p>The {@link #transform} field will be set to the {@link SingleOperation#getMathTransform()}
     * value.</p>
     */
    private SingleOperation operation;

    /**
     * Creates a new test case initialized with a default {@linkplain #tolerance tolerance}
     * threshold. The "{@linkplain #isDerivativeSupported is derivative supported}" flag is
     * set to {@code false} since the netCDF library does not implement projection derivatives.
     */
    public NetcdfProjectionTest() {
        super(NetcdfTransformFactoryTest.getDefaultFactory());
        tolerance = 1E-10;
        isDerivativeSupported = false;
        /*
         * Our objects are not yet strictly ISO compliant, so be lenient...
         */
        validators.coordinateOperation.requireMandatoryAttributes = false;
    }

    /**
     * Initializes the {@link #operation} and {@link #transform} fields to the Mercator projection.
     */
    private void createMercatorProjection() {
        final Mercator projection = new Mercator();
        operation = wrap(projection);
        transform = operation.getMathTransform();
        validators.validate(operation);
    }

    /**
     * Wraps the given netCDF projection into a GeoAPI operation object.
     *
     * @param  projection  the netCDF projection to wrap.
     * @return an operation implementation created from the given projection.
     */
    private SingleOperation wrap(final Projection projection) {
        return new NetcdfProjection(projection, null, null, null);
    }

    /**
     * Tests the consistency of various {@code transform} methods. This method runs the
     * {@link #verifyInDomain(double[], double[], int[], Random)} test method using a
     * simple {@link Mercator} implementation.
     *
     * @throws TransformException should never happen.
     */
    @Test
    public void testConsistency() throws TransformException {
        createMercatorProjection();
        verifyInDomain(new double[] {-180, -80}, // Minimal ordinate values to test.
                       new double[] {+180, +80}, // Maximal ordinate values to test.
                       new int[]    { 360, 160}, // Number of points to test.
                       new Random(216919106));
    }

    /**
     * Tests projection name and classname.
     */
    @Test
    public void testNames() {
        createMercatorProjection();
        final SingleOperation operation = this.operation;               // Protect from changes.
        assertEquals("Mercator", operation.getName().getCode());
        assertEquals("Mercator", operation.getMethod().getName().getCode());
    }

    /**
     * Tests the {@link NetcdfProjection#getDomainOfValidity()} method.
     *
     * <p><b>Note:</b> In netCDF 4.2, the declared bounding box was approximately
     * <var>west</var>  = -152.85°,
     * <var>east</var>  = -57.15°,
     * <var>south</var> = -43.1° and
     * <var>north</var> = 43.1°.
     * However we presume that this bounding box may change in the future.</p>
     */
    @Test
    public void testDomainOfValidity() {
        createMercatorProjection();
        final SingleOperation operation = this.operation;               // Protect from changes.
        final GeographicBoundingBox box = (GeographicBoundingBox)
                operation.getDomainOfValidity().getGeographicElements().iterator().next();
        assertBetween("westBoundLongitude", -180, -152, box.getWestBoundLongitude());
        assertBetween("eastBoundLongitude",  -58, +180, box.getEastBoundLongitude());
        assertBetween("southBoundLatitude",  -90,  -43, box.getSouthBoundLatitude());
        assertBetween("northBoundLatitude",   43,  +90, box.getNorthBoundLatitude());
    }
}
