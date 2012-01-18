/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.command.offense.lines;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.TacticalGraphicUtil;
import gov.nasa.worldwind.symbology.milstd2525.MilStd2525TacticalGraphic;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * Direction of Attack graphics. This class implements the following graphics:
 * <p/>
 * <ul> <li>Direction of Main Attack graphic (2.X.2.5.2.2.2.1)</li> <li>Direction of Supporting Attack graphic
 * (2.X.2.5.2.2.2.2)</li> </ul>
 *
 * @author pabercrombie
 * @version $Id$
 */
public class DirectionOfAttack extends MilStd2525TacticalGraphic
{
    /** Default length of the arrowhead, as a fraction of the total line length. */
    public final static double DEFAULT_ARROWHEAD_LENGTH = 0.1;
    /** Default angle of the arrowhead. */
    public final static Angle DEFAULT_ARROWHEAD_ANGLE = Angle.fromDegrees(60.0);
    /** Default width of the arrowhead outline. */
    public final static double DEFAULT_ARROWHEAD_OUTLINE_WIDTH = 0.3;

    /** Length of the arrowhead from base to tip, as a fraction of the total line length. */
    protected Angle arrowAngle = DEFAULT_ARROWHEAD_ANGLE;
    /** Angle of the arrowhead. */
    protected double arrowLength = DEFAULT_ARROWHEAD_LENGTH;
    /** Width of the arrowhead outline, as a fraction of the arrowhead length. */
    protected double outlineWidth = DEFAULT_ARROWHEAD_OUTLINE_WIDTH;

    /** First control point. */
    protected Position startPosition;
    /** Second control point. */
    protected Position endPosition;

    /** Path used to render the line. */
    protected Path[] paths;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(
            TacGrpSidc.C2GM_OFF_LNE_DIRATK_GRD_MANATK,
            TacGrpSidc.C2GM_OFF_LNE_DIRATK_GRD_SUPATK
        );
    }

    /**
     * Create a new arrow graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public DirectionOfAttack(String sidc)
    {
        super(sidc);
    }

    /**
     * Indicates the angle of the arrowhead.
     *
     * @return Angle of the arrowhead in the graphic.
     */
    public Angle getArrowAngle()
    {
        return this.arrowAngle;
    }

    /**
     * Specifies the angle of the arrowhead in the graphic.
     *
     * @param arrowAngle The angle of the arrowhead. Must be greater than zero degrees and less than 90 degrees.
     */
    public void setArrowAngle(Angle arrowAngle)
    {
        if (arrowAngle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (arrowAngle.degrees <= 0 || arrowAngle.degrees >= 90)
        {
            String msg = Logging.getMessage("generic.AngleOutOfRange");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.arrowAngle = arrowAngle;
    }

    /**
     * Indicates the length of the arrowhead.
     *
     * @return The length of the arrowhead as a fraction of the total line length.
     */
    public double getArrowLength()
    {
        return this.arrowLength;
    }

    /**
     * Specifies the length of the arrowhead.
     *
     * @param arrowLength Length of the arrowhead as a fraction of the total line length. If the arrowhead length is
     *                    0.25, then the arrowhead length will be one quarter of the total line length.
     */
    public void setArrowLength(double arrowLength)
    {
        if (arrowLength < 0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.arrowLength = arrowLength;
    }

    /**
     * Indicates the width of the arrowhead when it is drawn outlined. Note that the arrowhead is only drawn outlined
     * when the graphic represents a Main Direction of Attack.
     *
     * @return Width of the outline as a fraction of the arrowhead length.
     */
    public double getOutlineWidth()
    {
        return this.outlineWidth;
    }

    /**
     * Specifies the width of the the arrowhead when it is drawn outlined. Note that the arrowhead is only drawn
     * outlined when the graphic represents a Main Direction of Attack.
     *
     * @param outlineWidth Width of the outline as a fraction of the length of the arrowhead.
     */
    public void setOutlineWidth(double outlineWidth)
    {
        if (outlineWidth < 0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.outlineWidth = outlineWidth;
    }

    /**
     * {@inheritDoc}
     *
     * @param positions Control points that orient the graphic. Must provide at least three points.
     */
    public void setPositions(Iterable<? extends Position> positions)
    {
        if (positions == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            Iterator<? extends Position> iterator = positions.iterator();
            this.startPosition = iterator.next();
            this.endPosition = iterator.next();
        }
        catch (NoSuchElementException e)
        {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.paths = null; // Need to recompute path for the new control points
    }

    /** {@inheritDoc} */
    public Iterable<? extends Position> getPositions()
    {
        return Arrays.asList(this.startPosition, this.endPosition);
    }

    /** {@inheritDoc} */
    public Position getReferencePosition()
    {
        return this.startPosition;
    }

    /** {@inheritDoc} */
    public void doRenderGraphic(DrawContext dc)
    {
        if (this.paths == null)
        {
            this.createShapes(dc);
        }

        for (Path path : this.paths)
        {
            path.render(dc);
        }
    }

    /**
     * Indicates whether or not to draw the arrow head outlined.
     *
     * @return {@code true} if the arrow head should be drawn outlined.
     */
    protected boolean isDrawOutlined()
    {
        // Draw the arrow head outlined if this is a Main Attack graphic.
        return TacGrpSidc.C2GM_OFF_LNE_DIRATK_GRD_MANATK.equals(this.maskedSymbolCode);
    }

    /**
     * Create the list of positions that describe the arrow.
     *
     * @param dc Current draw context.
     */
    protected void createShapes(DrawContext dc)
    {
        this.paths = new Path[2];

        // Create a path for the line part of the arrow
        this.paths[0] = this.createPath(Arrays.asList(this.startPosition, this.endPosition));

        // Create the arrowhead
        List<Position> positions = this.computeArrowheadPositions(dc);
        this.paths[1] = createPath(positions);
    }

    /**
     * Determine the positions that make up the arrowhead.
     *
     * @param dc Current draw context.
     *
     * @return Positions that define the arrowhead.
     */
    protected List<Position> computeArrowheadPositions(DrawContext dc)
    {
        Globe globe = dc.getGlobe();

        // The arrowhead is drawn outlined for the Main Attack graphic, and as a single line for the Supporting Attack
        // graphic. When outlined, we need points A, B, C, D, E, and F. When not outlined the arrowhead is just points
        // A, B, and C.
        //
        //          F        _
        //         |\         |
        //        A\ \        | 1/2 width
        // ________B\ \ E    _|
        // Pt. 1    / /
        //        C/ /
        //         |/
        //         D
        //         | |
        //      Length

        Vec4 p1 = globe.computePointFromPosition(this.startPosition);
        Vec4 pB = globe.computePointFromPosition(this.endPosition);

        // Find vector in the direction of the arrow
        Vec4 vB1 = p1.subtract3(pB);

        double arrowLengthFraction = this.getArrowLength();

        // Find the point at the base of the arrowhead
        Vec4 arrowBase = pB.add3(vB1.multiply3(arrowLengthFraction));

        Vec4 normal = globe.computeSurfaceNormalAtPoint(arrowBase);

        // Compute the length of the arrowhead
        double arrowLength = vB1.getLength3() * arrowLengthFraction;
        double arrowHalfWidth = arrowLength * this.getArrowAngle().tanHalfAngle();

        // Compute a vector perpendicular to the segment and the normal vector
        Vec4 perpendicular = vB1.cross3(normal);
        perpendicular = perpendicular.normalize3().multiply3(arrowHalfWidth);

        // Find points A and C
        Vec4 pA = arrowBase.add3(perpendicular);
        Vec4 pC = arrowBase.subtract3(perpendicular);

        List<Position> positions;
        if (this.isDrawOutlined())
        {
            double outlineWidth = this.getOutlineWidth();

            // Find points D and F
            perpendicular = perpendicular.multiply3(1 + outlineWidth);
            Vec4 pF = arrowBase.add3(perpendicular);
            Vec4 pD = arrowBase.subtract3(perpendicular);

            Vec4 pE = pB.add3(vB1.normalize3().multiply3(-arrowLength * outlineWidth));

            positions = TacticalGraphicUtil.asPositionList(globe, pA, pB, pC, pD, pE, pF, pA);
        }
        else
        {
            positions = TacticalGraphicUtil.asPositionList(globe, pA, pB, pC);
        }

        return positions;
    }

    /**
     * Create and configure the Path used to render this graphic.
     *
     * @param positions Positions that define the path.
     *
     * @return New path configured with defaults appropriate for this type of graphic.
     */
    protected Path createPath(List<Position> positions)
    {
        Path path = new Path(positions);
        path.setFollowTerrain(true);
        path.setPathType(AVKey.GREAT_CIRCLE);
        path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        path.setDelegateOwner(this);
        path.setAttributes(this.getActiveShapeAttributes());
        return path;
    }
}
