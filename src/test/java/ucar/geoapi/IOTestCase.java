/*
 * Copyright (c) 2012-2021 Geomatys and University Corporation for Atmospheric Research/Unidata
 * Distributed under the terms of the BSD 3-Clause License.
 * SPDX-License-Identifier: BSD-3-Clause
 * See LICENSE for license information.
 */
package ucar.geoapi;

import java.io.IOException;
import org.opengis.test.dataset.TestData;
import ucar.nc2.NetcdfFile;


/**
 * Base class of netCDF test cases performing I/O operations. This base class provides an
 * {@link #open(TestData)} method for creating {@link NetcdfFile} objects from the build-in
 * test files.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
abstract strictfp class IOTestCase {
    /**
     * For subclass constructors only.
     */
    IOTestCase() {
    }

    /**
     * Opens the given netCDF file.
     *
     * @param  file  one of the {@code NETCDF_*} enumeration constants.
     * @return the netCDF file.
     * @throws IOException if an error occurred while opening the file.
     */
    final NetcdfFile open(final TestData file) throws IOException {
        /*
         * Binary netCDF files need to be read either from a file, or from a byte array in memory.
         * Reading from a file is not possible if the test file is in geoapi-conformance JAR file.
         * But since those test files are less than 15 kilobytes, loading them in memory is okay.
         */
        String location = file.location().toString();
        location = location.substring(location.lastIndexOf('/') + 1);
        return NetcdfFile.openInMemory(location, file.content());
    }
}
