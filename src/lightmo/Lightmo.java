/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package lightmo;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.fills.GradientFill;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Rectangle;

/**
 *  This simply fills your screen with a shifting gradient of colors. It is
 *  designed to be used in a dimly lit room, to provide dynamic atmospheric
 *  lighting.
 * 
 * @author Nash
 */
public class Lightmo extends BasicGame {
    /** The speed of the orb gradient. Pixels per second. */
    public static final int ORB_SPEED = 40; // pps
    /** The turning speed of the orb gradient. Radians per second. */
    public static final float ORB_TURN_SPEED = (float)(Math.PI);
    /** Auto-reset period for the orb's destination goal. Milliseconds. */
    public static final int ORB_DEST_RESET_TIME = 22000;
    /** How close the orb is to its destination to trigger a reset. Pixels.*/
    public static final int DEST_BUFFER = 20;
    /** The speed at which the linear gradient points move. 
     *  Pixels per second. */
    public static final int GRAD_SPEED = 6;
    /** The ratio of the orb's movement field to the screen size. */
    public static final float FIELD_SCALE = 1.5f;
    /** The rate at which gradient colors shift toward their goals. 
     *  Percent per second. */
    public static final float COLOR_SHIFT_SPEED = 0.0125f;
    /** Standard size for color palettes. */
    public static final int PALETTE_SIZE = 16;
    /** The rate at which the palette modulates toward its next goal. 
     *  Percent per second. */
    public static final float PALETTE_MOD_SPEED = 0.0165f;

    /** 
     *  Runs Lightmo. Command line arguments are as follows:<br>
     *  <ul><li>-s Runs the program as a screen saver.</li>
     *  <li>-p Screen saver preview mode (there is no preview)</li>
     *  <li>-c Screen saver configuration mode (no config either)</li>
     *  <li>-f Full Screen mode.</li>
     *  <li>-w Windowed mode In this case, the next two arguments should 
     *  be width and height.</li>
     *  <li>-m Mouse-escape mode, where mouse movements will close the program.</li></ul>
     * 
     * @param args The command line arguments.
     * @throws org.newdawn.slick.SlickException If Slick encounters a problem.
     */
    public static void main(String[] args) throws SlickException {
        // full screen
        boolean fs = true;
        // mouse motion/click exit
        boolean mEsc = false;
        // done parsing arguments
        boolean done = false;
        // quit without running Lightmo
        boolean quit = false;
        int winWidth = 640;
        int winHeight = 480;
        
        // parse the arguments array
        for(int i = 0; !done && i < args.length; i++) {
            String a = args[i];
            if(a.equals("-s")) { // screen saver mode
                fs = true;
                mEsc = true;
                done = true;
            } else if(a.equals("-p") || a.equals("-c")) { // preview/config mode
                quit = true;
            } else if(a.equals("-f")) { // full screen
                fs = true;
            } else if(a.equals("-w")) { // windowed
                fs = false;
                try {
                    winWidth = Integer.parseInt(args[i+1]);
                    winHeight = Integer.parseInt(args[i+2]);
                } catch(ArrayIndexOutOfBoundsException e) {
                    System.out.println("Missing screen dimensions. "
                            + "Using defaults. Next time use format "
                            + "'-w width height'");
                } catch(NumberFormatException e) {
                    System.out.println("Invalid dimension argument. "
                            + "Using defaults.");
                }
            } else if(a.equals("-m")) { // mouse escape
                mEsc = true;
            }
        }
        
        if(fs) {
            GraphicsDevice gd = GraphicsEnvironment
                    .getLocalGraphicsEnvironment().getDefaultScreenDevice();
            winWidth = gd.getDisplayMode().getWidth();
            winHeight = gd.getDisplayMode().getHeight();
        }

        if(!quit) {
            Lightmo game = new Lightmo();
            game.setMouseEscape(mEsc);

            AppGameContainer container = new AppGameContainer(game, winWidth, winHeight, fs);
            container.setMouseGrabbed(fs);
            container.setShowFPS(false);
            container.setUpdateOnlyWhenVisible(false);
            container.setAlwaysRender(true);
            container.setMinimumLogicUpdateInterval(16);
            container.setVSync(true);
            container.start();
        }
    }
    
    /** Creates a new Lightmo! No arguments, no hassle. */
    public Lightmo() {
        super("Lightmo");
        mouseEsc = false;
    }

    /** Initializes Lightmo to the given GameContainer.
     * 
     * @param gc    The GameContainer handling this instance.
     * @throws SlickException Any number of Slick2D related exceptions.
     */
    @Override
    public void init(GameContainer gc) throws SlickException {
        this.gc = gc;
        mouseStart = new Vect(gc.getInput().getMouseX(), 
                gc.getInput().getMouseY());
        
        palette = buildPalette(PALETTE_SIZE);
        destPal = buildPalette(PALETTE_SIZE);
        destPaletteTimer = 0;
        
        frame = new Rectangle(0, 0, gc.getWidth(), gc.getHeight());
        field = new Rectangle((gc.getWidth() / 2f) - 
                (FIELD_SCALE / 2f * gc.getWidth()), (gc.getHeight() / 2f) - 
                (FIELD_SCALE / 2f * gc.getHeight()), 
                FIELD_SCALE * gc.getWidth(), FIELD_SCALE * gc.getHeight());
        orb = new Point(
                rand(field.getWidth()) + (field.getX()),
                rand(field.getHeight()) + (field.getY()));
        orbRad = (int)((gc.getWidth()+gc.getHeight())/3.0);
        orbImg = buildOrb();
        frameGrad = new GradientFill(rand(frame.getWidth()), 
                rand(frame.getHeight()), rollColor(), 
                rand(frame.getWidth()), rand(frame.getHeight()), rollColor());
        gradADest = rollGradDest();
        gradBDest = rollGradDest();
        orbColorA = rollColor();
        orbColorB = rollColor();
        
        gradAColorA = frameGrad.getStartColor();
        gradAColorB = rollColor();
        
        gradBColorA = frameGrad.getEndColor();
        gradBColorB = rollColor();

        orbDest = rollOrbDest();
        orbDestTimer = 0;
        orbVect = Vect.toVect(new Point(orb.getX(), orb.getY()), orbDest);
        orbVect = orbVect.normalize();
    }

    /** Primary update cycle, updating all parameters based on the given delta 
     *  time.
     * 
     * @param gc    The GameContainer handling this Lightmo.
     * @param i     The delta time, in milliseconds.
     * @throws SlickException If Slick encounters a problem.
     */
    @Override
    public void update(GameContainer gc, int i) throws SlickException {
        // Handles mouse movement exit, if mouse escape is turned on.
        if(mouseEsc) {
            if(mouseStart.subtract(new Vect(gc.getInput().getAbsoluteMouseX(), 
                    gc.getInput().getAbsoluteMouseY())).calcLengthSquared() > 100) {
                gc.exit();
            }
        }
        
        // update orb position
        Vect loc = new Vect(orb.getX(), orb.getY());
        Vect move = orbVect.scale(ORB_SPEED / 1000f * i);
        loc = loc.add(move);
        orb.setLocation(loc.getX(), loc.getY());
        orbDestTimer += i;
        
        float turnAmt = ORB_TURN_SPEED / 1000f * i;
        orbVect = orbVect.turnToward(Vect.toVect(orb, orbDest), turnAmt);
        
        // reset orb dest
        if(orbDestTimer > ORB_DEST_RESET_TIME || new Vect(orbDest).subtract(loc).calcLengthSquared() < DEST_BUFFER) {
            orbDestTimer = 0;
            orbDest = rollOrbDest();
        }
        
        // update grad start position
        loc = new Vect(frameGrad.getStart().x, frameGrad.getStart().y);
        move = new Vect(gradADest).subtract(loc);
        move = move.setLength(GRAD_SPEED / 1000f * i);
        loc = loc.add(move);
        frameGrad.setStart(loc.getX(), loc.getY());
        if(Vect.toVect(new Point(frameGrad.getStart().x, frameGrad.getStart().y), 
                new Point(gradADest.getX(), gradADest.getY())).calcLengthSquared() 
                < DEST_BUFFER) {
            gradADest = rollGradDest();
        }
        
        // update grad end position
        loc = new Vect(frameGrad.getEnd().x, frameGrad.getEnd().y);
        move = new Vect(gradBDest).subtract(loc);
        move = move.setLength(GRAD_SPEED / 1000f * i);
        loc = loc.add(move);
        frameGrad.setEnd(loc.getX(), loc.getY());
        if(Vect.toVect(new Point(frameGrad.getEnd().x, frameGrad.getEnd().y), 
                new Point(gradBDest.getX(), gradBDest.getY())).calcLengthSquared() 
                < DEST_BUFFER) {
            gradBDest = rollGradDest();
        }

        // shift colors
        orbShift += COLOR_SHIFT_SPEED / 1000f * i;
        gradAShift += COLOR_SHIFT_SPEED / 1000f * i;
        gradBShift += COLOR_SHIFT_SPEED / 1000f * i;
        
        if(orbShift > 1) {
            orbColorA = orbColorB;
            orbColorB = rollColor();
            orbShift %= 1;
        }
        
        if(gradAShift > 1) {
            gradAColorA = gradAColorB;
            gradAColorB = rollColor();
            gradAShift %= 1;
        }
        
        if(gradBShift > 1) {
            gradBColorA = gradBColorB;
            gradBColorB = rollColor();
            gradBShift %= 1;
        }
        
        orbColor = blend(orbColorA, orbColorB, orbShift);
        frameGrad.setStartColor(blend(gradAColorA, gradAColorB, gradAShift));
        frameGrad.setEndColor(blend(gradBColorA, gradBColorB, gradBShift));
        
        // update the palette
        paletteShift += PALETTE_MOD_SPEED * i / 1000f;
        
        if(paletteShift > 2) {
            palette = destPal;
            destPal = buildPalette(PALETTE_SIZE);
            paletteShift = 0;
        }
    }

    /** Draws Lightmo to the screen.
     * 
     * @param gc        The GameContainer handling this Lightmo.
     * @param grphcs    The current Graphics context.
     * @throws SlickException If Slick encounters a problem.
     */
    @Override
    public void render(GameContainer gc, Graphics grphcs) throws SlickException {
        grphcs.fill(frame, frameGrad);
        grphcs.drawImage(orbImg, orb.getX(), orb.getY(), orbColor);
    }
    
    /** Pulls a random float between 0-1 from Math.random().
     * 
     * @return A pseudorandom float between [0, 1). 
     */
    public static float rand() {
        return (float)Math.random();
    }
    
    /** Pulls a random float, scaled by the given modifier.
     * 
     * @param mod The amount by which to scale the random value.
     * @return A pseudorandom value between [0, mod)
     */
    public static float rand(float mod) {
        return rand() * mod;
    }
    
    /** Builds the orb image, which is a simple, white radial gradient.
     * 
     * @return  The orb image. 
     */
    public Image buildOrb() {
        ImageBuffer buf = new ImageBuffer(orbRad*2+1, orbRad*2+1);
        double r2= orbRad*orbRad;
        for(int y = 0; y < buf.getHeight(); y++) {
            for(int x = 0; x < buf.getWidth(); x++) {
                double pct = Math.pow(x-orbRad,2)+Math.pow(y-orbRad, 2);
                if(pct < r2) {
                    pct /= r2;

                    buf.setRGBA(x, y, 255, 255, 255, (int)(255*(1-pct)));
                }
            }
        }
        
        return buf.getImage();
    }
    
    /** Selects a new destination for the orb, within the field.
     * 
     * @return  The new destination. 
     */
    public Point rollOrbDest() {
        return new Point(rand(field.getWidth()) + field.getX() - 
                (orbImg.getWidth() / 2f), rand(field.getHeight()) + 
                field.getY() - (orbImg.getHeight() / 2f));
    }
    
    /** Selects a new destination for one of the gradient anchor points.
     * 
     * @return  The new destination. 
     */
    public Point rollGradDest() {
        return new Point(rand(frame.getWidth()), rand(frame.getHeight()));
    }
    
    /** Selects a random color from the current palette.
     * 
     * @return  The Color. 
     */
    public Color rollColor() {
        float shift = paletteShift;
        if(shift > 1) {
            shift = 1;
        }
        int inx = (int)(Math.random()*PALETTE_SIZE);
        return blend(palette[inx], destPal[inx], shift);
    }
    
    /** Sets whether to exit the program on mouse motion.
     * 
     * @param esc   True to enable mouse escape, false otherwise. 
     */
    public void setMouseEscape(boolean esc) {
        mouseEsc = esc;
    }
    
    /** Exits the program if the escape key is pressed.
     * 
     * @param key   The key code of the pressed key.
     * @param c     The character of the pressed key.
     */
    @Override
    public void keyPressed(int key, char c) {
        if(key == Input.KEY_ESCAPE) {
            gc.exit();
        }
    }

    /** Exits the program on mouse click, if mouse escape is turned on.
     * 
     * @param button        The mouse button.
     * @param x             The current x coordinate.
     * @param y             The current y coordinate.
     * @param clickCount    The number of clicks recorded.
     */
    @Override
    public void mouseClicked(int button, int x, int y, int clickCount) {
        if(mouseEsc) {
            gc.exit();
        }
    }
    
    /** Generates a Color palette, based on randomly selected models.
     * 
     * @param size The number of colors to place in the palette.
     * @return  The new palette. 
     */
    public static Color[] buildPalette(int size) {
        // model for hue distributions
        float[][] hDists = {
            {0.0f, 0.3333f, 0.6666f}, // thirds
            {0.0f, 0.2f, 0.65f}, // pinched thirds
            {0.0f, 0.25f, 0.5f, 0.75f}, // fourths
            {0.0f, 0.2f, 0.4f, 0.65f, 0.85f}, // pinched fourths
            {0.0f, 0.2f, 0.4f, 0.6f, 0.8f}, // fifths
            {0.0f, 0.15f, 0.35f, 0.55f, 0.75f} // odd fifths
        };
        
        // model for saturation distributions
        float[][] sDists = {
            {0.0f, 0.0f, 0.0f},
            {0.0f, 0.0f, 0.3f},
            {0.0f, 0.33f, 0.66f}
        };
        
        // model for brightness distributions
        float[][] bDists = {
            {0.0f, 0.333f, 0.666f}, // thirds
            {0.0f, 0.25f, 0.5f, 0.75f}, // fourths
            {0.0f, 0.3f, 0.6f, 0.9f}, // sorta thirds
            {0.0f, 0.55f, 0.8f}, // stretched
            {0.0f, 0.2f, 0.45f}, // scrunched 
        };
        
        float hShift = (float)Math.random();
        float sShift = (float)Math.random();
        float bShift = (float)Math.random();
        float[] hDst = hDists[(int)(Math.random()*hDists.length)];
        float[] sDst = sDists[(int)(Math.random()*sDists.length)];
        float[] bDst = bDists[(int)(Math.random()*bDists.length)];
        
        Color[] plt = new Color[size];
        
        for(int i = 0; i < size; i++) {
            float h = hDst[(int)(Math.random()*hDst.length)]+hShift;
            float s = sDst[(int)(Math.random()*sDst.length)]+sShift;
            float b = bDst[(int)(Math.random()*bDst.length)]+bShift;
            
            s -= (int)s;
            b -= (int)b;
            
            java.awt.Color jCol = new java.awt.Color(
                    java.awt.Color.HSBtoRGB(h, s, b));
            plt[i] = new Color(jCol.getRed(), jCol.getBlue(), jCol.getGreen());
        }
        
        return plt;
    };
    
    
    /** Blends two colors together, according to the given distribution.
     *  The dist value should be a float between 0-1, which indicates the 
     *  percentage of Color b that we will use in the blend. For instance, if 
     *  dist=0.5f, we will have an even blend. If dist=0.75f, the result will
     *  be 75% Color b, 25% Color a.
     * 
     * @param a     The first Color.
     * @param b     The second Color.
     * @param dist  The percent that Color B will have in the blend.
     * @return      The resulting color.
     */
    public static Color blend(Color a, Color b, float dist) {
        float[] aHSB = java.awt.Color.RGBtoHSB(
                a.getRed(), a.getGreen(), a.getBlue(), null);
        float[] bHSB = java.awt.Color.RGBtoHSB(
                b.getRed(), b.getGreen(), b.getBlue(), null);
        float[] c = new float[3];
        for(int i = 0; i < bHSB.length; i++) {
            c[i] = aHSB[i]+(bHSB[i]-aHSB[i])*dist;
        }
        java.awt.Color hsb = java.awt.Color.getHSBColor(c[0],c[1],c[2]);
        return new Color(hsb.getRed(), hsb.getGreen(), hsb.getBlue());
        /*float nDist = 1 - dist;
        return new Color((nDist * a.r) + (dist * b.r), 
                (nDist * a.g) + (dist * b.g), (nDist * a.b) + (dist * b.b));*/
    }
    
    /** The GameContainer running this Lightmo. */
    GameContainer gc;
    /** Whether we will close on mouse motion (as in a screen saver). */
    boolean mouseEsc;
    /** The initial mouse position, for detecting mouse motion 
     *  if mouseEsc=true. */
    Vect mouseStart;
    
    /** The current Color palette. */
    Color[] palette;
    /** The destination Color palette, which palette will gravitate toward. */
    Color[] destPal;
    /** How far along we are in shifting from palette to destPal. */
    float paletteShift = 0;
    /** The timer for updating the destination palette. */
    float destPaletteTimer;
    
    /** The frame for our linear gradient anchor points. 
     * Equivalent to the window dimensions. */
    Rectangle frame;
    /** The field of motion for the radial gradient orb. Slightly larger than 
     *  the window dimensions.
     */
    Rectangle field;
    /** The location of the orb. */
    Point orb;
    /** Image for drawing the orb. */
    Image orbImg;
    /** The current orb color. */
    Color orbColor;
    /** The starting orb color. */
    Color orbColorA;
    /** The destination orb color. */
    Color orbColorB;
    /** The percentage that the orb color has shifted from A to B. */
    float orbShift = 0;
    /** The radius of the orb. */
    int orbRad;
    
    /** The starting color of the linear gradient's A anchor. */
    Color gradAColorA;
    /** The destination color of the linear gradient's A anchor. */
    Color gradAColorB;
    /** The starting color of the linear gradient's B anchor. */
    Color gradBColorA;
    /** The destination color of the linear gradient's B anchor. */
    Color gradBColorB;
    /** The percentage that the linear gradient's A anchor has shifted 
     *  from color A to B. */
    float gradAShift = 0.333f;
    /** The percentage that the linear gradient's B anchor has shifted
     *  from color A to B. */
    float gradBShift = 0.666f;
    
    /** The destination point for the linear gradient's A anchor. */
    Point gradADest;
    /** The destination point for the linear gradient's B anchor. */
    Point gradBDest;
    
    /** The current motion vector of the orb. */
    Vect orbVect;
    /** The destination point for the orb. */
    Point orbDest;
    /** The orb destination update timer. */
    float orbDestTimer;
    
    /** The linear gradient. */
    GradientFill frameGrad;
}
