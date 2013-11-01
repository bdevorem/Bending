package Entity;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import destruct.World;
import java.awt.Color;
import java.awt.Graphics;
import java.nio.ByteBuffer;

/**
 *
 * @author John
 */
public class GroundinatorEntity extends Entity{
    public GroundinatorEntity(int x, int y, int hspeed, int vspeed)
    {
        X = x;
        Y = y;
        xspeed = hspeed;
        yspeed = vspeed;
    }
    @Override
    public void onDraw(Graphics G, int viewX, int viewY) {
        if (X>viewX&&X<viewX+300&&Y>viewY&&Y<viewY+300)
        {
            G.setColor(Color.GREEN);
            G.fillArc((X-1)-viewX, (Y-1)-viewY, 2, 2, 0, 360);
        }
    }

    @Override
    public void onUpdate(World apples) {
       if (!apples.inBounds(X, Y)||apples.checkCollision(X, Y))
       {
           alive = false;
           apples.unexplode(X, Y, 32, 8, 16);
       }
       if (yspeed<12)
       {
           yspeed++;
       }
    }

    @Override
    public void cerealize(ByteBuffer out) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
