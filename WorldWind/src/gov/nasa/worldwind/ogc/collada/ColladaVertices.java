/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

import java.util.*;

/**
 * Represents the Collada <i>Vertices</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id$
 */
public class ColladaVertices extends ColladaAbstractObject
{
    protected List<ColladaInput> inputs = new ArrayList<ColladaInput>();

    public ColladaVertices(String ns)
    {
        super(ns);
    }

    public List<ColladaInput> getInputs()
    {
        return this.inputs;
    }

    public ColladaInput getPositionInput()
    {
        for (ColladaInput input : this.getInputs())
        {
            if ("POSITION".equals(input.getSemantic()))
                return input;
        }
        return null;
    }

    @Override
    public void setField(String keyName, Object value)
    {
        if (keyName.equals("input"))
        {
            this.inputs.add((ColladaInput) value);
        }
        else
        {
            super.setField(keyName, value);
        }
    }
}
