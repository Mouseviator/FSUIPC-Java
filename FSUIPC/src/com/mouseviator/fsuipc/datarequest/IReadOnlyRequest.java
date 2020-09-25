/**
 * <pre>
 * ##########################################################################################################
 * ######                            This file is part of Java FSUIPC SDK                              ######
 * ######                                        Version: 1.0                                          ######
 * ######         Based upon 64 bit Java SDK by Paul Henty who amended 32 bit SDK by Mark Burton       ######
 * ######                                   ©2020, Radek Henys                                         ######
 * ######                         All rights .... well, this will be LGPL or so                        ######
 * ######                                   http:\\mouseviator.com                                     ######
 * ##########################################################################################################
 * </pre>
 */
package com.mouseviator.fsuipc.datarequest;

/**
 * This interface extends the {@link IDataRequest} interface and should be used when implementing data request,
 * that is read only, as it provides default implementation of {@link IDataRequest#setValue(java.lang.Object) } function, which causes
 * exception every once called.
 * 
 * @author Mouseviator
 * @param <DataType> The data type of this data request, such as Byte, Short...
 */
public interface IReadOnlyRequest<DataType> extends IDataRequest<DataType> {
    /**
     * This is default implementation for read-only data request. Calling this will cause an exception!
     * @param value A value to set to be stored within this data request.
     */
    @Override
    default public void setValue(DataType value) {
        throw new UnsupportedOperationException("This is READ ONLY data request!");
    }
}
