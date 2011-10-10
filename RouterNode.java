import javax.swing.*;        

public class RouterNode {
    private int myID;
    private GuiTextArea myGUI;
    private RouterSimulator sim;
    private int[] costs = new int[RouterSimulator.NUM_NODES];
    private int[] routes = new int[RouterSimulator.NUM_NODES];
    private int[][] neighbourVectors = new int[RouterSimulator.NUM_NODES][RouterSimulator.NUM_NODES];

    //--------------------------------------------------
    public RouterNode(int ID, RouterSimulator sim, int[] costs) {
        myID = ID;
        this.sim = sim;
        myGUI =new GuiTextArea("  Output window for Router #"+ ID + "  ");

        for(int i = 0; i < RouterSimulator.NUM_NODES; i++) {
          routes[i] = i; 
          for(int j = 0; j < RouterSimulator.NUM_NODES; j++) {
            neighbourVectors[i][j] = RouterSimulator.INFINITY;
          }
        }

        System.arraycopy(costs, 0, this.costs, 0, RouterSimulator.NUM_NODES);

        sendVectors();
    }

    //--------------------------------------------------
    private void sendVectors() {
        for(int i = 0; i < RouterSimulator.NUM_NODES; i++) {
            if(i != myID) {
                if(costs[i] != RouterSimulator.INFINITY) {
                    RouterPacket pkt = new RouterPacket(myID, i, costs);
                    sendUpdate(pkt);
                }
            }
        }
    }
 

    //--------------------------------------------------
    private boolean recalculateCost() {
        System.out.println("Recalculating costs");  

        boolean dirty = false;
        for(int i = 0; i < RouterSimulator.NUM_NODES; i++) {
            for(int dest = 0; dest < RouterSimulator.NUM_NODES; dest++) {
                int cost = neighbourVectors[i][dest] + neighbourVectors[myID][dest];
                if(cost < costs[dest]) {
                    costs[dest] = cost; 
                    routes[dest] = i;
                    dirty = true;
                }
            }
        }

        return dirty; 
    }

    //--------------------------------------------------
    public void recvUpdate(RouterPacket pkt) {
        System.out.println("recvUpdate");
        neighbourVectors[pkt.sourceid] = pkt.mincost;

        boolean dirty = recalculateCost();
        if(dirty) {
            sendVectors();
        }
    }


    //--------------------------------------------------
    private void sendUpdate(RouterPacket pkt) {
        sim.toLayer2(pkt);
        System.out.println("sendUpdate");
    }


    //--------------------------------------------------
    public void printDistanceTable() {
        System.out.println("printDistanceTable");
        myGUI.print("Current state for router " + myID + " at time " + sim.getClocktime() + " \n\n");

        myGUI.print("Distancetable: \n");

        for(int i = 0; i < RouterSimulator.NUM_NODES; i++) {
            myGUI.print("\t" + i); 
        }

        myGUI.print("\n");

        for(int i = 0; i < 12*(1+RouterSimulator.NUM_NODES); i++) {
            myGUI.print("-");
        }
        myGUI.print("\n");

        for(int i = 0; i < RouterSimulator.NUM_NODES; i++) {
            if(i != myID) {  
                myGUI.print("" + i);
                for(int j = 0; j < RouterSimulator.NUM_NODES; j++) {
                    myGUI.print("\t" + neighbourVectors[i][j]);
                }
                myGUI.print("\n");
            }
        }


        myGUI.print("\n");

        myGUI.println("Our distance vector and routes:");

        myGUI.print("dst");
        for(int i = 0; i < RouterSimulator.NUM_NODES; i++) {
            myGUI.print("\t" + i); 
        }

        myGUI.print("\n");

        for(int i = 0; i < 12*(1+RouterSimulator.NUM_NODES); i++) {
            myGUI.print("-");
        }
        myGUI.print("\n");

        myGUI.print("cost");
        for(int i = 0; i < RouterSimulator.NUM_NODES; i++) {
            myGUI.print("\t" + costs[i]);
        }

        myGUI.print("\n");
        myGUI.print("route");
        for(int i = 0; i < RouterSimulator.NUM_NODES; i++) {
            myGUI.print("\t" + routes[i]);
        }

        myGUI.print("\n");
        myGUI.print("\n");
        myGUI.print("\n");
 
    }

    //--------------------------------------------------
    public void updateLinkCost(int dest, int newcost) {
        neighbourVectors[myID][dest] = newcost;

        boolean dirty = recalculateCost();
        if(dirty) {
            sendVectors();
        }
    }

}
