/*
 * Copyright (c) 2012-2021 Geomatys and University Corporation for Atmospheric Research/Unidata
 * Distributed under the terms of the BSD 3-Clause License.
 * SPDX-License-Identifier: BSD-3-Clause
 * See LICENSE for license information.
 */
package ucar.geoapi;

import java.util.EnumSet;
import org.opengis.util.Factory;
import org.opengis.test.TestSuite;
import org.opengis.test.Configuration;
import org.opengis.test.ToleranceModifier;
import org.opengis.test.ToleranceModifiers;
import org.opengis.test.ValidatorContainer;
import org.opengis.test.ImplementationDetails;
import org.opengis.referencing.operation.MathTransform;

import static org.opengis.test.CalculationType.*;


/**
 * Runs all supported tests from the
 * <code><a href="http://www.geoapi.org/conformance/index.html">geoapi-conformance</a></code>
 * module.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
public class ConformanceTest extends TestSuite implements ImplementationDetails {
    /**
     * The configuration of our netCDF tests.
     * Will be created when first needed.
     */
    private Configuration configuration;

    /**
     * Returns {@code true} if at least one factory in the given array is our implementation.
     * We will returns a configuration map only for our own implementation, and don't propose
     * anything for implementations we don't known about.
     */
    private static boolean isOurImplementation(final Factory[] factories) {
        for (final Factory factory : factories) {
            if (factory instanceof NetcdfTransformFactory) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the map of tests to disable for this implementation, or {@code null}
     * if the given factories are not netCDF implementations.
     */
    @Override
    public synchronized Configuration configuration(final Factory... factories) {
        if (!isOurImplementation(factories)) {
            return null;
        }
        if (configuration == null) {
            configuration = new Configuration();
            configuration.unsupported(
                    Configuration.Key.isStandardNameSupported,
                    Configuration.Key.isStandardAliasSupported,
                    Configuration.Key.isDerivativeSupported);
            /*
             * Our objects are not yet strictly ISO compliant, so be lenient...
             */
            final ValidatorContainer validators = new ValidatorContainer();
            validators.crs.enforceStandardNames = false;
            validators.naming.requireMandatoryAttributes = false;
            validators.metadata.requireMandatoryAttributes = false;
            validators.coordinateOperation.requireMandatoryAttributes = false;
            configuration.put(Configuration.Key.validators, validators);
        }
        return configuration;
    }

    /**
     * Relaxes the tolerance threshold for some transforms to be tested.
     */
    @Override
    public ToleranceModifier tolerance(final MathTransform transform) {
        return ToleranceModifiers.scale(EnumSet.of(DIRECT_TRANSFORM, INVERSE_TRANSFORM), 100, 100);
    }
}
