package tuplesProject;

public class TuplesProject {

    public static void main(String[] args) {
        
        Tuple.setTypedTuple(new Tuple("").ap(0).lockSize("String-Integer Tuple"));
        
        Tuple tupla2 = Tuple.getTypedTuple("String-Integer Tuple").set("Text 1").set(5);
        
        System.out.println(tupla2);
        
        Tuple tupla3 = Tuple.getTypedTuple("String-Integer Tuple").set("Text 2").set(10);
        System.out.println(tupla3);
    }
}
