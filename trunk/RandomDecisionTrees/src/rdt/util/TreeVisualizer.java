package rdt.util;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import rdt.tree.Tree;
import rdt.tree.collector.Collector;
import rdt.tree.node.InnerNode;
import rdt.tree.node.Leaf;
import rdt.tree.node.Node;
import rdt.tree.node.NodeType;
import rdt.tree.splitter.Splitter;

/**
 * Class to visualize the structure of a tree. You can just use the static method "visualize(Tree tree)"
 * to simply visualize a tree.
 * 
 * @author MK
 */
public class TreeVisualizer extends JFrame {
	private static final long serialVersionUID = 572752279369119717L;

	public TreeVisualizer(Tree tree)
	{		
		super("TreeVisualizer");
		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();
		
		graph.getModel().beginUpdate();
		

		recursiveVisualization(graph, parent, tree.getRoot(), 0, new Pair<Object,Integer>(null,0));
		
		graph.getModel().endUpdate();
		
		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		getContentPane().add(graphComponent);
		
		graphComponent.addMouseWheelListener(new MouseWheelListener(){

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				
				int tmp = e.getWheelRotation();
				
				if(tmp > 0){
					graphComponent.zoomOut();
				}else{
					graphComponent.zoomIn();
				}
				
			}
			
		});
		
	}
	

	
	private Pair<Object,Integer> recursiveVisualization(mxGraph graph, Object parent, Node node, int currentDeep, Pair<Object,Integer> y){	
		currentDeep++;
		
		if(node.getNodeType() == NodeType.INNER_NODE){
			InnerNode in = (InnerNode) node;
			Splitter splitter = in.getSplitter();
			
			
			Node[] children = in.getChildren();
			
			int oldY = y.getSecond();
			
			List<Object> objects = new LinkedList<Object>();
			
			for(int i=0; i<children.length; i++){
				y = recursiveVisualization(graph, parent, children[i], currentDeep, y);
				objects.add(y.getFirst());
			}
			
			Object v = graph.insertVertex(parent, null, splitter.getType().name(), 100*((oldY+y.getSecond())/2), 50*currentDeep, 80, 30);
			
			for(int i=0; i<objects.size(); i++){
				graph.insertEdge(parent, null, i, v, objects.get(i));
			}
			
			return new Pair<Object,Integer>(v,y.getSecond());
			
		}else{
			Leaf leaf = (Leaf) node;
			
			Collector[] cols = leaf.getCollectors();
			
			StringBuilder sb = new StringBuilder();
			
			for(int i=0; i<cols.length; i++){
				sb.append((int)cols[i].getNumInst());
				sb.append("\n");
			}
			
			Object v = graph.insertVertex(parent, null, sb.toString(), 100*y.getSecond(), 50*currentDeep, 30, 30);
			return new Pair<Object,Integer>(v,y.getSecond()+1);
		}
		
	}
	
	/**
	 * Visualizes the given tree in a new frame.
	 * 
	 * @param tree the tree which will be visualized
	 */
	public static void visualize(Tree tree){
		TreeVisualizer frame = new TreeVisualizer(tree);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(MAXIMIZED_BOTH);
		frame.setVisible(true);
	}

}