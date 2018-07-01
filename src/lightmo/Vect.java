/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package lightmo;

import org.newdawn.slick.geom.Point;
import org.newdawn.slick.util.FastTrig;

/** Vector class for Lightmo. Provides general vector support, but limited 
 *  to what Lightmo actually needs to use.
 *
 * @author Nash
 */
public class Vect {
    /** The x coordinate of the vector. */
    protected float x;
    /** The y coordinate of the vector. */
    protected float y;
    
    /** Creates a new Vect with the given coordinates.
     * 
     * @param x The x value.
     * @param y The y value.
     */
    public Vect(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    /** Creates a new Vect using the given Point.
     * 
     * @param p A Point containing the desired vector coordinates. 
     */
    public Vect(Point p) {
        this(p.getX(), p.getY());
    }
    
    /** Returns the x value of this Vect.
     * 
     * @return  The x value. 
     */
    public float getX() {
        return x;
    }
    
    /** Returns the y value of this Vect.
     * 
     * @return  The y value. 
     */
    public float getY() {
        return y;
    }
    
    /** Calculates and returns the length of this Vect.
     * 
     * @return The length of this vector.
     */
    public float calcLength() {
        return (float)Math.sqrt(x*x+y*y);
    }
    
    /** Calculates and returns the length of this Vector squared. This avoids 
     *  the square-root step of calcLength(), so provides better performance 
     *  when it is possible to compare len^2 instead of len.
     * 
     * @return The length of this vector squared. 
     */
    public float calcLengthSquared() {
        return (x*x+y*y);
    }
    
    /** Returns a copy of this Vector multiplied by the given scalar.
     * 
     * @param sc    The scalar.
     * @return      A scaled copy of this Vector.
     */
    public Vect scale(float sc) {
        return new Vect(x*sc, y*sc);
    }
    
    /** Returns a copy of this Vect with its length set to the given value, 
     *  but facing the same direction. This is the same as 
     * Vect.normalize().scale(len).
     * 
     * @param len   The length to set.
     * @return      A copy, scaled to the given length.
     */
    public Vect setLength(float len) {
        return normalize().scale(len);
    }
    
    /** Returns a normalized copy of this Vect, with a length of 1.
     * 
     * @return The unit vector copy.
     */
    public Vect normalize() {
        return scale(1f/calcLength());
    }
    
    /** Returns the sum of this Vect and a given Vect.
     * 
     * @param b The Vect to add to this one.
     * @return  The sum, as a new Vect.
     */
    public Vect add(Vect b) {
        return new Vect(this.x+b.x, this.y+b.y);
    }
    
    /** Returns the difference between a given Vect b and this Vect.
     *  That is, subtract(B) = B-A, where A is this Vect and B is the argument. 
     *  Additionally, B-A is a vector that goes from A to B.
     * 
     * @param b The Vect to subtract from.
     * @return  The difference, as a new Vect.
     */
    public Vect subtract(Vect b) {
        return new Vect(this.x-b.x, this.y-b.y);
    }
    
    /** Returns the dot product of this and Vect b.
     * 
     * @param b Vect b.
     * @return  The dot product.
     */
    public float dot(Vect b) {
        return this.x*b.x + this.y*b.y;
    }
    
    /** Converts this Vector to a Point.
     * 
     * @return  The Point. 
     */
    public Point toPoint() {
        return new Point(x, y);
    }
    
    /** Returns whether this Vect is facing Vect b.
     * 
     * @param b Vect b.
     * @return  Whether Vect(B) is facing 
     */
    public boolean isFacing(Vect b) {
        return dot(b)>0;
        //return dot(b)<(Math.PI/2);
    }
    
    /** Returns whether Vect b is clockwise from Vect. ("Turn clockwise to reach 
     *  Vect b?")
     * 
     * @param b Vect b.
     * @return  Whether to turn clockwise to face b.
     */
    public boolean isCWTo(Vect b) {
        return getCCWPerp().isFacing(b);
    }
    
    /** Returns whether Vect b is counter-clockwise from Vect. ("Turn CCW to 
     *  reach Vect b?")
     * 
     * @param b Vect b.
     * @return  Whether to turn counter-clockwise to face b.
     */
    public boolean isCCWTo(Vect b) {
        return getCWPerp().isFacing(b);
    }
    
    /** Returns a Vect that is perpendicular to and counter-clockwise from 
     *  this one.
     * 
     * @return  The CCW perpendicular, as a new Vect. 
     */
    public Vect getCCWPerp() {
        return new Vect(y, -x);
    }
    
    /** Returns a Vect that is perpendicular to and clockwise from this one.
     * 
     * @return The CW perpendicular, as a new Vect.
     */
    public Vect getCWPerp() {
        return new Vect(-y, x);
    }
    
    /** Turns this Vect slightly toward Vect b.
     * 
     * @param b Vect b.
     * @param turnAmt   The amount to turn, in radians.
     * @return  A new Vect that has been turned slightly.
     */
    public Vect turnToward(Vect b, float turnAmt) {
        Vect aUni = normalize();
        Vect bUni = b.normalize();
        float dot = aUni.dot(bUni);
        float angle = (float)Math.acos(dot);
        
        if(Math.abs(angle)<turnAmt) {
            return bUni.scale(calcLength());
        } else if(isCWTo(b)) {
            return rotate(-turnAmt);
        } else {
            return rotate(turnAmt);
        }
    }
    
    /** Rotates this Vect by the given angle.
     * 
     * @param rads  The angle of rotation, in radians.
     * @return      The result of rotation, as a new Vect.
     */
    public Vect rotate(float rads) {
        float cos = (float)FastTrig.cos(rads);
        float sin = (float)FastTrig.sin(rads);
        return new Vect(x*cos-y*sin, x*sin+y*cos);
    }
    
    /** Returns a new Vect that travels from Point a to Point b. This is 
     *  equivalent to the difference between Vect(b) and Vect(a).
     * 
     * @param a Vect a.
     * @param b Vect b.
     * @return  A new Vect traveling from a to b.
     */
    public static Vect toVect(Point a, Point b) {
        return new Vect(b).subtract(new Vect(a));
    }
}
