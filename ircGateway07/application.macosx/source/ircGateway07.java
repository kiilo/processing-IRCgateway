import processing.core.*; 
import processing.xml.*; 

import org.jibble.pircbot.*; 
import java.io.*; 
import controlP5.*; 
import oscP5.*; 
import netP5.*; 

import controlP5.*; 
import org.jibble.pircbot.*; 
import oscP5.*; 
import netP5.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class ircGateway07 extends PApplet {

PImage backgr;

public void setup() {
  size(640, 320, P2D);
  smooth();
  noStroke();
  frameRate(20);
  backgr = loadImage("backgr.png");
  //background(255,204,0);
  background(backgr);
  SetupP5Properties();
  SetupControlP5();
  SetupOscP5();
  SetupIRCbot();  
}

public void draw() {
  //PImage b = loadImage("backgr.png");
  background(backgr);
  controlP5.draw();
  delay(25);
}


//import org.jibble.pircbot.*;



String IrcServer; 
String IrcChannel;
String IrcBotName;

MyBot IrcBot;

public void SetupIRCbot() {

  IrcBot = new MyBot();
  // Now start our bot up.
  //println("BOT created");
  //println(IrcBot);

  // Enable debugging output.
  IrcBot.setVerbose(true);

  // Connect to the IRC server.
  try { 
    IrcBot.connect(IrcServer);
  }
  catch (IOException e) {
    println("IO mooooh! "+e.getMessage());
  }
  catch (IrcException e) {
    println("IRC mooooh! "+e.getMessage());
  }

  println("connected to IRC");

  // Join the #<IrcChannel>
  IrcBot.joinChannel(IrcChannel);
  IrcBot.sendMessage(IrcChannel, "Hello, " + IrcBotName + " is UP and RUNNING");
  ActivityLogAddLine("Hello, " + IrcBotName + " is UP and RUNNING");
}


public class MyBot extends PircBot {

  public MyBot() {
    this.setName(IrcBotName);
  }

  // this function is called when the bot receives a message
  public void onMessage(String channel, String sender,
  String login, String hostname, String message) {
    ActivityLogAddLine("IRC RECV "+channel+" "+sender+" "+message);
    IrcToOsc(sender, message);
    // if you ask in irc for time
    if (message.equalsIgnoreCase("time")) {
      String time = new java.util.Date().toString();
      sendMessage(channel, sender + ": The time is now " + time);
      ActivityLogAddLine("IRC SEND "+sender + ": The time is now " + time);
    }
    // if you ask in irc for status TODO UPtime, incomming, outgoing since startup
    if (message.equalsIgnoreCase("status")) {
      String time = new java.util.Date().toString();
      sendMessage(channel, sender + " UP and RUNNING " + time);
      ActivityLogAddLine("IRC SEND "+sender + " UP and RUNNING " + time);
    }
    // if you say "ping" the bot answers "pong"
    if (message.equalsIgnoreCase("ping")) {
      sendMessage(channel, sender + " pong");
      ActivityLogAddLine("IRC SEND "+sender + " pong");
    }
  }

  // if there is a disconnect message in IRC, try to reconnect and join the channel again (needs some minutes)
  public void onDisconnect() {
    ActivityLogAddLine("disconnected ");
    delay(10000);
    ActivityLogAddLine("try reconnect ");
    try {
      this.reconnect();
    }
    catch (IOException e) {
      println("IO mooooh! "+e.getMessage());
    }
    catch (IrcException e) {
      println("IRC mooooh! "+e.getMessage());
    }
    this.joinChannel(IrcChannel);
    ActivityLogAddLine("reconnected ");
  }
}

public void IrcToOsc(String IrcSender, String IrcMessage) {
  IrcMessage = trim(IrcMessage);
  OscMessage SendOscMessage = new OscMessage(OscSendAddress);
  SendOscMessage.add(IrcSender);
  String[] OscMessageArguments = split(IrcMessage, ' ');
  for(int i=0; i < OscMessageArguments.length; i++) {
    try
    {
      float f = Float.valueOf(OscMessageArguments[i].trim()).floatValue();
      SendOscMessage.add(f);
    }
    catch (NumberFormatException nfe)
    {
      SendOscMessage.add(OscMessageArguments[i]);
    }

    //SendOscMessage.add(OscMessageArguments[i]);
  }
  oscP5.send(SendOscMessage, OscDestination);
  ActivityLogAddLine("OSC SEND "+OscSendAddress+" "+IrcSender+" "+IrcMessage);
}







P5Properties props;

// this function reads our properties file
public void SetupP5Properties() {
  try {
    props = new P5Properties();
    // load a configuration from a file inside the data folder
    props.load(openStream("../config.pde"));
    OscRecvPort = props.getIntProperty("osc.receiving.port",8000);
    OscDestHost = props.getStringProperty("osc.destination.host","localhost");
    OscDestPort = props.getIntProperty("osc.destination.port",9000);
    OscRecvAddress = props.getStringProperty("osc.address.recv","/toIrc");
    OscSendAddress = props.getStringProperty("osc.address.send","/fromIrc");
    IrcServer = props.getStringProperty("irc.server","irc.freenode.net");
    IrcChannel = props.getStringProperty("irc.channel","#playaround");
    IrcBotName = props.getStringProperty("irc.botname","PlayAroundBot");
  }
  catch(IOException e) {
    println("couldn't read config file..."+e.getMessage());
  }
}

/**
 * simple convenience wrapper object for the standard
 * Properties class to return pre-typed numerals
 */
class P5Properties extends Properties {
 
  public boolean getBooleanProperty(String id, boolean defState) {
    return PApplet.parseBoolean(getProperty(id,""+defState));
  }
 
  public int getIntProperty(String id, int defVal) {
    return PApplet.parseInt(getProperty(id,""+defVal)); 
  }
 
  public float getFloatProperty(String id, float defVal) {
    return PApplet.parseFloat(getProperty(id,""+defVal)); 
  }
  
  public String getStringProperty(String id, String defVal) {
    return trim(getProperty(id,""+defVal)); 
  }
}
/* configuration in comments to USE processing IDE for editing DONT REMOVE 
#
# OSC sttings
# receiving PORT is the the PORT where this gateway is listening on - for <osc.address.recv> (see below)
osc.receiving.port = 8000
# destination HOST:PORT is where the irc messages are send to - OSC adress pattern used is in <osc.address.send> (see below)
osc.destinaton.host = 10.187.123.255
osc.destinaton.port = 9000
# OSC address pattern
osc.address.recv = /toIrc
osc.address.send = /fromIrc
#
# IRC settings
irc.server = irc.freenode.net
irc.channel = #playaround
irc.botname = kiilo-CrashTest
# DONT REMOVE THIS COMMENT BELOW - processing doesnt have a properties editor, why? - its a hack!
*/



ControlP5 controlP5;

//int myColorBackground = color(0,0,0);
//int sliderValue = 100;

Textarea ActivityLogTextarea;


public void SetupControlP5() {
  controlP5 = new ControlP5(this);
  //controlP5.setControlFont(new ControlFont(createFont("Verdana",28,true), 14));

  ActivityLogTextarea = controlP5.addTextarea( "ActivityLog","",16,32,592,240);
  ActivityLogTextarea.setLineHeight(12);
  ActivityLogTextarea.setColor(0x1f1f1f);
  ActivityLogTextarea.enableColorBackground();
  ActivityLogTextarea.setColorBackground(0x0708090a);  
  ActivityLogTextarea.showScrollbar();
  ActivityLogAddLine("setup ...");

  /* OK this tabbed view willl be done later
  // STATUS tab
  controlP5.addButton("button",10,100,80,80,20);
  RecvPort = controlP5.addTextlabel("RecvPort","recv PORT",16,32);
  RecvPort.setColorValue(0x1f1f1f);
  SendDest = controlP5.addTextlabel("SendDest","send DEST",16,48);
  SendDest.setColorValue(0x1f1f1f);
  controlP5.controller("button").moveTo("status");
  controlP5.controller("RecvPort").moveTo("status");
  controlP5.controller("SendDest").moveTo("status");
  
  //SETTINGS TAB
  SetRecvPort = controlP5.addTextfield("set port",16,32, 64,16);
  SetSendDest = controlP5.addTextfield("set destination:port",16,64,256,16);
  SetRecvPort.setAutoClear(false);
  SetSendDest.setAutoClear(false);
  controlP5.controller("set port").moveTo("settings");
  controlP5.controller("set destination:port").moveTo("settings");
  

  controlP5.tab("settings").setColorForeground(0xffff0000);
  controlP5.tab("settings").setColorBackground(0xff330000);
  
  controlP5.trigger();
  
  // in case you want to receive a controlEvent when
  // a  tab is clicked, use activeEvent(true)
  controlP5.tab("settings").activateEvent(true);
  controlP5.tab("settings").setId(0);
  
  controlP5.tab("status").activateEvent(false);
  //controlP5.tab("default").setLabel("status");
  controlP5.tab("status").setId(1);
  */
  controlP5.draw(); //draw once because SetupIrc needs some seconds
}

/*
void slider(int theColor) {
  myColorBackground = color(theColor);
  println("a slider event. setting background to "+theColor);
}

void sliderValue(int theColor) {
  myColorBackground = color(theColor);
  println("a sliderValue event. setting background to "+theColor);
}
*/

public void controlEvent(ControlEvent theControlEvent) {
  if(theControlEvent.isController()) {
    println("controller : "+theControlEvent.controller().label());
  } else if (theControlEvent.isTab()) {
    println("tab : "+theControlEvent.tab().id()+" / "+theControlEvent.tab().name());
    
  }
}

public void ActivityLogAddLine(String aTextLine) {
  if (ActivityLogTextarea.text().length() > 5000) {
    int IndexOfFirstLine = ActivityLogTextarea.text().indexOf("\n");
    ActivityLogTextarea.setText(ActivityLogTextarea.text().substring(IndexOfFirstLine+1));
  }
  ActivityLogTextarea.setText(ActivityLogTextarea.text()+String.valueOf(day())+"."+String.valueOf(month())+"."+String.valueOf(year())+" "+String.valueOf(hour())+":"+String.valueOf(minute())+":"+String.valueOf(second())+"."+String.valueOf(millis())+" "+aTextLine+"\n");
}




OscP5 oscP5;
String OscDestHost;
int OscDestPort;
int OscRecvPort;
String OscRecvAddress;
String OscSendAddress;
NetAddress OscDestination;

public void SetupOscP5() {
  oscP5 = new OscP5(this, OscRecvPort);
  OscDestination = new NetAddress(OscDestHost, OscDestPort);
  // following line just works for ONE symbol sen from puredata
  //oscP5.plug(this,"OscToIrc", OscRecvAddress);
}

// OscToIrc
public void OscToIrc(String OscToIrcMessage) {
  ActivityLogAddLine("OSC RECV "+OscRecvAddress+" "+OscToIrcMessage);
  IrcBot.sendMessage(IrcChannel, OscToIrcMessage);
  ActivityLogAddLine("IRC SEND "+OscToIrcMessage);
}


public void oscEvent(OscMessage aOscMessage) {
  // following lines build a string from the OSC message
  if (aOscMessage.checkAddrPattern(OscRecvAddress) == true ) {   // react only to our osc.address.recv setting
    String OscToIrcMessage = "";
    for(int i=0; i < aOscMessage.typetag().length(); i++) {
      switch ( aOscMessage.typetag().charAt(i) ) { 
      case 's':
        OscToIrcMessage += aOscMessage.get(i).stringValue()+" ";
        break;
      case 'i':
        OscToIrcMessage += str(aOscMessage.get(i).intValue())+" ";
        break;
      case 'f':
        OscToIrcMessage += str(aOscMessage.get(i).floatValue())+" ";
        break;
      default:
        println("ERROR unsupported osc typtag");
        ActivityLogAddLine("ERROR unsupported osc typtag");
        break;
      }
    }
    OscToIrcMessage = trim(OscToIrcMessage);
    OscToIrc(OscToIrcMessage);
  }
}


  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#DFDFDF", "ircGateway07" });
  }
}
