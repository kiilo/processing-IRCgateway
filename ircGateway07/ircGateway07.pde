PImage backgr;

void setup() {
  size(640, 320, P2D);
  smooth();
  noStroke();
  frameRate(10);
  backgr = loadImage("backgr.png");
  //background(255,204,0);
  background(backgr);
  SetupP5Properties();
  SetupControlP5();
  SetupOscP5();
  SetupIRCbot();  
}

void draw() {
  //PImage b = loadImage("backgr.png");
  background(backgr);
  controlP5.draw();
  delay(25);
}


