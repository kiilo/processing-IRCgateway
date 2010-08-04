//import org.jibble.pircbot.*;
import org.jibble.pircbot.*;
import java.io.*;

String IrcServer; 
String IrcChannel;
String IrcBotName;

MyBot IrcBot;

void SetupIRCbot() {

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

void IrcToOsc(String IrcSender, String IrcMessage) {
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







