
public class RouterPacket implements Cloneable {
  int sourceid;       /* id of sending router sending this pkt */
  int destid;         /* id of router to which pkt being sent 
                         (must be an immediate neighbor) */
  int[] mincost = new int[RouterSimulator.NUM_NODES];    /* min cost to node 0 ... 3 */


  RouterPacket (int sourceID, int destID, int[] mincosts){
    this.sourceid = sourceID;
    this.destid = destID;
    System.arraycopy(mincosts, 0, this.mincost, 0, RouterSimulator.NUM_NODES);
  }

  public Object clone(){
    try {
      RouterPacket newPkt = (RouterPacket) super.clone();
      newPkt.mincost = (int[]) newPkt.mincost.clone();
      return newPkt;
    }
    catch(CloneNotSupportedException e){
      System.err.println(e);
      System.exit(1);
    }
    return null;
  }
    
}

