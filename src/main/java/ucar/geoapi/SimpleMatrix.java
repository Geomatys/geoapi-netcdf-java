/*
 * Copyright (c) 2012-2021 Geomatys and University Corporation for Atmospheric Research/Unidata
 * Distributed under the terms of the BSD 3-Clause License.
 * SPDX-License-Identifier: BSD-3-Clause
 * See LICENSE for license information.
 */
package ucar.geoapi;

import org.opengis.referencing.operation.Matrix;
import ucar.ma2.MAMatrix;


/**
 * A {@link Matrix} built on top of UCAR library.
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
final class SimpleMatrix extends MAMatrix implements Matrix {
    /**
     * Creates a matrix of size {@code size}&nbsp;×&nbsp;{@code size}.
     * Elements on the diagonal (<var>j</var> == <var>i</var>) are set to 1.
     */
    SimpleMatrix(final int size) {
        super(size, size);
        for (int i=0; i<size; i++) {
            setDouble(i, i, 1);
        }
    }

    /**
     * Creates a copy of given matrix.
     */
    private SimpleMatrix(final MAMatrix other) {
        super(other.getNrows(), other.getNcols());
        for (int j=0; j<getNrows(); j++) {
            for (int i=0; i<getNcols(); i++) {
                setDouble(j, i, other.getDouble(j, i));
            }
        }
    }

    /** Returns matrix size. */
    @Override public int    getNumRow() {return getNrows();}
    @Override public int    getNumCol() {return getNcols();}
    @Override public double getElement(int row, int col) {return getDouble(row, col);}
    @Override public void   setElement(int row, int col, double v) {setDouble(row, col, v);}

    /**
     * Returns {@code true} if this matrix is an identity matrix.
     */
    @Override
    public boolean isIdentity() {
        final int numRow = getNumRow();
        final int numCol = getNumCol();
        if (numRow != numCol) {
            return false;
        }
        for (int j=0; j<numRow; j++) {
            for (int i=0; i<numCol; i++) {
                if (getElement(j,i) != (i==j ? 1 : 0)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns a clone of this matrix.
     */
    @Override
    public SimpleMatrix clone() {
        return new SimpleMatrix(this);
    }
}
