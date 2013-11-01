/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
///TODO: FIX NETWORKING
package destruct;

import Entity.EnemyEntity;
import Entity.Entity;
import Entity.HillEntity;
import Entity.PumpkinEntity;
import java.awt.Polygon;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author John
 */
public final class Server implements Runnable{
        public static final byte //MESSAGE IDs
            LOGIN = 0,
            MOVE  = 1,
            LEAVE = 2,
            MAP   = 3,
            ENTITY = 4,
            AI     = 5,
            MESSAGE = 6,
            WORLDEXPAND = 7,
            ENTIREWORLD = 8,
            DIG         = 9,
            FILL        = 10,
            ID          = 11,
            CHUNK       = 12,
            AIRBENDING  = 13,
            EARTHBENDING= 14,
            WATERBENDING= 15,
            FIREBENDING = 16,
            FREEZE = 17,
            PUDDLE = 18,
            DEATH = 19,
            CHARGE = 20,
            LIGHTNING = 21,
            DESTROY = 22,
            STEAM = 23,
            HURT = 24,
            SANDINATE = 25,
            LOGOUT = 26,
            SCORE = 27,
            DARKNESS = 28,
            DRAIN = 29;
        public static final int
                TEAMDEATHMATCH = 1,
                FREEFORALL = -1,
                KINGOFTHEHILL = -2,
                THEHIDDEN = 2;
        public static final byte //MESSAGE IDs
            UDPMOVE = 0;
        public static int MYID = 0;
        protected int nextVote = 0;
        public static int getID()
        {
            return MYID++;
        }
        public static final String MESSAGEIDs[] = new String[]
        {
            "Player Login","Player Move","Player Left","Map Request","Entity Request",
            "AI Request","Chat Message","World Expansion","Entire World","Dig","Fill","ID",
            "Chunk","Airbending","Earthbending","Boats Float In The Ocean!","Firebending","Freeze","BOATS_IN_THE_KINDOM",
            "Death","Charge","Lightning","Entity Kill","Entity Steam","Hurt","SANDINATE","QUITTER","POINTS","DARKNESS","DRAIN"
        };
         int port = 25565;
         public ArrayList<Integer> team1 = new ArrayList<>(), team2 = new ArrayList<>(); 
    public ArrayList<PlayerOnline> playerList = new ArrayList<>();
    static ServerSocket SocialSecurity;
    Thread playerAcceptor = new Thread(this);
    int spawnX, spawnY, pID = 1, eID = 0;
    public World earth = new World();
    boolean goLeft = false, goRight = false;
    public int score[] = new int[256];
    public static int gameMode = 1;
    static Random random = new Random();
        public static int choose(int... e)
    {
        return (e[random.nextInt(e.length)]);
    }
    public void kill()
    {
        try {
            this.playerAcceptor.interrupt();
            worldHandle.interrupt();
            expander.interrupt();
            SocialSecurity.close();
            try
            {
                if (!playerList.isEmpty())
                {
                    for (PlayerOnline p:playerList)
                        p.killMe();
                }
            }
            catch (Exception e)
            {
                
            }
            gameRunning = false;
            playerList.clear();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    Thread worldHandle, udplistener;
    public Server()
    {
        try {
            SocialSecurity = new ServerSocket(25565);
            playerAcceptor.start();
            spawnX = earth.x;
            spawnY = earth.y;
            earth.lol=this;
            loadMap(1);
            startExpander();
//            udplistener = new Thread(new UDPThread());
  //          udplistener.start();
            worldHandle = new Thread(){
                @Override
                public void run()
                { 
                    long lastTime = System.currentTimeMillis();
                    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                    while (gameRunning)
                    {
                        try {
                            long l = System.currentTimeMillis()-lastTime;
                            while (l<25)
                            {
                                Thread.sleep(1);
                                l = System.currentTimeMillis()-lastTime;
                            }
                        } catch (InterruptedException ex) {
                        }
                        lastTime = System.currentTimeMillis();
                        
                        if (earth==null)
                        {
                            continue;
                        }
                        earth.onUpdate();
                        if (nextVote*3>playerList.size()*2)
                        {
                            expander.interrupt();//muwahahaa
                        }
                        if (gameMode==THEHIDDEN)
                        {
                            if (playerList.size()>1&&playerList.get(0).score>=(playerList.size()-1))
                            {
                                expander.interrupt();
                            }
                        }
                       // fixStuff();
                   //     System.out.println(lastTime);
                        try {
                            earth.ground.handleWater();
                        } catch (Exception ex) {
                            //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
            //            earth.ground.ShowData();
                        
                        
                    }
                }
            };
            worldHandle.start();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  
    public static Server main2(String[] args)
            {
                Server me = new Server();
                me.IP = args[0];
                return me;
//                me.port = Integer.getInteger(WhyMustJavaBeSoArguementative[0]);
              //  me.earth.ground.ClearCircle(100, 100, 400);
            }
    public static void main(String[] args)
            {
                Server me = new Server();
                me.IP = args[0];
//                me.port = Integer.getInteger(WhyMustJavaBeSoArguementative[0]);
              //  me.earth.ground.ClearCircle(100, 100, 400);
            }
    int runThrough = 0;
    public String IP = "";
    boolean gameRunning = true, accepting = true;
    ConnectToDatabase INSTANCE = ConnectToDatabase.INSTANCE();
    @Override
    public void run() {
        while (gameRunning&&accepting)
        {
            
            try {
            Socket toAdd = (SocialSecurity.accept());
            toAdd.setKeepAlive(false);
            playerList.add(new PlayerOnline(spawnX,spawnY,toAdd,pID,this));
            INSTANCE.updateCount(IP, playerList.size());
            if ((gameMode==THEHIDDEN))
            {
                if ((team1.isEmpty()))
                {
                    team1.add(pID);
                }
                else
                {
                    team2.add(pID);
                }
            }
            else
            {
                if ((team1.size()>team2.size()))
                {
                    team2.add(pID);
                }
                else
                {
                    team1.add(pID);
                }
            }
            pID++;
          //  Thread.sleep(10);
           // newPlayer();
            
            
                Thread.sleep(100);
            } catch (IOException | InterruptedException ex) {
               //return;
            }
        }
    }
    public void newPlayer(int id, String user)
    {
        for (PlayerOnline p:playerList)
        {
             if (id!=p.ID)
             {
                p.writeNewPlayer(id, user);
             }  
        }

    }
    public void sendMessage(byte id, ByteBuffer mes)
    {
        for (PlayerOnline p:playerList)
        {
            try {
                // p.out.write(id);
                //Server.writeByteBuffer(mes, p.out);
                p.out.addMesssage(mes, id);
            } catch (Exception ex) {
                //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void movePlayer(int id, int x, int y, int m, int v, int la, int ra, short st, short hp)
    {
        for (PlayerOnline p:playerList)
        {
            if (p.ID!=id)
            {
                    p.writeMovePlayer(id, x, y, m, v, la, ra, st, hp);
            }
        }
    }
         public void moveRelative(int x, int y)
    {
        for (PlayerOnline p:playerList)
        {
                    p.writeMovePlayer(p.ID, p.x+x, p.y+y, p.move, p.vspeed,(int)p.leftArmAngle,(int)p.rightArmAngle,p.status,p.HP);
        }
    }
         int call = 0;
         public void fixStuff()
         {
             call++;
             for (Entity p:earth.entityList)
                        {
                            if (p instanceof EnemyEntity)
                            {
                                EnemyEntity e = (EnemyEntity)p;
                                e.onServerUpdate(this);
                                if (call>100)
                                {
                                    call = 0;
                                    ByteBuffer aIMessage = ByteBuffer.allocate(1000);
                                    aIMessage.putInt(e.X).putInt(e.Y).putInt(e.HP).putInt(e.move).putInt(e.yspeed).putInt(e.target).putInt(e.id);
                                    sendMessage(AI, aIMessage);
                                }
                            }
                        }
             
         }
         Thread expander;
public void startExpander()
{
             expander = new Thread(new Runnable(){
            @Override
            public void run()
            {
                while (gameRunning)
                {
                loadMap(mapRotation);
                    try {
                        Thread.sleep(5*60*1000);
                    } catch (InterruptedException ex) {
                       //Up! Time for a new map!
                    }
                }
            }
        }       );
            expander.start();
}
int mapRotation = 0;
int maxMap = 1;


public static void writeByteBuffer(ByteBuffer toSend, OutputStream output) throws IOException
{
    
    
    byte yay[] = new byte[toSend.position()];//toSend.slice().array();
     //S   System.a
        System.arraycopy(toSend.array(), 0, yay, 0, toSend.position());
        //output.write(toSend.position());
        byte[] size = ByteBuffer.allocate(4).putInt(yay.length).array();
     //   System.err.println(yay.length);
        output.write(size);
    output.write(yay);
    output.flush();
}
public static ByteBuffer putString(ByteBuffer yes, String y)
{
    yes.putInt(y.length());
    yes.put(y.getBytes());
    return yes;
}
public static String getString(ByteBuffer yes)
{
    byte[] dst = new byte[yes.getInt()];
    yes.get(dst);
    return new String(dst);
}
public static ByteBuffer readByteBuffer(InputStream in) throws IOException
{
            ByteBuffer size = ByteBuffer.allocate(4);
                        
                        size.put((byte)in.read());
                        size.put((byte)in.read());
                        size.put((byte)in.read());
                        size.put((byte)in.read());
                        size.rewind();
                        int howMuchData = size.getInt();
                      //  System.err.println(howMuchData);
                        int total = 0;
                        byte[] buf = new byte[howMuchData];
                        while (total<howMuchData)
                        {
                            total += in.read(buf, total, howMuchData-total);
                        }
                        return ByteBuffer.wrap(buf);
}
public String getKiller(int i)
{
    for (Player p:playerList)
    {
        if (p.ID==i)
        {
            return p.username;
        }
    }
    return "NULL";
}
public Player getPlayer(int i)
{
    for (Player p:playerList)
    {
        if (p.ID==i)
        {
            return p;
        }
    }
    return null;
}

String dir = System.getenv("APPDATA")+"\\Bending\\";
    public void loadMap(int i)
    {  
        nextVote = 0;
        if (++mapRotation>maxMap)
                    {
                        mapRotation = 0;
                    }
                    
                String yes;
                if (gameMode==TEAMDEATHMATCH)
                {
                    int score1 = 0, score2 = 0;
                    String teamname1 = "", teamname2 = "";
                    for (PlayerOnline p:playerList)
                    {
                        if (team1.contains(p.ID))
                        {
                            score1 += p.score;
                            teamname1 += p.username+" ";
                        }
                        else
                        {
                            score2 += p.score;
                            teamname2 += p.username+" ";
                        }
                        p.score = 0;
                    }
                    if (score1>score2)
                    {
                        yes = "This match's winners were: "+teamname1;
                        sendMessage(MESSAGE, Server.putString(ByteBuffer.allocate(yes.length()*4+4).putInt(0xFF0000),yes));
                    }
                    else if (score1<score2)
                    {
                        yes = "This match's winners were: "+teamname2;
                        sendMessage(MESSAGE, Server.putString(ByteBuffer.allocate(yes.length()*4+4).putInt(0xFF0000),yes));
                    }
                    else
                    {
                         yes = "The matched ended in... a tie!";
                        sendMessage(MESSAGE, Server.putString(ByteBuffer.allocate(yes.length()*4+4).putInt(0xFF0000),yes));
                    }
                }
                else
                {
                    int max = -1;
                    int winner = 0;
                    for (Entity e:earth.entityList)
                    {
                        e.setAlive(false);
                    }
                    earth.entityList.clear();   
                    for (PlayerOnline P:playerList)
                    {
                        if (P.score>max)
                        {
                            max = P.score;
                            winner = P.ID;
                        }
                    }
                    yes = getKiller(winner)+" won the round!";
                    sendMessage(MESSAGE, Server.putString(ByteBuffer.allocate(yes.length()*4+4).putInt(0xFF0000),yes));
                }
                gameMode = choose(FREEFORALL,TEAMDEATHMATCH,KINGOFTHEHILL,THEHIDDEN);
                Collections.shuffle(playerList);
                team1.clear();
                team2.clear();
                for (PlayerOnline P:playerList)
                {
                    P.score = 0;
                }
                int id;
                if (gameMode==THEHIDDEN)
                {
                    if (playerList.size()>0)
                    {
                        team1.add(playerList.get(0).ID);
                        System.out.println(playerList.get(0).username+" joined red");
                        for (int index = 1; index < playerList.size(); index++)
                        {
                            id = playerList.get(index).ID;
                            team2.add(id);
                            System.out.println(playerList.get(index).username+" joined red");
                        }
                    }
                }
                else
                {
                    for (int index = 0; index < playerList.size(); index++)
                    {
                        id = playerList.get(index).ID;
                        if (index%2==0)
                        {
                            team1.add(id);
                            System.out.println(playerList.get(index).username+" joined red");
                        }
                        else
                        {
                            team2.add(id);
                            System.out.println(playerList.get(index).username+" joined blue");
                        }
                    }
                }
                
                String gm = "";
                switch (gameMode)
                {
                    case TEAMDEATHMATCH:
                        gm = "Team Death Match";
                    break;
                    case FREEFORALL:
                        gm = "Free for All";
                    break;
                    case KINGOFTHEHILL:
                        gm = "King of the Hill";
                    break;
                    case THEHIDDEN:
                        gm = "The Hidden";
                    break;
                }
                yes = "The next game type will be "+gm+".";

                sendMessage(MESSAGE, Server.putString(ByteBuffer.allocate(yes.length()*4+4).putInt(0x00FF3C),yes));
        Arrays.fill(score, 0);
        earth.ground.ClearCircleStrong(150, 150, 9000);
        File mapsFolder = new File(dir+"maps");
        File[] mapFiles = mapsFolder.listFiles();
        if (mapFiles.length == 0)
        {
            switch (i)
            {
                default:
                case 0:
                    earth.ground.FillCircle(150, 900, 300);
                    earth.ground.FillCircle(450, 900, 300);
                    earth.ground.FillCircle(750, 900, 300);
                break;
                case 1:
                    earth.ground.FillRectW(0, 800, 100, 900,World.LAVA);
                    for (int x = 0; x <= 900; x +=100)
                    {
                        earth.ground.FillCircleW(x, 800, 100, World.STONE);
                    }
                break;
                case 2:
                    Polygon P = new Polygon();
                    P.addPoint(0,	900);
                    P.addPoint(25,	800);
                    P.addPoint(100,	600);
                    P.addPoint(300,	750);
                    P.addPoint(400,	700);
                    P.addPoint(500,	500);
                    P.addPoint(600,	720);
                    P.addPoint(700,	820);
                    P.addPoint(800,	860);
                    P.addPoint(900,	900);
                    earth.ground.FillRectW(0, 700, 200, 900,World.WATER);
                    earth.ground.FillPolygon(P, World.ICE);
               break;
            }
        }
        else
        {
            Random r = new Random();
            boolean invalid = true;
            while (invalid)
            {
                int toPick = r.nextInt(mapFiles.length);
                File chosen = mapFiles[toPick];
                if (chosen!=null&&chosen.isFile()&&chosen.canRead()&&chosen.getName().endsWith(".ter"))
                {
                    try
                    {
                        loadMap(chosen);
                        invalid = false;
                    }
                    catch (Exception e)
                    {
                        
                    }
                }
            }
        }
        //earth.entityList.add(new EnemyEntity(300,300,0,0,500).setID(Server.getID()));
        earth.entityList.add(new PumpkinEntity(earth.wIdTh/2,earth.hEigHt/2).floor(earth).setID(Server.getID()));
        switch (gameMode)
                {
                    default:
                    break;
                    case KINGOFTHEHILL:
                        earth.entityList.add(new HillEntity(earth.wIdTh/2,earth.hEigHt/2,0,0).setID(Server.getID()));
                    break;
                }
        for (PlayerOnline p:playerList)
        {
            p.writeWorld();
        }
    }
    public void loadMap(File f) throws Exception
{
    DataInputStream fos;
              System.out.println("LOADING MAP: "+f.getName());
              fos = new DataInputStream(new FileInputStream(f));
              int xxxxxx = fos.readInt(), yyyyy = fos.readInt();
              earth.ground.cellData = new byte[xxxxxx][yyyyy];
              earth.ground.w = xxxxxx;
              earth.ground.h = yyyyy;
              earth.wIdTh = xxxxxx;
              earth.hEigHt = yyyyy;
              for (int i = 0; i <  earth.ground.cellData.length; i++)
              {
                  byte in[] = new byte[yyyyy];
                  fos.read(in);
                   earth.ground.cellData[i] = in;
              }
              fos.close();

}
    private class UDPThread implements Runnable
    {
        @Override
        public void run()
        {
            try {
                DatagramSocket waffles;
                waffles = new DatagramSocket(12345);
                waffles.setBroadcast(true);
            while (true)
            {
              //  System.out.println("YES!");
                UDPPacket e = UDPPacket.read(waffles);
               // System.out.println("GOT SOMETHING!" + e.ID);
                switch (e.ID)
                {
                    default:
                    //    System.out.println("HRGRG");
                         break;
                    case UDPMOVE:
                        //System.out.println("MOVE IT");
                        int   player = e.getInt();
                        short      x = e.geShortt();
                        short      y = e.geShortt();
                        short   move = e.geShortt();
                        short vspeed = e.geShortt();
                        short    lan = e.geShortt();
                        short    ran = e.geShortt();
                       // System.out.println("REC FROM "+e.packet.getPort()+" ; "+playerList.size());
                        
                        for (int i = 0; i<playerList.size(); i++)
                        { 
                            PlayerOnline po = playerList.get(i);
                            //waffles.disconnect();
                           // System.err.println(" CHECKING "+i);
                            if (po.ID==player)
                            {
                                po.x = x;
                                po.y = y;
                                po.rightArmAngle = ran;
                                po.leftArmAngle = lan;
                                po.move = move;
                                po.vspeed = vspeed;
                            }
                            else
                            {
                         //       System.out.println("writing to "+po.UDPPORT);
                                UDPPacket toSend = UDPPacket.allocate(4*6, Server.UDPMOVE);
                                toSend.putInt(player);
                                toSend.putShort((short)x);
                                toSend.putShort((short)y);
                                toSend.putShort((short)move);
                                toSend.putShort((short)vspeed);
                                toSend.putShort((short)lan);
                                toSend.putShort((short)ran);
                                toSend.write(waffles, po.playerSocket.getInetAddress(), po.UDPPORT);
                                
                            }
                            }
                    break;
                        
                }
                 }
            } catch (Exception ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
