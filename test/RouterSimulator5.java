import java.util.Random;

/* ******************************************************************
Project 4: implementing distributed, asynchronous, distance vector routing.

THIS IS THE MAIN ROUTINE.  The TRACE and LINKCHANGES variables can be
changed to modify the simulator's output and behavior.  

This code is a Java translation of code provided by Kurose and Ross.

Output GUIs added by Ch. Schuba 2007.

**********************************************************************/

public class RouterSimulator {

  public static final int NUM_NODES = 5;
  public static final int INFINITY = 999;

  public static final boolean LINKCHANGES = false;

  public int TRACE = 1;             /* for debugging */

  private GuiTextArea myGUI = null;

  private RouterNode[] nodes;

  private Random generator;

  private int[][] connectcosts = new int[NUM_NODES][NUM_NODES];


/*****************************************************************
***************** NETWORK EMULATION CODE STARTS BELOW ***********
The code below emulates the layer 2 and below network environment:
  - emulates the tranmission and delivery (with no loss and no
    corruption) between two physically connected nodes
  - calls the initializations routines rtinit0, etc., once before
    beginning emulation

THERE IS NOT REASON THAT ANY STUDENT SHOULD HAVE TO READ OR UNDERSTAND
THE CODE BELOW.  YOU SHOLD NOT TOUCH, OR REFERENCE (in your code) ANY
OF THE DATA STRUCTURES BELOW.  If you're interested in how I designed
the emulator, you're welcome to look at the code - but again, you
should not have to, and you defeinitely should not have to modify
******************************************************************/

  Event evlist = null;   /* the event list */

  /* possible events: */
  final static int FROM_LAYER2 = 2;
  final static int LINK_CHANGE   =  10;

  double clocktime = 0.000;


  public static void main(String[] argv){
    RouterSimulator sim = new RouterSimulator();

    sim.runSimulation();
  }

  RouterSimulator()                         /* initialize the simulator */
  {
    double sum, avg;
    Event evptr;  
    long seed = 1234;

    String prop;
    
    myGUI = new GuiTextArea("  Output window for Router Simulator  ");
    
    if((prop=System.getProperty("Trace"))!=null)
      TRACE = Integer.parseInt(prop);
  

    if((prop=System.getProperty("Seed"))!=null)
      seed = Long.parseLong(prop);

    generator = new Random(seed);

    clocktime=0.0;                /* initialize time to 0.0 */

    /* set initial costs */
    // remember that in java everything defaults to 0
    connectcosts[0][1]=1;  
    connectcosts[0][2]=3;
    connectcosts[0][3]=7;
    connectcosts[0][4]=1;
    connectcosts[1][0]=1;
    connectcosts[1][2]=1;
    connectcosts[1][3]=INFINITY;
    connectcosts[1][4]=1;
    connectcosts[2][0]=3;  
    connectcosts[2][1]=1;
    connectcosts[2][3]=2;
    connectcosts[2][4]=4;
    connectcosts[3][0]=7;
    connectcosts[3][1]=INFINITY;
    connectcosts[3][2]=2;
    connectcosts[3][4]=INFINITY;
    connectcosts[4][0]=1;
    connectcosts[4][1]=1;
    connectcosts[4][2]=4;
    connectcosts[4][3]=INFINITY;
    
    nodes = new RouterNode[NUM_NODES];
    for(int i=0;i<NUM_NODES;i++)
      nodes[i] = new RouterNode(i,this,connectcosts[i]);

    /* initialize future link changes */
    if (LINKCHANGES)   {
      evptr = new Event();
      evptr.evtime =  10000.0;
      evptr.evtype =  LINK_CHANGE;
      evptr.eventity =  0;
      evptr.rtpktptr =  null;
      evptr.dest = 3;
      evptr.cost = 1;
      insertevent(evptr);

      evptr = new Event();
      evptr.evtype =  LINK_CHANGE;
      evptr.evtime =  20000.0;
      evptr.eventity =  0;
      evptr.rtpktptr =  null;
      evptr.dest = 1;
      evptr.cost = 6;
      insertevent(evptr);    
    }
  
  }



  void runSimulation(){
    Event eventptr;
   
    while (true) {
     
      eventptr = evlist;            /* get next event to simulate */
      if (eventptr==null)
	break;
      evlist = evlist.next;        /* remove this event from event list */
      if (evlist!=null)
           evlist.prev=null;
      if (TRACE>1) {
	myGUI.println("MAIN: rcv event, t="+
			   eventptr.evtime+ " at "+
			   eventptr.eventity);
          if (eventptr.evtype == FROM_LAYER2 ) {
	    myGUI.print(" src:"+eventptr.rtpktptr.sourceid);
            myGUI.print(", dest:"+eventptr.rtpktptr.destid);
            myGUI.println(", contents: "+ 
              eventptr.rtpktptr.mincost[0]+" "+ eventptr.rtpktptr.mincost[1]+
			       " "+
			       eventptr.rtpktptr.mincost[2]+" "+ 
			       eventptr.rtpktptr.mincost[3]);
            }
          }
      clocktime = eventptr.evtime;    /* update time to next event time */
      if (eventptr.evtype == FROM_LAYER2 ) {
	if(eventptr.eventity >=0 && eventptr.eventity < NUM_NODES)
	  nodes[eventptr.eventity].recvUpdate(eventptr.rtpktptr);
	else { myGUI.println("Panic: unknown event entity\n"); System.exit(0); }
      }
      else if (eventptr.evtype == LINK_CHANGE ) {
	// change link costs here if implemented
	nodes[eventptr.eventity].updateLinkCost(eventptr.dest,
						eventptr.cost);
	nodes[eventptr.dest].updateLinkCost(eventptr.eventity,
					    eventptr.cost);
      }
      else
	{ myGUI.println("Panic: unknown event type\n"); System.exit(0); }
      
      if(TRACE > 2)
	for(int i=0;i<NUM_NODES;i++)
	  nodes[i].printDistanceTable();

    }
    
    
    myGUI.println("\nSimulator terminated at t="+clocktime+
		       ", no packets in medium\n");
  }

  public double getClocktime() {
    return clocktime;
  }

  /********************* EVENT HANDLINE ROUTINES *******/
  /*  The next set of routines handle the event list   */
  /*****************************************************/
  

  void insertevent(Event p){
    Event q,qold;

    if (TRACE>3) {
      myGUI.println("            INSERTEVENT: time is "+clocktime);
      myGUI.println("            INSERTEVENT: future time will be "+
			 p.evtime); 
    }
    q = evlist;     /* q points to header of list in which p struct inserted */
    if (q==null) {   /* list is empty */
      evlist=p;
      p.next=null;
      p.prev=null;
    }
    else {
      for (qold = q; q !=null && p.evtime > q.evtime; q=q.next)
	qold=q; 
      if (q==null) {   /* end of list */
	qold.next = p;
	p.prev = qold;
	p.next = null;
      }
      else if (q==evlist) { /* front of list */
	p.next=evlist;
	p.prev=null;
	p.next.prev=p;
	evlist = p;
      }
      else {     /* middle of list */
	p.next=q;
	p.prev=q.prev;
	q.prev.next=p;
	q.prev=p;
      }
    }
  }

  void printevlist()
  {
    Event q;
    myGUI.println("--------------\nEvent List Follows:");
    for(q = evlist; q!=null; q=q.next) {
      myGUI.println("Event time: "+q.evtime+
			 ", type: "+q.evtype+
			 " entity: "+q.eventity);
    }
    myGUI.println("--------------");
  }


  /************************** TOLAYER2 ***************/
  void toLayer2(RouterPacket packet){
    RouterPacket  mypktptr;
    Event evptr, q;
    double lastime;
    int i;


    /* be nice: check if source and destination id's are reasonable */
    if (packet.sourceid<0 || packet.sourceid > NUM_NODES-1) {
      myGUI.println("WARNING: illegal source id in your packet, ignoring packet!");
      return;
    }
    if (packet.destid<0 || packet.destid > NUM_NODES-1) {
      myGUI.println("WARNING: illegal dest id in your packet, ignoring packet!");
      return;
    }
    if (packet.sourceid == packet.destid)  {
      myGUI.println("WARNING: source and destination id's the same, ignoring packet!");
      return;
    }
    if (connectcosts[packet.sourceid][packet.destid] == INFINITY)  {
      myGUI.println("WARNING: source and destination not connected, ignoring packet!");
      return;
    }

    /* make a copy of the packet student just gave me since may */
    /* be modified after we return back */ 
    mypktptr = (RouterPacket)packet.clone();

    if (TRACE>2)  {
      myGUI.print("    TOLAYER2: source: "+mypktptr.sourceid+
		       " dest: "+ mypktptr.destid+
		       "             costs:");
      
      for (i=0; i<NUM_NODES; i++)
        myGUI.print(mypktptr.mincost[i]+" ");
      myGUI.println();
    }

    /* create future event for arrival of packet at the other side */
    evptr = new Event();
    evptr.evtype =  FROM_LAYER2;   /* packet will pop out from layer3 */
    evptr.eventity = packet.destid; /* event occurs at other entity */
    evptr.rtpktptr = mypktptr;       /* save ptr to my copy of packet */

    /* finally, compute the arrival time of packet at the other end.
       medium can not reorder, so make sure packet arrives between 1
       and 10 time units after the latest arrival time of packets
       currently in the medium on their way to the destination */
    lastime = clocktime;
    for (q=evlist; q!=null ; q = q.next) 
      if ( (q.evtype==FROM_LAYER2  && q.eventity==evptr.eventity) ) 
	lastime = q.evtime;
    evptr.evtime =  lastime + 9*generator.nextDouble() + 1;

 
    if (TRACE>2)  
      myGUI.println("    TOLAYER2: scheduling arrival on other side");
    insertevent(evptr);
  } 
}

class  Event {
  double evtime;           /* event time */
  int evtype;             /* event type code */
  int eventity;           /* entity where event occurs */
  RouterPacket rtpktptr; /* ptr to packet (if any) assoc w/ this event */
  int dest, cost;        /* for link cost change */
  Event prev;
  Event next;
}
