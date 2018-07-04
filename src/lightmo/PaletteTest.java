/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lightmo;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

/** Testing class for visualizing palettes built by Lightmo.buildPalette(s).
 *
 * @author Nash
 */
public class PaletteTest extends BasicGame {
    /** The size of the palettes. Multiples of four are nice. */
    public static final int SIZE = 16;
    
    /** Creates a new PaletteTest.
     */
    public PaletteTest() {
        super("PaletteTest");
    }
    
    /** Main method. Creates and runs PaletteTest in a 640x480 window.
     * 
     * @param args  Command line arguments, unused.
     * @throws SlickException If Slick encounters a problem.
     */
    public static void main(String[] args) throws SlickException {
        PaletteTest pt = new PaletteTest();
        AppGameContainer agc = new AppGameContainer(pt, 640, 480, false);
        agc.start();
    }
    
    /** Builds a starting palette.
     * @param gc The GameContainer
     */
    @Override
    public void init(GameContainer gc) {
        palette = Lightmo.buildPalette(SIZE);
        destPal = Lightmo.buildPalette(SIZE);
    }
    
    /** Update cycle. Does nothing.
     * 
     * @param gc    The GameContainer.
     * @param i     Delta time, in milliseconds.
     */
    @Override
    public void update(GameContainer gc, int i) {
        paletteShift += 50f * (Lightmo.PALETTE_MOD_SPEED * i / 1000f);
        if(paletteShift > 2) {
            palette = destPal;
            destPal = Lightmo.buildPalette(SIZE);
            paletteShift = 0;
        }
    }
    
    /** Draws the colors to the screen.
     * 
     * @param gc    The GameContainer.
     * @param g     The current Graphics context.
     */
    @Override
    public void render(GameContainer gc, Graphics g) {
        int cols = 4;
        int wid = gc.getWidth()/cols;
        int hei = gc.getHeight()/(palette.length/cols);
        for(int i = 0; i < palette.length; i++) {
            int row = i/cols;
            float shift = paletteShift;
            if(shift > 1) {
                shift = 1;
            }
            g.setColor(Lightmo.blend(palette[i], destPal[i], shift));
            g.fillRect(wid*(i%cols), hei*row, wid, hei);
            g.setColor(palette[i]);
            //g.fillRect(wid*(i%cols), hei*row, wid/4, hei/4);
            g.setColor(destPal[i]);
            //g.fillRect(wid*(i%cols)+(wid/4), hei*row, wid/4, hei/4);
            g.setColor(Color.white);
            g.drawString("PCT: " + ((int)(paletteShift*10000)/100.0f), 15, 40);
        }
    }
    
    /** KeyPress handler. Closes the program on ESC, generates a new palette on 
     *  the right arrow key.
     * 
     * @param key   The key code.
     * @param c     The key character.
     */
    @Override
    public void keyPressed(int key, char c) {
        if(key == Input.KEY_ESCAPE) {
            System.exit(0);
        }
    }
    
    Color[] palette;
    Color[] destPal;
    float paletteShift = 0;
}
