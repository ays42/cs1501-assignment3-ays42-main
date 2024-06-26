/**
 * An implementation of CompressionCodeBookInterface using a DLB Trie.
 */
 public class DLBCodeBook implements CompressionCodeBookInterface {

  private static final int R = 256;        // alphabet size
  private DLBNode root;
  public StringBuilder currentPrefix;
  private DLBNode currentNode;
  private int W;       // current codeword width
  private int minW;    // minimum codeword width
  private int maxW;    // maximum codeword width
  private int L;       // maximum number of codewords with 
                       // current codeword width (L = 2^W)
  private int code;    // next available codeword value

  public DLBCodeBook(int minW, int maxW){
    this.maxW = maxW;
    this.minW = minW;
    currentPrefix = new StringBuilder();
    currentNode = null;
    initialize();
  }

  public void add(String str, boolean flushIfFull){
    boolean haveRoom = false;
    if(root == null){
      root = new DLBNode(str.charAt(0));
    }
    if(code < L){
      haveRoom = true;
    }
    if(haveRoom){
      if(str.length() > 0){
        add(root, code, str, 0);
      }
      System.err.println(str + " " + code + " W = " + W); // debugging print statment - use str
      code++;
    }
  }

  private void add(DLBNode node, int codeword, String word, int index){
    DLBNode current = node;
    char c = word.charAt(index);
    while(current != null){
      if(current.data == c){
        if(index == word.length() - 1){
          current.codeword = codeword;
        } else { //move down
          if(current.child == null){
            current.child = new DLBNode(word.charAt(index+1));
          }
          add(current.child, codeword, word, index+1);
        }
        break;
      } else {
        if(current.sibling == null){
          current.sibling = new DLBNode(c);
        }
        current = current.sibling;
      }
    }
  }

  public int getCodewordWidth(){
    return W;
  }

  private void initialize(){
    root = null;
    W = minW;
    L = 1<<W;
    code = 0;
    for (int i = 0; i < R; i++)
      add("" + (char) i, false);
    add("", false); //R is codeword for EOF
  }


  public boolean advance(char c){
    boolean result = false;
    currentPrefix.append(c);
    if(currentNode == null){
      currentNode = root;
      while(currentNode != null){
        if(currentNode.data == c){
          result = true;
          break;
        }
        currentNode = currentNode.sibling;
      }
    } else {
      DLBNode curr = currentNode.child;
      while(curr != null){
        if(curr.data == c){
          currentNode = curr;
          result = true;
          break;
        }
        curr = curr.sibling;
      }
    }    
    return result;
  }
  
  public void add(boolean flushIfFull){
    boolean haveRoom = false;

    if(code < L){ //codebook is not full
      haveRoom = true;
    }
    if(code >= L) { //codebook is full
      if(W < maxW) { //check if W surpasses max
        W++;
        L *= 2; //double codebook size 
        haveRoom = true;
      } else if(W >= maxW) {
        if(flushIfFull) { //check flushIfFull
          initialize();
          haveRoom = true;
        } else { // else, other code in method won't run
          haveRoom = false;
        }
      }
    }

    if(haveRoom){
      DLBNode newNode = 
        new DLBNode(currentPrefix.charAt(currentPrefix.length()-1));
  
      newNode.codeword = code;
      System.err.println(currentPrefix.toString() + " " + code + " W = " + W); // debugging print statment - use currentPrefix

      code++;
      newNode.sibling = currentNode.child;
      currentNode.child = newNode;        
    }

    currentNode = null;
    currentPrefix = new StringBuilder();

  }

  public int getCodeWord() {
    return currentNode.codeword;
  }

  //The DLB node class
  private class DLBNode{
    private char data;
    private DLBNode sibling;
    private DLBNode child;
    private Integer codeword;

    private DLBNode(char data){
        this.data = data;
        child = sibling = null;        
    }
  } 
}