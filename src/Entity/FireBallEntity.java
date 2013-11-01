package Entity;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import destruct.Server;
import destruct.World;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author John
 */
public class FireBallEntity extends Entity{
    public int maker = 0;
    public int radius = 16;
    public int gravity = 1;
    public FireBallEntity(int x, int y, int hspeed, int vspeed, int ma)
    {
        X = x;
        Y = y;
        xspeed = hspeed;
        yspeed = vspeed;
        maker = ma;
    }
    @Override
    public void onDraw(Graphics G, int viewX, int viewY) {
        if (X>viewX&&X<viewX+300&&Y>viewY&&Y<viewY+300)
        {
            Graphics2D g = (Graphics2D)G;
            Composite c = g.getComposite();
            // g.setComposite(new Additive());
            g.setColor(new Color(255,r.nextInt(255),0,r.nextInt(255)));
                g.fillArc((X-6)-viewX, (Y-6)-viewY, 12,12, 0, 360); 
            for (int i = 0; i < 4; i++)
            {
                int e1 = 6-r.nextInt(12), e2 = 6-r.nextInt(12);
                g.setColor(new Color(255,r.nextInt(255),0,r.nextInt(255)));
                g.fillArc((X+e1)-viewX, (Y+e2)-viewY, e1, e2, 0, 360); 
            }
            g.setComposite(c);
        }
    }
    int next = 0;
    @Override
    public void onUpdate(World apples) {
       if (!apples.inBounds(X, Y)||apples.checkCollision(X, Y))
       {
           
           alive = false;
           //apples.explode(X, Y, 32, 8, 16);
       }
       if (next++>2)
       {
            next = 0;
            yspeed+=gravity;
       }
       /*if (yspeed<12)
       {
           yspeed++;
       }*/
    }
    @Override
public void onServerUpdate(Server lol)
{
        if (lol.earth.checkCollision(X, Y))
        {
           radius*=4;
           lol.earth.ground.ClearCircle(X, Y, radius);
           lol.sendMessage(Server.DIG, ByteBuffer.allocate(40).putInt(X).putInt(Y).putInt(radius));
           alive = false;
        }
        if (lol.earth.inBounds(X, Y)&&collided(lol.earth))//lol.earth.ground.cellData[X][Y]==World.WATER
        {
            alive = false;
            lol.sendMessage(Server.STEAM, ByteBuffer.allocate(40).putInt(X).putInt(Y).putInt(this.MYID));
        }
}
    @Override
    public void cerealize(ByteBuffer out) {
        try {
            Server.putString(out,  this.getClass().getName());
            out.putInt(X);
            out.putInt(Y);
            out.putInt(xspeed);
            out.putInt(yspeed);
            out.putInt(maker);
        } catch (Exception ex) {
            Logger.getLogger(ExplosionEntity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private boolean collided(World w)
    {
        double xx = X;
        double yy = Y;
        for (int i = 0; i < 16; i++)
        {
            if (w.inBounds((int)xx,(int)yy)&&w.ground.cellData[(int)xx][(int)yy]==World.WATER)
            {
                X = (int)xx;
                Y = (int)yy;
                return true;
            }
            xx+=xspeed/16d;
            yy+=yspeed/16d;
        }
        return false;
    }
    public static void reconstruct(ByteBuffer in, World world) {
        try {
            world.entityList.add(new FireBallEntity(in.getInt(),in.getInt(),in.getInt(),in.getInt(),in.getInt()));
        } catch (Exception ex) {
            Logger.getLogger(FireBallEntity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
