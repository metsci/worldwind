/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.text.*;
import java.util.*;
import java.util.List;

/**
 * Base class for tactical graphics defined by <a href="http://www.assistdocs.com/search/document_details.cfm?ident_number=114934">MIL-STD-2525</a>.
 * See the TacticalGraphic <a title="Tactical Graphic Usage Guide" href="http://goworldwind.org/developers-guide/symbology/tactical-graphics/"
 * target="_blank">Usage Guide</a> for instructions on using TacticalGraphic in an application.
 * <p/>
 * The following table lists the modifiers supported by 2525 graphics. Note that not all graphics support all modifiers.
 * <table width="100%"> <tr><th>Field</th><th>Modifier key</th><th>Data type</th><th>Description</th></tr>
 * <tr><td>A</td><td>AVKey.SYMBOL</td><td>{@link TacticalSymbol}</td><td>Symbol icon</td></tr>
 * <tr><td>B</td><td>AVKey.ECHELON</td><td>String</td><td>Echelon</td></tr> <tr><td>C</td><td>AVKey.QUANTITY</td><td>String</td><td>Quantity</td></tr>
 * <tr><td>H</td><td>AVKey.DESCRIPTION</td><td>String</td><td>Additional information</td></tr>
 * <tr><td>N</td><td>AVKey.SHOW_HOSTILE</td><td>Boolean</td><td>Show/hide hostile entity indicator</td></tr>
 * <tr><td>Q</td><td>AVKey.HEADING</td><td>{@link gov.nasa.worldwind.geom.Angle}</td><td>Direction indicator</td></tr>
 * <tr><td>S</td><td>AVKey.OFFSET</td><td>{@link gov.nasa.worldwind.render.Offset}</td><td>Offset location
 * indicator</td></tr> <tr><td>T</td><td>AVKey.TEXT</td><td>String</td><td>Unique designation</td></tr>
 * <tr><td>V</td><td>AVKey.TYPE</td><td>String</td><td>Type</td></tr> <tr><td>W</td><td>AVKey.DATE_TIME</td><td>Date</td><td>Date/time</td></tr>
 * <tr><td>X</td><td>AVKey.ALTITUDE</td><td>Double</td><td>Altitude/depth</td></tr>
 * <tr><td>Y</td><td>AVKey.SHOW_POSITION</td><td>Boolean</td><td>Show/hide position field</td></tr>
 * <tr><td>AM</td><td>AVKey.RADIUS, AVKey.WIDTH, AVKey.LENGTH</td><td>Double</td><td>Radius, length or width of
 * rectangle.</td></tr> <tr><td>AN</td><td>AVKey.AZIMUTH</td><td>Angle</td><td>Azimuth</td></tr>
 * <tr><td>--</td><td>AVKey.GRAPHIC</td><td>{@link TacticalGraphic}</td><td>Child graphic</td></tr> </table>
 * <p/>
 * Here's an example of setting modifiers during construction of a graphic:
 * <pre>
 * AVList modifiers = new AVListImpl();
 * modifiers.setValue(AVKey.TEXT, "X469"); // Field T
 * modifiers.setValue(AVKey.DATE_TIME, new Date()); // Field W
 * modifiers.setValue(AVKey.ADDITIONAL_INFO, "Anthrax Suspected"); // Field H
 * modifiers.setValue(AVKey.HEADING, Angle.fromDegrees(30.0)); // Field Q
 *
 * Position position = Position.fromDegrees(35.1026, -118.348, 0);
 *
 * // Create the graphic with the modifier list
 * TacticalGraphic graphic = factory.createGraphic("GHMPNEB----AUSX", positions, modifiers);
 * </pre>
 * <p/>
 * Some graphics support multiple instances of a modifier. For example, 2525 uses the field code W for a date/time
 * modifier. Some graphics support multiple timestamps, in which case the fields are labeled W, W1, W2, etc. An
 * application can pass an {@link Iterable} to <code>setModifier</code> if multiple values are required to specify the
 * modifier. Here's an example of how to specify two timestamps:
 * <pre>
 * Date startDate = ...
 * Date endData = ...
 *
 * graphic.setModifier(AVKey.DATE_TIME, Arrays.asList(startDate, endDate));
 * </pre>
 *
 * @author pabercrombie
 * @version $Id$
 */
// TODO give the app a way to control formatting of date/time
public abstract class MilStd2525TacticalGraphic extends AVListImpl implements TacticalGraphic, Renderable
{
    /** Text modifier used to indicate hostile entities. */
    public final static String HOSTILE_INDICATOR = "ENY";

    public final static Material MATERIAL_FRIEND = Material.BLACK;
    public final static Material MATERIAL_HOSTILE = Material.RED;

    /** Default date pattern used to format dates. */
    public final static String DEFAULT_DATE_PATTERN = "ddhhmmss'Z'MMMyyyy";
    public final static String TIME_PATTERN = "hhmmss'Z'";

    /** The default highlight color. */
    protected static final Material DEFAULT_HIGHLIGHT_MATERIAL = Material.WHITE;

    protected String text;

    protected boolean highlighted;
    protected boolean visible = true;
    protected boolean showModifiers = true;
    protected TacticalGraphicAttributes normalAttributes;
    protected TacticalGraphicAttributes highlightAttributes;

    protected String functionId;
    protected String standardIdentity;
    protected String echelon;
    protected String status;
    protected String countryCode;

    protected AVList modifiers;

    protected List<Label> labels;

    protected long frameTimestamp = -1L;

    protected TacticalGraphicAttributes activeOverrides = new BasicTacticalGraphicAttributes();
    protected ShapeAttributes activeShapeAttributes = new BasicShapeAttributes();

    /** Flag to indicate that labels must be recreated before the graphic is rendered. */
    protected boolean mustCreateLabels = true;

    public abstract String getCategory();

    protected abstract void doRenderGraphic(DrawContext dc);

    /** {@inheritDoc} */
    public String getIdentifier()
    {
        SymbolCode symCode = new SymbolCode();
        symCode.setStandardIdentity(this.standardIdentity);
        symCode.setEchelon(this.echelon);
        symCode.setCategory(this.getCategory());
        symCode.setFunctionId(this.getFunctionId());

        return symCode.toString();
    }

    /** {@inheritDoc} */
    public boolean isVisible()
    {
        return this.visible;
    }

    /** {@inheritDoc} */
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    /** {@inheritDoc} */
    public TacticalGraphicAttributes getAttributes()
    {
        return this.normalAttributes;
    }

    /** {@inheritDoc} */
    public void setAttributes(TacticalGraphicAttributes attributes)
    {
        this.normalAttributes = attributes;
    }

    /** {@inheritDoc} */
    public TacticalGraphicAttributes getHighlightAttributes()
    {
        return this.highlightAttributes;
    }

    /** {@inheritDoc} */
    public void setHighlightAttributes(TacticalGraphicAttributes attributes)
    {
        this.highlightAttributes = attributes;
    }

    /** {@inheritDoc} */
    public boolean isHighlighted()
    {
        return this.highlighted;
    }

    /** {@inheritDoc} */
    public void setHighlighted(boolean highlighted)
    {
        this.highlighted = highlighted;
    }

    /** {@inheritDoc} */
    public Object getModifier(String modifier)
    {
        if (AVKey.TEXT.equals(modifier))
        {
            return this.text;
        }
        else if (SymbologyConstants.FUNCTION_ID.equals(modifier))
        {
            return this.getFunctionId();
        }
        else if (SymbologyConstants.STANDARD_IDENTITY.equals(modifier))
        {
            return this.getStandardIdentity();
        }
        else if (SymbologyConstants.ECHELON.equals(modifier))
        {
            return this.getEchelon();
        }
        else if (SymbologyConstants.STATUS.equals(modifier))
        {
            return this.getStatus();
        }
        else
        {
            return this.modifiers.getValue(modifier);
        }
    }

    /** {@inheritDoc} */
    public void setModifier(String modifier, Object value)
    {
        if (AVKey.TEXT.equals(modifier) && (value instanceof String))
        {
            this.setText((String) value);
        }
        else if (SymbologyConstants.FUNCTION_ID.equals(modifier) && (value instanceof String))
        {
            this.setFunctionId((String) value);
        }
        else if (SymbologyConstants.STANDARD_IDENTITY.equals(modifier) && (value instanceof String))
        {
            this.setStandardIdentity((String) value);
        }
        else if (SymbologyConstants.ECHELON.equals(modifier) && (value instanceof String))
        {
            this.setEchelon((String) value);
        }
        else if (SymbologyConstants.STATUS.equals(modifier) && (value instanceof String))
        {
            this.setStatus((String) value);
        }
        else
        {
            if (this.modifiers == null)
                this.modifiers = new AVListImpl();

            this.modifiers.setValue(modifier, value);
        }
    }

    /** {@inheritDoc} */
    public boolean isShowModifiers()
    {
        return this.showModifiers;
    }

    /** {@inheritDoc} */
    public void setShowModifiers(boolean showModifiers)
    {
        this.showModifiers = showModifiers;
    }

    /**
     * Indicates the scheme portion of the graphic identifier.
     *
     * @return The scheme of this graphic.
     */
    public String getScheme()
    {
        return SymbologyConstants.SCHEME_TACTICAL_GRAPHICS;
    }

    /**
     * Indicates the function ID of this graphic.
     *
     * @return The graphic's function ID.
     */
    public String getFunctionId()
    {
        return this.functionId;
    }

    /**
     * Specifies the function ID of this graphic. This may cause the graphic to change how it draws itself.
     *
     * @param functionId New function ID.
     */
    public void setFunctionId(String functionId)
    {
        this.functionId = functionId;
    }

    public String getStandardIdentity()
    {
        return this.standardIdentity;
    }

    public void setStandardIdentity(String standardIdentity)
    {
        if (standardIdentity == null)
        {
            String msg = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.standardIdentity = standardIdentity;
    }

    public String getEchelon()
    {
        return this.echelon;
    }

    public void setEchelon(String echelon)
    {
        if (echelon == null)
        {
            String msg = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.echelon = echelon;
    }

    /**
     * Indicates the status portion of the graphic identifier.
     *
     * @return The status associated with this graphic.
     */
    public String getStatus()
    {
        return this.status;
    }

    /**
     * Specifies the status portion of the graphic identifier.
     *
     * @param status The status associated with this graphic.
     */
    public void setStatus(String status)
    {
        this.status = status;
    }

    /**
     * Indicates the country code portion of the graphic identifier.
     *
     * @return The country code associated with this graphic.
     */
    public String getCountryCode()
    {
        return this.countryCode;
    }

    /**
     * Specifies the country code portion of the graphic identifier.
     *
     * @param countryCode The country code associated with this graphic.
     */
    public void setCountryCode(String countryCode)
    {
        this.countryCode = countryCode;
    }

    /** {@inheritDoc} */
    public String getText()
    {
        return this.text;
    }

    /** {@inheritDoc} */
    public void setText(String text)
    {
        this.text = text;
    }

    /////////////
    // Rendering
    /////////////

    /** {@inheritDoc} */
    public void render(DrawContext dc)
    {
        if (!this.isVisible())
        {
            return;
        }

        this.determinePerFrameAttributes(dc);

        this.doRenderGraphic(dc);

        if (this.isShowModifiers())
        {
            this.doRenderModifiers(dc);
        }
    }

    /**
     * Determine geometry and attributes for this frame. This method only determines attributes the first time that it
     * is called for each frame. Multiple calls in the same frame will have no effect.
     *
     * @param dc Current draw context.
     */
    protected void determinePerFrameAttributes(DrawContext dc)
    {
        long timeStamp = dc.getFrameTimeStamp();
        if (this.frameTimestamp != timeStamp)
        {
            // Allow the subclass to create labels, if necessary
            if (this.mustCreateLabels)
            {
                this.createLabels();
                this.mustCreateLabels = false;
            }

            this.determineActiveAttributes();
            this.computeGeometry(dc);
            this.frameTimestamp = timeStamp;
        }
    }

    public void doRenderModifiers(DrawContext dc)
    {
        if (this.labels != null)
        {
            for (Label label : this.labels)
            {
                label.render(dc);
            }
        }
    }

    /**
     * Determine positions for the start and end labels.
     *
     * @param dc Current draw context.
     */
    protected void determineLabelPositions(DrawContext dc)
    {
        // Do nothing, but allow subclasses to override
    }

    protected void createLabels()
    {
        // Do nothing, but allow subclasses to override
    }

    protected Label addLabel(String text)
    {
        if (this.labels == null)
            this.labels = new ArrayList<Label>();

        Label label = new Label();
        label.setText(text);
        label.setDelegateOwner(this);
        label.setTextAlign(AVKey.CENTER);
        this.labels.add(label);

        return label;
    }

    protected void computeGeometry(DrawContext dc)
    {
        // Allow the subclass to decide where to put the labels
        this.determineLabelPositions(dc);
    }

    /**
     * Format a date to a string.
     *
     * @param d Date to format.
     *
     * @return The formatted date.
     */
    protected String formatDate(Date d)
    {
        DateFormat format = new SimpleDateFormat(TIME_PATTERN);
        return format.format(d);
    }

    /** Determine active attributes for this frame. */
    protected void determineActiveAttributes()
    {
        // Apply defaults for this graphic
        this.applyDefaultAttributes(this.activeShapeAttributes);

        if (this.isHighlighted())
        {
            TacticalGraphicAttributes highlightAttributes = this.getHighlightAttributes();

            // If the application specified overrides to the highlight attributes, then apply the overrides
            if (highlightAttributes != null)
            {
                this.activeOverrides.copy(highlightAttributes);

                // Apply overrides specified by application
                this.applyOverrideAttributes(highlightAttributes, this.activeShapeAttributes);
            }
            else
            {
                // If no highlight attributes have been specified we need to use the normal attributes but adjust them
                // to cause highlighting.
                this.activeShapeAttributes.setOutlineMaterial(DEFAULT_HIGHLIGHT_MATERIAL);
                this.activeShapeAttributes.setInteriorMaterial(DEFAULT_HIGHLIGHT_MATERIAL);
            }
        }
        else
        {
            // Apply overrides specified by application
            TacticalGraphicAttributes normalAttributes = this.getAttributes();
            if (normalAttributes != null)
            {
                this.activeOverrides.copy(normalAttributes);
                this.applyOverrideAttributes(normalAttributes, this.activeShapeAttributes);
            }
        }

        this.applyLabelAttributes();
    }

    /** Apply the active attributes to the graphic's labels. */
    protected void applyLabelAttributes()
    {
        if (this.labels == null || labels.isEmpty())
            return;

        Material labelMaterial = this.getLabelMaterial();

        Font font = this.activeOverrides.getTextModifierFont();
        if (font == null)
            font = Label.DEFAULT_FONT;

        for (Label label : this.labels)
        {
            label.setMaterial(labelMaterial);
            label.setFont(font);
        }

        // Apply the offset to the main label.
        Offset offset = this.activeOverrides.getLabelOffset();
        if (offset == null)
            offset = this.getDefaultLabelOffset();
        this.labels.get(0).setOffset(offset);
    }

    /**
     * Indicates the default offset applied to the graphic's main label. This offset may be overridden by the graphic
     * attributes.
     *
     * @return Offset to apply to the main label.
     */
    protected Offset getDefaultLabelOffset()
    {
        return Label.DEFAULT_OFFSET;
    }

    /**
     * Get the override attributes that are active for this frame.
     *
     * @return Override attributes. Values set in this bundle override defaults specified by the symbol set.
     */
    protected TacticalGraphicAttributes getActiveOverrideAttributes()
    {
        return this.activeOverrides;
    }

    /**
     * Get the active shape attributes for this frame. The active attributes are created by applying application
     * specified overrides to the default attributes specified by the symbol set.
     *
     * @return Active shape attributes.
     */
    protected ShapeAttributes getActiveShapeAttributes()
    {
        return this.activeShapeAttributes;
    }

    /**
     * Get the Material that should be used to draw labels. If no override material has been specified, the graphic's
     * outline Material is used for the labels.
     *
     * @return The Material that should be used when drawing labels. May change each frame.
     */
    protected Material getLabelMaterial()
    {
        Material material = this.activeOverrides.getTextModifierMaterial();
        if (material != null)
            return material;
        else
            return this.activeShapeAttributes.getOutlineMaterial();
    }

    protected void applyDefaultAttributes(ShapeAttributes attributes)
    {
        Material material = this.getDefaultOutlineMaterial();
        attributes.setOutlineMaterial(material);

        String status = this.getStatus();
        if (!SymbologyConstants.STATUS_PRESENT.equals(status))
        {
            attributes.setOutlineStippleFactor(6);
            attributes.setOutlineStipplePattern((short) 0xAAAA);
        }

        // Most 2525 area graphic do not have a fill.
        attributes.setDrawInterior(false);
    }

    protected Material getDefaultOutlineMaterial()
    {
        String identity = this.getStandardIdentity();
        if (SymbologyConstants.STANDARD_IDENTITY_HOSTILE.equals(identity))
            return MATERIAL_HOSTILE;
        else
            return MATERIAL_FRIEND;
    }

    protected void applyOverrideAttributes(TacticalGraphicAttributes graphicAttributes, ShapeAttributes shapeAttributes)
    {
        Material material = graphicAttributes.getInteriorMaterial();
        if (material != null)
        {
            shapeAttributes.setInteriorMaterial(material);
        }

        material = graphicAttributes.getOutlineMaterial();
        if (material != null)
        {
            shapeAttributes.setOutlineMaterial(material);
        }

        Double value = graphicAttributes.getInteriorOpacity();
        if (value != null)
        {
            shapeAttributes.setInteriorOpacity(value);
        }

        value = graphicAttributes.getOutlineOpacity();
        if (value != null)
        {
            shapeAttributes.setOutlineOpacity(value);
        }

        value = graphicAttributes.getOutlineWidth();
        if (value != null)
        {
            shapeAttributes.setOutlineWidth(value);
        }
    }

    //////////////////////////////////////////////////
    // Convenience methods to access common modifiers
    //////////////////////////////////////////////////

    /**
     * Get the date range from the graphic's modifiers. This method looks at the value of the
     * <code>AVKey.DATE_TIME</code> modifier, and returns the results as a two element array. If either value is an
     * instance of {@link Date}, the date will be formatted to a String using the active date format. If the value of
     * the modifier is an <code>Iterable</code>, then this method returns the first two values of the iteration. If the
     * value of the modifier is a single object, this method returns an array containing that object and
     * <code>null</code>.
     *
     * @return A two element array containing the altitude modifiers. One or both elements may be null.
     */
    protected Object[] getDateRange()
    {
        Object date1 = null;
        Object date2 = null;

        Object o = this.getModifier(AVKey.DATE_TIME);
        if (o instanceof Iterable)
        {
            Iterator iterator = ((Iterable) o).iterator();
            if (iterator.hasNext())
            {
                o = iterator.next();
                if (o instanceof Date)
                    date1 = this.formatDate((Date) o);
                else
                    date1 = o;
            }

            if (iterator.hasNext())
            {
                o = iterator.next();
                if (o instanceof Date)
                    date2 = this.formatDate((Date) o);
                else
                    date2 = o;
            }
        }
        else
        {
            date1 = o;
        }

        return new Object[] {date1, date2};
    }

    /**
     * Get the altitude range from the graphic's modifiers. This method looks at the value of the
     * <code>AVKey.ALTITUDE</code> modifier, and returns the results as a two element array. If the value of the
     * modifier is an <code>Iterable</code>, then this method returns the first two values of the iteration. If the
     * value of the modifier is a single object, this method returns an array containing that object and
     * <code>null</code>.
     *
     * @return A two element array containing the altitude modifiers. One or both elements may be null.
     */
    protected Object[] getAltitudeRange()
    {
        Object alt1 = null;
        Object alt2 = null;

        Object o = this.getModifier(AVKey.ALTITUDE);
        if (o instanceof Iterable)
        {
            Iterator iterator = ((Iterable) o).iterator();
            if (iterator.hasNext())
            {
                alt1 = iterator.next();
            }

            if (iterator.hasNext())
            {
                alt2 = iterator.next();
            }
        }
        else
        {
            alt1 = o;
        }

        return new Object[] {alt1, alt2};
    }
}
