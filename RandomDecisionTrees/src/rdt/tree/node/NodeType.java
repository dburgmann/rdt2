package rdt.tree.node;

/**
 * Enumeration for all different kinds of nodes. The node-type is used
 * to easily identify the type of the node by only using the getType()-method
 * of the node. With this we can avoid using instanceof during the runtime.
 * 
 * @author MK
 */
public enum NodeType {
	INNER_NODE,
	LEAF
}
