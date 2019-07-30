package parser;

import java.util.Arrays;
import java.util.List;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.trees.LabeledScoredTreeNode;
import edu.stanford.nlp.trees.Tree;

/**
 * 
 * @author thanhhungqb
 * @since Mar 2018
 *
 */
public class ExtendLabeledScoredTreeNode extends LabeledScoredTreeNode {
	/**
	 * 
	 */
	private static final long serialVersionUID = 171838183481L;
	private Tree _parent = null;
	private boolean isMark = false;

	/**
	 * Constructor from parent tree
	 * 
	 * @param tree
	 */
	public ExtendLabeledScoredTreeNode(LabeledScoredTreeNode tree) {
		super(tree.label(), Arrays.asList(tree.children()));

		ExtendLabeledScoredTreeNode t;
		for (Tree t1 : this.children()) {
			t = (ExtendLabeledScoredTreeNode) t1;
			t.setParent(this);
		}
	}

	public ExtendLabeledScoredTreeNode(Label label, List<ExtendLabeledScoredTreeNode> childs) {
		super(label);
		for (ExtendLabeledScoredTreeNode t : childs)
			this.addChild(t);

		for (ExtendLabeledScoredTreeNode t : childs)
			t.setParent(this);
	}

	public ExtendLabeledScoredTreeNode() {
		super();
	}

	public ExtendLabeledScoredTreeNode(Label label) {
		super(label);
	}

	public void setParent(Tree parent) {
		this._parent = parent;
	}

	@Override
	public Tree parent() {
		return this._parent;
	}

	public boolean isMark() {
		return isMark;
	}

	public void setMark(boolean isMark) {
		this.isMark = isMark;
	}
}
