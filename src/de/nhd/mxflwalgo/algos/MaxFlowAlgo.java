package de.nhd.mxflwalgo.algos;

import java.util.ArrayList;
import java.util.Collection;

import de.nhd.mxflwalgo.model.Arc;
import de.nhd.mxflwalgo.model.MArc;
import de.nhd.mxflwalgo.model.MGraph;
import de.nhd.mxflwalgo.model.MaxFlowProblem;
import de.nhd.mxflwalgo.model.ResArc;

public abstract class MaxFlowAlgo {

	protected final MaxFlowProblem problem;
	protected boolean finished = false;
	protected MGraph resGraph;

	public MaxFlowAlgo(MaxFlowProblem maxFlowProblem) {
		this.problem = maxFlowProblem;
		this.createResGraph();
	}

	/**
	 * Run the whole algorithm
	 * 
	 * @return A graph that represents the final result
	 */
	public abstract MGraph run();

	/**
	 * Run one step of the algorithm
	 * 
	 * @return A graph that represents the intermediate result
	 */
	public abstract MGraph runStep();

	/**
	 * Check if the algorithm has already finished
	 * 
	 * @return True if finished, False otherwise
	 */
	public boolean isFinished() {
		return finished;
	};

	/**
	 * Initialize the residual graph.
	 */
	protected void createResGraph() {
		ArrayList<Arc> arcs = new ArrayList<>();
		for (Arc arc : this.problem.getGraph().getArcs()) {
			MArc mArc = (MArc) arc;

			ResArc forward = new ResArc(mArc.getStartVertex(), mArc.getEndVertex(),
					mArc.getCapacity() - mArc.getFlow(), null, arc, true);

			ResArc backward = new ResArc(mArc.getEndVertex(), mArc.getStartVertex(),
					mArc.getFlow(), forward, arc, false);

			forward.setBackwardResArc(backward);
			arcs.add(forward);
			arcs.add(backward);
			mArc.getStartVertex().addIncidentResArc(forward);
			mArc.getEndVertex().addIncidentResArc(backward);
		}
		this.resGraph = new MGraph(this.problem.getGraph().getVertices(), arcs);
		System.out
				.println("[Max-Flow Algorithm] Residual Graph created: (|V|,|A|) = ("
						+ this.resGraph.getVertices().size() + ", "
						+ this.resGraph.getArcs().size() + ")");
	}

	/**
	 * Update the graph and its residual graph along a flow augmenting path
	 * 
	 * @param augPath
	 *            The given flow augmenting path
	 */
	protected void updateGraphs(AugmentingPath augPath) {
		for (ResArc arc : augPath.arcs)
			arc.addFlow(augPath.value);
	}

	public MGraph getResGraph() {
		return this.resGraph;
	}

	/**
	 * Highlight all the arcs and nodes along the given augmenting path. The old
	 * highlight is also cleared optionally.
	 * 
	 * @param path
	 * @param clear
	 */
	protected void highlightAugPath(AugmentingPath path, boolean clear) {
		this.highlightArcs(new ArrayList<Arc>(path.arcs), clear);
	}

	/**
	 * Highlight all original arcs of the given residual arcs
	 * 
	 * @param arcs
	 * @param clear
	 */
	protected void highlightArcs(Collection<Arc> arcs, boolean clear) {
		if (clear)
			this.problem.getGraph().clearAllHighlight();
		for (Arc rArc : arcs) {
			this.problem.getGraph().highlightArc(((ResArc) rArc).getOriginalArc(), null);
			this.problem.getGraph().hightlightVertex(rArc.getStartVertex(), null);
			this.problem.getGraph().hightlightVertex(rArc.getEndVertex(), null);
		}
	}

	/**
	 * Inner class that represents a flow augmenting path
	 *
	 */
	public class AugmentingPath {

		/**
		 * The arcs on the residual graph that belong to this path
		 */
		public final ArrayList<ResArc> arcs;
		public final int value;

		public AugmentingPath(ArrayList<ResArc> arcs, int value) {
			this.arcs = arcs;
			this.value = value;
		}

		public String toString() {
			String str = value + " | ";
			for (ResArc arc : this.arcs)
				str += arc.getDirection() + " - ";
			return str.substring(0, str.length() - 2);
		}
	}

	/**
	 * Reset all calculated stuffs
	 */
	public void reset() {	
		this.finished = false;
		this.problem.getGraph().reset();
		this.createResGraph();	
	}
}
