import java.util.LinkedList;
import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;

public  class JogoDos15 {
//Node==> Guarda informação sobre o estado atual: local da peça vazia, caminho percorrido até ao momento
    private static class node{
        int level = 0;
        int [][] state = new int [4][4];
        int blankRow;
        int blankCol;
        String move = "";
        node parent = null;
        node up = null;
        node down = null;
        node left = null;
        node right = null;
    }
//SolutionData==> Guarda informação sobre a solução alcançada, como o caminho percorrido/número de jogadas, a memória usada, e o tempo que demorou a chegar à solução
    private static class solutionData {
        int expandedNodes = 0;
        node solutionNode = null;
        long memoryUsed;
        long totalTime;
        String path = "";
        String startingBoard = "";
    }
//Leitura de dados
public static void main(String [] args){
        Scanner in = new Scanner(System.in);
        String board = "";
        int[][] objective = new int[4][4];
        node root = new node();
        root.move = "Start";
        //Lê estado inicial
        for (int i =0; i < 4; i++) {
            for (int q = 0; q < 4; q++) {
                root.state[i][q] = in.nextInt();
                board = board + root.state[i][q] +" ";
                if(root.state[i][q] == 0){
                    root.blankRow = i;
                    root.blankCol = q;
                }
            }
        }
        //Lê final desejado
        for(int i=0; i<4; i++){
            for(int k = 0; k<4; k++){
                objective[i][k] = in.nextInt();
            }
        }
        in.close();
        //Verifica se pode ser resolvido
        if(!solvable(root, objective)){
            System.out.println("Não tem solução! ;w;");
            return;
        }
        
//Tentativas de métodos
        System.gc();
        //Breadth-First Search
        System.out.println("\nDFS:"); 
        try{
            solutionData depthsolution = depthfind(root, objective, board);
            
            if(depthsolution != null)
                printSolutionData(depthsolution);
            else
                System.out.println("Can't find solution- DFS ran out of memory :(");

        }catch(OutOfMemoryError e){
            System.out.println("Can't find solution- DFS ran out of memory :(");
        }

        System.gc();

        //Breadth-First Search
        System.out.println("\nBFS:"); 
        try{
            solutionData breadthSolution = breadthFind(root, objective, board);
            
            if(breadthSolution != null)
                printSolutionData(breadthSolution);
            else
                System.out.println("Can't find solution- BFS ran out of memory :(");

        }catch(OutOfMemoryError e){
            System.out.println("Can't find solution- BFS ran out of memory :(");
        }
        System.gc();

        //Iterative depth first search
        System.out.println("\nIDDFS:");
        try{
            solutionData solution = iterDepthFind(root, objective, board);
            
            if(solution != null)
                printSolutionData(solution);
            else
                System.out.println("Can't find solution- IDDFS ran out of memory :(");

        }catch(OutOfMemoryError e){
            System.out.println("Can't find solution- IDDFS ran out of memory :(");
        }
    
        System.gc();

        //A* com heurística de manhattan
        System.out.println("\nA* Manhattan:");
        try{
            solutionData solution = aStarH1(root, board);
            if(solution != null)
                printSolutionData(solution);
            else
                System.out.println("Can't find solution- A*h1 ran out of memory :(");

        }catch(OutOfMemoryError e){
            System.out.println("Can't find solution- A*h1 ran out of memory :(");
        }
        System.gc();

        System.out.println("\nA* peças fora do local:");
        try{
            solutionData solution = aStarH2(root, objective, board);
            
            if(solution != null)
                printSolutionData(solution);
            else
                System.out.println("Can't find solution- A*h2 ran out of memory :(");

        }catch(OutOfMemoryError e){
            System.out.println("Can't find solution- A*h2 ran out of memory :(");
        }

        System.out.println("\nGreedy manhattan:"); 
        try{
            solutionData solution = greedyh1(root, objective, board);
            
            if(solution != null)
                printSolutionData(solution);
            else
                System.out.println("Can't find solution- greedy ran out of memory :(");

        }catch(OutOfMemoryError e){
            System.out.println("Can't find solution- greedy ran out of memory :(");
        }
        System.gc();

        System.out.println("\nGreedy misplaced tiles:"); 
        try{
            solutionData solution = greedyh2(root, objective, board);
            
            if(solution != null)
                printSolutionData(solution);
            else
                System.out.println("Can't find solution- greedy ran out of memory :(");

        }catch(OutOfMemoryError e){
            System.out.println("Can't find solution- greedy ran out of memory :(");
        }
        System.gc();
    }
//Métodos
//Greedy com heurística de misplaced tiles
    public static solutionData greedyh2(node root, int[][] objective, String board) {
        ArrayList<node> unexpandedNodes = new ArrayList<node>();
        unexpandedNodes.add(root);

        solutionData breadthSolution = new solutionData();
        int expandedNodes = 0;
        long startTime = System.currentTimeMillis();

        node cur = null;
        while(expandedNodes < Integer.MAX_VALUE){
            // get node with lowest f(n) = g(n)result
            int minVal = Integer.MAX_VALUE;
            for (int i = 0; i < unexpandedNodes.size() ; i++) {
                node tmp = unexpandedNodes.get(i);
                if(getMissplacedTiles(tmp,objective) < minVal){
                    minVal = getMissplacedTiles(tmp,objective);
                    cur = tmp;
                }
            }
            unexpandedNodes.remove(cur);

            if (goalTest(cur, objective)){
                breadthSolution.expandedNodes = expandedNodes;
                breadthSolution.solutionNode = cur;
                breadthSolution.startingBoard = board;
                breadthSolution.path = getPath(cur);

                Runtime runtime = Runtime.getRuntime();
                breadthSolution.memoryUsed = (runtime.totalMemory() - runtime.freeMemory())/(1024);
                breadthSolution.totalTime = System.currentTimeMillis() - startTime;
                
                return breadthSolution;
            }
            else{
                evaluateChildren(cur);
                expandedNodes++;

                if(cur.left != null)
                    unexpandedNodes.add(cur.left);
                if(cur.right != null)
                    unexpandedNodes.add(cur.right);
                if(cur.up != null)
                    unexpandedNodes.add(cur.up);
                if(cur.down != null)
                    unexpandedNodes.add(cur.down);
            }
        }
        return null;
    }
//Greedy com heurística de manhattan
    public static solutionData greedyh1(node root, int[][] objective, String board) {
         ArrayList<node> unexpandedNodes = new ArrayList<node>();
        unexpandedNodes.add(root);

        solutionData breadthSolution = new solutionData();
        int expandedNodes = 0;
        long startTime = System.currentTimeMillis();

        node cur = null;
        while(expandedNodes < Integer.MAX_VALUE){
            // get node with lowest f(n) = g(n)result
            int minVal = Integer.MAX_VALUE;
            for (int i = 0; i < unexpandedNodes.size() ; i++) {
                node tmp = unexpandedNodes.get(i);
                if(getDistanceSum(tmp) < minVal){
                    minVal = getDistanceSum(tmp);
                    cur = tmp;
                }
            }
            unexpandedNodes.remove(cur);

            if (goalTest(cur, objective)){
                breadthSolution.expandedNodes = expandedNodes;
                breadthSolution.solutionNode = cur;
                breadthSolution.startingBoard = board;
                breadthSolution.path = getPath(cur);

                Runtime runtime = Runtime.getRuntime();
                breadthSolution.memoryUsed = (runtime.totalMemory() - runtime.freeMemory())/(1024);
                breadthSolution.totalTime = System.currentTimeMillis() - startTime;
                
                return breadthSolution;
            }
            else{
                evaluateChildren(cur);
                expandedNodes++;

                if(cur.left != null)
                    unexpandedNodes.add(cur.left);
                if(cur.right != null)
                    unexpandedNodes.add(cur.right);
                if(cur.up != null)
                    unexpandedNodes.add(cur.up);
                if(cur.down != null)
                    unexpandedNodes.add(cur.down);
            }
        }
        return null;
    }
// Método A* que usa heurística de manhattan
    public static solutionData aStarH1(node root, String board){
        ArrayList<node> unexpandedNodes = new ArrayList<node>();
        unexpandedNodes.add(root);

        solutionData breadthSolution = new solutionData();
        int expandedNodes = 0;
        long startTime = System.currentTimeMillis();

        node cur = null;
        while(expandedNodes < Integer.MAX_VALUE){
            // get node with lowest f(n) = g(n) + h(n) result
            int minVal = Integer.MAX_VALUE;
            for (int i = 0; i < unexpandedNodes.size() ; i++) {
                node tmp = unexpandedNodes.get(i);
                if(getDistanceSum(tmp) + tmp.level < minVal){
                    minVal = getDistanceSum(tmp) + tmp.level;
                    cur = tmp;
                }
            }
            unexpandedNodes.remove(cur);

            if (getDistanceSum(cur) == 0){
                breadthSolution.expandedNodes = expandedNodes;
                breadthSolution.solutionNode = cur;
                breadthSolution.startingBoard = board;
                breadthSolution.path = getPath(cur);

                Runtime runtime = Runtime.getRuntime();
                breadthSolution.memoryUsed = (runtime.totalMemory() - runtime.freeMemory())/(1024);
                breadthSolution.totalTime = System.currentTimeMillis() - startTime;
                
                return breadthSolution;
            }
            else{
                evaluateChildren(cur);
                expandedNodes++;

                if(cur.left != null)
                    unexpandedNodes.add(cur.left);
                if(cur.right != null)
                    unexpandedNodes.add(cur.right);
                if(cur.up != null)
                    unexpandedNodes.add(cur.up);
                if(cur.down != null)
                    unexpandedNodes.add(cur.down);
            }
        }
        return null;
    }
// Método A* que usa número de peças fora do local devido
    public static solutionData aStarH2(node root,int[][] objective,String board){
        ArrayList<node> unexpandedNodes = new ArrayList<node>();
        unexpandedNodes.add(root);

        solutionData breadthSolution = new solutionData();
        int expandedNodes = 0;
        long startTime = System.currentTimeMillis();

        node cur = null;
        while(expandedNodes < Integer.MAX_VALUE){
            // get node with lowest f(n) = g(n) + h(n) result
            int minVal = Integer.MAX_VALUE;
            for (int i = 0; i < unexpandedNodes.size() ; i++) {
                node tmp = unexpandedNodes.get(i);
                if(getMissplacedTiles(tmp, objective) + tmp.level < minVal){
                    minVal = getMissplacedTiles(tmp, objective) + tmp.level;
                    cur = tmp;
                }
            }
            unexpandedNodes.remove(cur);

            if (getMissplacedTiles(cur, objective) == 0){
                breadthSolution.expandedNodes = expandedNodes;
                breadthSolution.solutionNode = cur;
                breadthSolution.startingBoard = board;
                breadthSolution.path = getPath(cur);

                Runtime runtime = Runtime.getRuntime();
                breadthSolution.memoryUsed = (runtime.totalMemory() - runtime.freeMemory())/(1024);
                breadthSolution.totalTime = System.currentTimeMillis() - startTime;
                
                return breadthSolution;
            }
            else{
                evaluateChildren(cur);
                expandedNodes++;

                if(cur.left != null)
                    unexpandedNodes.add(cur.left);
                if(cur.right != null)
                    unexpandedNodes.add(cur.right);
                if(cur.up != null)
                    unexpandedNodes.add(cur.up);
                if(cur.down != null)
                    unexpandedNodes.add(cur.down);
            }
        }
        return null;
    }
//Depth First Search
    public static solutionData depthfind(node root, int[][] objective, String board){
        
        solutionData depthsolution = new solutionData();
        int expandedNodes = 0;
        long startTime = System.currentTimeMillis();
        int depthLevel=0;
        while(depthLevel < Integer.MAX_VALUE){
            //ArrayList<String> savedStates = new ArrayList<String>();
            LinkedList<node> stack = new LinkedList<node>();
            stack.add(root);

            while(!stack.isEmpty()){
                node cur = stack.removeLast();

                if(goalTest(cur, objective)){
                    depthsolution.expandedNodes = expandedNodes;
                    depthsolution.solutionNode = cur;
                    depthsolution.startingBoard = board;
                    depthsolution.path = getPath(cur);

                    Runtime runtime = Runtime.getRuntime();
                    depthsolution.memoryUsed = (runtime.totalMemory() - runtime.freeMemory())/(1024);
                    depthsolution.totalTime = System.currentTimeMillis() - startTime;
                    
                    return depthsolution;
                }
                else{
                    evaluateChildren(cur);
                    expandedNodes++;

                    if(cur.left != null)
                        stack.add(cur.left);
                    if(cur.right != null)
                        stack.add(cur.right);
                    if(cur.up != null)
                        stack.add(cur.up);
                    if(cur.down != null)
                        stack.add(cur.down);
                }
            }
            depthLevel++;
        }
        // should never happen unless puzzle is not valid or ran out of memory
        return null;
    }

// Método breadth-first search
    public static solutionData breadthFind(node root, int[][] objective, String board){
        LinkedList<node> queue = new LinkedList<node>();
        queue.add(root);
        
        solutionData breadthSolution = new solutionData();
        int expandedNodes = 0;
        long startTime = System.currentTimeMillis();

        while(!queue.isEmpty() && expandedNodes <= Integer.MAX_VALUE){
            node cur = queue.removeFirst();

            if(goalTest(cur, objective)){
                breadthSolution.expandedNodes = expandedNodes;
                breadthSolution.solutionNode = cur;
                breadthSolution.startingBoard = board;
                breadthSolution.path = getPath(cur);

                Runtime runtime = Runtime.getRuntime();
                breadthSolution.memoryUsed = (runtime.totalMemory() - runtime.freeMemory())/(1024);
                breadthSolution.totalTime = System.currentTimeMillis() - startTime;
                
                return breadthSolution;
            }
            else {
                // expands node
                evaluateChildren(cur);
                expandedNodes++;

                if(cur.left != null)
                    queue.add(cur.left);
                if(cur.right != null)
                    queue.add(cur.right);
                if(cur.up != null)
                    queue.add(cur.up);
                if(cur.down != null)
                    queue.add(cur.down);
            }
        }
        // should never happen unless puzzle is not valid or ran out of memory
        return null;
    }

// Método de profundidade iterativa
    public static solutionData iterDepthFind(node root, int[][] objective, String board){
        int depthLevel = 0;
        solutionData breadthSolution = new solutionData();
        int expandedNodes = 0;
        long startTime = System.currentTimeMillis();
        
        while(depthLevel < Integer.MAX_VALUE){
            //ArrayList<String> savedStates = new ArrayList<String>();
            LinkedList<node> stack = new LinkedList<node>();
            stack.add(root);

            while(!stack.isEmpty()){
                node cur = stack.removeLast();

                if(goalTest(cur, objective)){
                    breadthSolution.expandedNodes = expandedNodes;
                    breadthSolution.solutionNode = cur;
                    breadthSolution.startingBoard = board;
                    breadthSolution.path = getPath(cur);

                    Runtime runtime = Runtime.getRuntime();
                    breadthSolution.memoryUsed = (runtime.totalMemory() - runtime.freeMemory())/(1024);
                    breadthSolution.totalTime = System.currentTimeMillis() - startTime;
                    
                    return breadthSolution;
                }
                else if(cur.level < depthLevel){
                    // savedStates.add(curState);
                    evaluateChildren(cur);
                    expandedNodes++;

                    if(cur.left != null)
                        stack.add(cur.left);
                    if(cur.right != null)
                        stack.add(cur.right);
                    if(cur.up != null)
                        stack.add(cur.up);
                    if(cur.down != null)
                        stack.add(cur.down);
                }
            }
            depthLevel++;
        }
        // should never happen unless puzzle is not valid or ran out of memory
        return null;
    }
//Auxiliares(Imprimir dados, guardar estados, calcular distâncias e criar nós filhos, verificar se foi resolvido)
    // Cria ID de um dado estado para ser armazenado
    public static String makeStateID(node curNode){
        String id = "";
        for(int i = 0; i < 4; i++){
            for (int q = 0; q < 4; q++)
                id = id + ((char) (65 + curNode.state[i][q]));
        }
        return id;
    }

    // Verifica jogadas válidas
    public static void evaluateChildren(node curNode){
        if(curNode.blankCol > 0){
            curNode.left = new node();
            curNode.left.move = "L";
            curNode.left.level = curNode.level + 1;
            curNode.left.blankCol = curNode.blankCol - 1;
            curNode.left.blankRow = curNode.blankRow;
            curNode.left.parent = curNode;
            curNode.left.state = makeState(curNode.state, curNode.blankRow, curNode.blankCol, 'L');
        }
        if(curNode.blankCol < 3){
            curNode.right = new node();
            curNode.right.move = "R";
            curNode.right.level = curNode.level + 1;
            curNode.right.blankCol = curNode.blankCol + 1;
            curNode.right.blankRow = curNode.blankRow;
            curNode.right.parent = curNode;     
            curNode.right.state = makeState(curNode.state, curNode.blankRow, curNode.blankCol, 'R');

        }
        if(curNode.blankRow > 0){
            curNode.up = new node();
            curNode.up.move = "U";
            curNode.up.level = curNode.level + 1;
            curNode.up.blankCol = curNode.blankCol;
            curNode.up.blankRow = curNode.blankRow - 1;
            curNode.up.parent = curNode;
            curNode.up.state = makeState(curNode.state, curNode.blankRow, curNode.blankCol, 'U');
        
        }
         if(curNode.blankRow < 3){
            curNode.down = new node();
            curNode.down.move = "D";
            curNode.down.level = curNode.level + 1;
            curNode.down.blankCol = curNode.blankCol;
            curNode.down.blankRow = curNode.blankRow + 1;
            curNode.down.parent = curNode;
            curNode.down.state = makeState(curNode.state, curNode.blankRow, curNode.blankCol, 'D');
        }       
    }
    
    // retorna um novo estado dado o estado atual e a posição da peça vazia, através das direções UP, DOWN, LEFT, e RIGHT
    public static int[][] makeState(int[][] curState, int blankY, int blankX, char move){
        int [][] newState = new int [4][];
        for(int i = 0; i < 4; i++)
            newState[i] = curState[i].clone();

        if(move == 'U'){
            newState[blankY][blankX] = newState[blankY - 1][blankX];
            newState[blankY - 1][blankX] = 0;
        }
        else if(move == 'D'){
            newState[blankY][blankX] = newState[blankY + 1][blankX];
            newState[blankY + 1][blankX] = 0;
        }
        else if(move == 'L'){
            newState[blankY][blankX] = newState[blankY][blankX - 1];
            newState[blankY][blankX - 1] = 0;
        }
        else{
            newState[blankY][blankX] = newState[blankY][blankX + 1];
            newState[blankY][blankX + 1] = 0;
        }
        return newState;
    }

    // compara o objetivo com o cur atual para determinar a distância de manhattan
    public static int getDistanceSum(node curNode){
        int manhatSum = 0;
        for (int row = 0; row < 4 ; row++) {
            for (int col = 0; col < 4 ; col++ ) {
                int x;
                int y;
                int val = curNode.state[row][col];
                
                if(val >= 1 && val <= 4){
                    y = 0;
                    x = val - 1;
                }
                else if(val >= 5 && val <= 8){
                    y = 1;
                    x = val - 5;
                }
                else if(val >= 9 && val <= 12){
                    y = 2;
                    x = val - 9;
                }
                else if(val >= 13 && val <= 15){
                    y = 3;
                    x = val - 13;
                }
                else{
                    y = 3;
                    x = 3;
                }

                manhatSum = manhatSum + Math.abs(row - y) + Math.abs(col - x);
            }
        }
        return manhatSum;
    }

    // compara o objetivo com o cur atual para determinar as peças fora do local devido
    public static int getMissplacedTiles(node curNode, int[][] ending){
        int tileCounter = 0;
        for (int row = 0; row<4; row++){
            for (int col = 0; col < 4; col++){
                if(ending[row][col] != curNode.state[row][col])
                    tileCounter++;
            }
        }
        return tileCounter;
    }

    // verifica se foi resolvido
    public static boolean goalTest(node curNode, int[][] ending){
        return Arrays.deepEquals(curNode.state, ending);    
    }

    // Retorna o caminho percorrido para chegar à solução
    public static String getPath(node solution){
        LinkedList<node> solutionPath = new LinkedList<node>();
        node cur = solution;
        String path = "";
        while(cur != null){
            solutionPath.addFirst(cur);
            cur = cur.parent;
        }
        while(!solutionPath.isEmpty()){
            node tmp = solutionPath.removeFirst();
            if(tmp.move != "Start")
                path = path + tmp.move;
        }
        return path;
    }

    // Imprime os dados da solução, como a memória utilizada, o número de passos, a profundidade a que se chegou, e o tempo total
    public static void printSolutionData(solutionData solution){
        System.out.println(solution.startingBoard + "  Moves:" + solution.path.length() + "  Steps:" + solution.path);
        System.out.println("Memory: " + solution.memoryUsed +"kb   Time: "+ solution.totalTime + "ms   Expanded Nodes:" + solution.expandedNodes);
    }

//Verifica solubilidade
    public static boolean solvable(node cur, int[][] objective){
        if (condI(cur) == condF(objective)) return true;
        return false;
    }

    // condiçao do estado inicial
    public static boolean condI(node cur){
        int blankRow = 0;
	int[] line = new int[16];
	int k=0;
        for(int i=0; i<4; i++){
            for(int j=0; j<4; j++){
		line[k] = cur.state[i][j];
		k++;
                if(cur.state[i][j] == 0){
		    blankRow = 4-i;
		}
	    }
	}
	return (inv(line) % 2 == blankRow % 2);
    }

    // condição do estado final
    public static boolean condF(int[][] objective){
        int blankRow = 0;
	int[] line = new int[16];
	int k=0;
        for(int i=0; i<4; i++){
            for(int j=0; j<4; j++){
		line[k] = objective[i][j];
		k++;
                if(objective[i][j] == 0){
                    blankRow = 4-i;
                }
            }
        }
        return (inv(line) % 2 == blankRow % 2);
    }
    //Conta inversões
    public static int inv(int[] x){
        int total = 0;
        for(int i=0 ; i<16 ; i++){
            for(int j=i+1 ; j<16 ; j++)
                if(i < j && x[i] > x[j] && x[i]!=0 && x[j]!=0){
                    total++;
                }
        }
        return total;
    }
}