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
 * that is write only, as it provides default implementation of {@link IDataRequest#getValue() } function, which causes
 * exception every once called.
 * 
 * @author Mouseviator
 * @param <DataType> The data type of this data request, such as Byte, Short...
 */
public interface IWriteOnlyRequest <DataType> extends IDataRequest<DataType> {

    /**
     * This is default implementation for write-only data request. Calling this will cause an exception!
     * 
     * @return The value stored within this data request.
     */
    @Override
    default public DataType getValue() {
        throw new UnsupportedOperationException("This is WRITE ONLY data request!");
    }    
}
