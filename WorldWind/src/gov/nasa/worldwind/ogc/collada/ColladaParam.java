/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.ogc.collada;

/**
 * Represents the Collada <i>Param</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id$
 */
public class ColladaParam extends ColladaAbstractObject
{
    public ColladaParam(String ns)
    {
        super(ns);
    }

    public String getName()
    {
        return (String) this.getField("name");
    }
}
