/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

/**
 * Represents the Collada <i>Source</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id$
 */
public class ColladaSource extends ColladaAbstractObject
{
    public ColladaSource(String ns)
    {
        super(ns);
    }

    public ColladaAccessor getAccessor()
    {
        // Handles only the COLLADA Common profile
        ColladaTechniqueCommon technique = (ColladaTechniqueCommon) this.getField("technique_common");
        if (technique == null)
            return null;

        return (ColladaAccessor) technique.getField("accessor");
    }
}
