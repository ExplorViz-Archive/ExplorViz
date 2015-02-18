package explorviz.plugin_server.rootcausedetection.util;

import java.util.ArrayList;
import java.util.List;

import explorviz.plugin_server.rootcausedetection.exception.InvalidDistanceGraphException;

public class DistanceGraph {

	private class Record {
		int weight = 0;
		int hash = 0;
		double rcr = 0.0;
		final List<Record> sources = new ArrayList<Record>();
	}

	private final Record tree;

	public DistanceGraph(int hash) {
		tree = new Record();
		tree.hash = hash;
	}

	public int addRecord(int hash, int targetHash) {
		try {
			if (getRecord(targetHash) == null) {
				return -1;
			}
			int distance = getDistance(hash);
			if (distance == 0) {
				Record own = new Record();
				own.hash = hash;
				getRecord(targetHash).sources.add(own);
				return hash;
			} else if (getDistance(targetHash) < (distance - 1)) {
				Record own = getRecord(hash);
				remove(hash, tree);
				getRecord(targetHash).sources.add(own);
				return hash;
			} else {
				return -1;
			}
		} catch (final Exception e) {
			throw new InvalidDistanceGraphException("DistanceGraph#add(...):InvalidTargetHashUsed");
		}
	}

	public void addWeightRCR(int hash, int weight, double rcr) {
		Record rec = getRecord(hash);
		if (rec != null) {
			rec.weight = weight;
			rec.rcr = rcr;
		}
	}

	public ArrayList<Integer> getWeights() {
		ArrayList<Integer> results = new ArrayList<Integer>();
		for (int i = 0; i < tree.sources.size(); i++) {
			results.addAll(getWeightsRec(tree.sources.get(i)));
		}
		return results;
	}

	private ArrayList<Integer> getWeightsRec(Record rec) {
		ArrayList<Integer> results = new ArrayList<Integer>();
		results.add(getWeight(rec.hash));
		for (int i = 0; i < rec.sources.size(); i++) {
			results.addAll(getWeightsRec(rec.sources.get(i)));
		}
		return results;
	}

	public ArrayList<Integer> getDistances() {
		ArrayList<Integer> results = new ArrayList<Integer>();
		for (int i = 0; i < tree.sources.size(); i++) {
			results.addAll(getDistancesRec(tree.sources.get(i)));
		}
		return results;
	}

	private ArrayList<Integer> getDistancesRec(Record rec) {
		ArrayList<Integer> results = new ArrayList<Integer>();
		results.add(getDistance(rec.hash));
		for (int i = 0; i < rec.sources.size(); i++) {
			results.addAll(getDistancesRec(rec.sources.get(i)));
		}
		return results;
	}

	public ArrayList<Double> getRCRs() {
		ArrayList<Double> results = new ArrayList<Double>();
		for (int i = 0; i < tree.sources.size(); i++) {
			results.addAll(getRCRsRec(tree.sources.get(i)));
		}
		return results;
	}

	private ArrayList<Double> getRCRsRec(Record rec) {
		ArrayList<Double> results = new ArrayList<Double>();
		results.add(rec.rcr);
		for (int i = 0; i < rec.sources.size(); i++) {
			results.addAll(getRCRsRec(rec.sources.get(i)));
		}
		return results;
	}

	private Record getRecord(int hash) {
		if (tree.hash == hash) {
			return tree;
		}
		for (int i = 0; i < tree.sources.size(); i++) {
			if (getRecordRecursive(hash, tree.sources.get(i)) != null) {
				return getRecordRecursive(hash, tree.sources.get(i));
			}
		}
		return null;
	}

	private Record getRecordRecursive(int hash, Record rec) {
		if (rec.hash == hash) {
			return rec;
		}
		for (int i = 0; i < rec.sources.size(); i++) {
			if (getRecordRecursive(hash, rec.sources.get(i)) != null) {
				return getRecordRecursive(hash, rec.sources.get(i));
			}
		}
		return null;
	}

	private void remove(int hash, Record rec) {
		for (int i = 0; i < rec.sources.size(); i++) {
			if (rec.sources.get(i).hash == hash) {
				rec.sources.remove(i);
				return;
			} else {
				remove(hash, rec.sources.get(i));
			}
		}
	}

	/*
	 * Returns the distance of the requested class
	 *
	 * @param hash - hash value of the requested class
	 *
	 * @return distance to the requested class
	 */
	private int getDistance(int hash) {
		return getDistanceRec(tree, hash, 0);
	}

	/*
	 * Recursive function to retrieve the distance of the requested class
	 *
	 * @param rec - Starting record hash - hash value of the requested class
	 * distance - starting distance, initialize with 0
	 *
	 * @return distance to the requested class
	 */
	private int getDistanceRec(Record rec, int hash, int distance) {
		if (rec.hash != hash) {
			if (rec.sources.size() >= 0) {
				int result = 0;
				for (int i = 0; i < rec.sources.size(); i++) {
					result += getDistanceRec(rec.sources.get(i), hash, distance + 1);
				}
				return result;
			} else {
				return 0;
			}
		} else {
			return distance;
		}
	}

	/*
	 * Returns the weight of the requested class
	 *
	 * @param hash - hash value of the requested class
	 *
	 * @return weight of all leafs leading to the requested class aggregated
	 */
	private int getWeight(int hash) {
		return getWeightRec(tree, hash, 0);
	}

	/*
	 * Recursive function to retrieve the weight of the requested class
	 *
	 * @param rec - Starting record hash - hash value of the requested class
	 * weight - starting weight, initialize with 0
	 *
	 * @return weight of all leafs leading to the requested class aggregated
	 */
	private int getWeightRec(Record rec, int hash, int weight) {
		if (rec.hash != hash) {
			if (rec.sources.size() >= 0) {
				int result = 0;
				for (int i = 0; i < rec.sources.size(); i++) {
					result += getWeightRec(rec.sources.get(i), hash, weight + rec.weight);
				}
				return result;
			} else {
				return 0;
			}
		} else {
			return weight + rec.weight;
		}
	}

}
