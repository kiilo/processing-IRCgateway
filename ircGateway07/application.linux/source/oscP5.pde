import oscP5.*;
import netP5.*;

OscP5 oscP5;
String OscDestHost;
int OscDestPort;
int OscRecvPort;
String OscRecvAddress;
String OscSendAddress;
NetAddress OscDestination;

void SetupOscP5() {
  oscP5 = new OscP5(this, OscRecvPort);
  OscDestination = new NetAddress(OscDestHost, OscDestPort);
  // following line just works for ONE symbol sen from puredata
  //oscP5.plug(this,"OscToIrc", OscRecvAddress);
}

// OscToIrc
void OscToIrc(String OscToIrcMessage) {
  ActivityLogAddLine("OSC RECV "+OscRecvAddress+" "+OscToIrcMessage);
  IrcBot.sendMessage(IrcChannel, OscToIrcMessage);
  ActivityLogAddLine("IRC SEND "+OscToIrcMessage);
}


void oscEvent(OscMessage aOscMessage) {
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

