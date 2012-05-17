/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

/**
 * Represents the Collada <i>Input</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id$
 */
public class ColladaInput extends ColladaAbstractObject
{
    public ColladaInput(String ns)
    {
        super(ns);
    }

    public int getOffset()
    {
        return Integer.parseInt((String) this.getField("offset"));
    }

    public String getSource()
    {
        return (String) this.getField("source");
    }

    public String getSemantic()
    {
        return (String) this.getField("semantic");
    }
}
