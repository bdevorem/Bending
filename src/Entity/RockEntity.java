package Entity;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import destruct.Player;
import destruct.Server;
import destruct.World;
import java.awt.Color;
import java.awt.Graphics;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 *
 * @author John
 */
public class RockEntity extends Entity{
    public int maker = 0;
    public int radius = 16;
    public int gravity = 1;
    int a1, a2, a3;
    int s1, s2, s3;
    public RockEntity(int x, int y, int hspeed, int vspeed, int ma)
    {
        X = x;
        Y = y;
        xspeed = hspeed;
        yspeed = vspeed;
        maker = ma;
        Random r = new Random();
            a1 = 60+r.nextInt(120);
            a2 = 60+r.nextInt(120);
            a3 = 60+r.nextInt(120);
            s1 = r.nextInt(360);
            s2 = r.nextInt(360);
            s3 = r.nextInt(360);
    }
    @Override
    public void onDraw(Graphics G, int viewX, int viewY) {
        if (X>viewX&&X<viewX+300&&Y>viewY&&Y<viewY+300)
        {
            G.setColor(Color.DARK_GRAY);
            G.fillArc((X-radius/2)-viewX, (Y-radius/2)-viewY, radius, radius, 0, 360);
            
            G.setColor(Color.GRAY);
            G.fillArc((X-2)-viewX, (Y-2)-viewY, 4, 4, s1, a1);
            G.fillArc((X-4)-viewX, (Y-4)-viewY, 8, 8, s2, a2);
            G.fillArc((X-6)-viewX, (Y-6)-viewY, 12, 12, s3, a3);
        }
    }

    @Override
    public void onUpdate(World apples) {
       if (!apples.inBounds(X, Y)||apples.checkCollision(X, Y))
       {
           
           alive = false;
           //apples.explode(X, Y, 32, 8, 16);
       }
       for (Player p:apples.playerList)
       {
           if (apples.pointDis(X, Y, p.x, p.y)<radius&&maker!=p.ID)
           {
               alive = false;
           }
       }
       yspeed+=gravity;
       
       /*if (yspeed<12)
       {
           yspeed++;
       }*/
    }
    long time = System.currentTimeMillis();
    @Override
public void onServerUpdate(Server lol)
{
    
   // System.out.println(System.currentTimeMillis()-time);
    time = System.currentTimeMillis();
    if (!lol.earth.inBounds(X+xspeed, Y+yspeed)||lol.earth.checkCollision(X+xspeed, Y+yspeed))
       {
           radius*=3;
           lol.earth.ground.FillCircleW(X+xspeed, Y+yspeed, 96, World.STONE);
           lol.sendMessage(Server.FILL, ByteBuffer.allocate(40).putInt(X+xspeed).putInt(Y+yspeed).putInt(96).put(World.STONE));
       }
}

    @Override
    public void cerealize(ByteBuffer out) {
       // throw new UnsupportedOperationException("Not supported yet.");
    }
    

}
